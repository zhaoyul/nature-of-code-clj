#!/usr/bin/env python3
"""Locate original teaching fragments without rewriting any translated prose."""
from pathlib import Path
import hashlib
import json
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
TRANSLATION = ROOT / "resources/translations/the-nature-of-code/sections"
OUTPUT = ROOT / "resources/clojure-book/layout"


def code_blocks(markdown):
    document = json.loads(subprocess.check_output(
        ["pandoc", "--from=markdown+tex_math_dollars", "--to=json"],
        input=markdown.encode()))
    found = []

    def visit(value):
        if isinstance(value, dict):
            if value.get("t") == "CodeBlock":
                found.append(value["c"][1])
            for child in value.values():
                visit(child)
        elif isinstance(value, list):
            for child in value:
                visit(child)
    visit(document["blocks"])
    return found


def locate(markdown, blocks):
    lines = markdown.splitlines(keepends=True)
    offsets = []
    position = 0
    for line in lines:
        offsets.append(position)
        position += len(line)
    previous = 0
    spans = []
    for index, source in enumerate(blocks):
        expected = source.splitlines()
        match = None
        for start in range(previous, len(lines)):
            first = lines[start].rstrip("\r\n")
            if not first.endswith(expected[0]):
                continue
            indent = first[:len(first) - len(expected[0])]
            if len(indent) < 4 or indent.strip():
                continue
            candidates = lines[start:start + len(expected)]
            if len(candidates) != len(expected):
                continue
            if all((not actual.strip() if not wanted else
                    actual.rstrip("\r\n") == indent + wanted)
                   for actual, wanted in zip(candidates, expected)):
                match = (offsets[start], offsets[start] + sum(map(len, candidates)), source)
                previous = start + len(expected)
                break
        assert match is not None, f"Cannot locate original code block {index}: {source[:80]}"
        spans.append(match)
    return spans


def edn_string(value):
    return json.dumps(value, ensure_ascii=False)


CHINESE = re.compile(r"[\u3400-\u9fff\U00020000-\U000323af]")


def javascript_annotations(source):
    """Yield comment/string contents in order, ignoring markers inside strings.

    The book includes partial functions and classes, so extracting annotations
    must not depend on a JavaScript parser accepting a complete program.
    """
    index = 0
    while index < len(source):
        if source[index] in "\"'`":
            quote = source[index]
            start = index + 1
            index = start
            while index < len(source) and source[index] != quote:
                if source[index] == "\\":
                    index += 2  # An escaped quote does not end the string.
                else:
                    index += 1
            yield "string", source[start:index]
            index += 1
        elif source.startswith("//", index):
            start = index + 2
            end = source.find("\n", start)
            if end < 0:
                end = len(source)
            yield "comment", source[start:end]
            index = end
        elif source.startswith("/*", index):
            start = index + 2
            end = source.find("*/", start)
            if end < 0:
                end = len(source)
            yield "comment", source[start:end]
            index = end + 2
        else:
            index += 1


def javascript_string_value(text):
    """Decode quoted literal escapes without changing already literal Chinese."""
    escapes = {"n": "\n", "r": "\r", "t": "\t", "b": "\b", "f": "\f",
               "v": "\v", "0": "\0", "\n": "", "\r\n": ""}

    def unescape(match):
        value = match.group(0)[1:]
        if value.startswith("u{"):
            return chr(int(value[2:-1], 16))
        if value.startswith(("u", "x")) and len(value) > 1:
            return chr(int(value[1:], 16))
        return escapes.get(value, value)

    return re.sub(r"\\(?:u\{[0-9A-Fa-f]+\}|u[0-9A-Fa-f]{4}|x[0-9A-Fa-f]{2}|\r\n|[\s\S])",
                  unescape, text)


def comment_lines(comment):
    # Preserve English/math lines too when they belong to a Chinese block.
    lines = [re.sub(r"^\* ?", "", line.strip()) for line in comment.splitlines()]
    while lines and not lines[0]:
        lines.pop(0)
    while lines and not lines[-1]:
        lines.pop()
    return lines


def clojure_sources(path):
    """Read the JSON-compatible source strings in the generated EDN resources."""
    if not path.exists():
        return {}
    pattern = r':index\s+(\d+)\s+:source\s+("(?:\\.|[^"\\])*")'
    return {int(index): json.loads(source)
            for index, source in re.findall(pattern, path.read_text())}


def original_comments(source, ported_source=""):
    result = []
    for kind, text in javascript_annotations(source):
        if kind == "comment" and CHINESE.search(text):
            result.extend(comment_lines(text))
        elif kind == "string":
            value = javascript_string_value(text)
            if CHINESE.search(value) and value not in ported_source:
                # A translated display/status string is still part of the book
                # even when the Clojure adaptation displays a different result.
                result.extend("原代码中文字符串：" + line for line in value.splitlines())
    return result


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    count = 0
    for path in sorted(TRANSLATION.glob("*.md")):
        if path.stem == "index":  # The index's indented blocks contain text, not programs.
            continue
        markdown = path.read_text()
        blocks = code_blocks(markdown)
        if not blocks:
            continue
        ported_sources = clojure_sources(ROOT / "resources/clojure-book" / (path.stem + ".edn"))
        spans = locate(markdown, blocks)
        boundaries = [m.start() for m in re.finditer(
            r"(?m)^(?:#{1,6}\s+|\d+\.\s+)示例\s+\d+\.\d+", markdown)]
        boundaries = sorted(set([0] + boundaries + [len(markdown)]))
        pieces = []
        cursor = 0
        prose_index = 0
        for index, (start, end, source) in enumerate(spans):
            assert not any(start < b < end for b in boundaries), "Example heading inside source code"
            cuts = [b for b in boundaries if cursor < b < start] + [start]
            for stop in cuts:
                if stop > cursor:
                    part = sum(b <= cursor for b in boundaries[:-1]) - 1
                    pieces.append("{:kind :markdown :part " + str(part)
                                  + " :index " + str(prose_index) + " :text "
                                  + edn_string(markdown[cursor:stop]) + "}")
                    prose_index += 1
                    cursor = stop
            part = sum(b <= start for b in boundaries[:-1]) - 1
            pieces.append("{:kind :clojure :part " + str(part) + " :index " + str(index)
                          + " :original-sha256 " + edn_string(hashlib.sha256(source.encode()).hexdigest())
                          + " :comments [" + " ".join(map(edn_string, original_comments(
                              source, ported_sources.get(index, "")))) + "]}")
            cursor = end
        for stop in [b for b in boundaries if cursor < b]:
            part = sum(b <= cursor for b in boundaries[:-1]) - 1
            pieces.append("{:kind :markdown :part " + str(part) + " :index " + str(prose_index)
                          + " :text " + edn_string(markdown[cursor:stop]) + "}")
            prose_index += 1
            cursor = stop
        result = "{:source-sha256 " + edn_string(hashlib.sha256(path.read_bytes()).hexdigest())
        result += " :code-blocks " + str(len(spans)) + " :segments [\n" + "\n".join(pieces) + "]}\n"
        (OUTPUT / (path.stem + ".edn")).write_text(result)
        count += len(spans)
        print(path.stem, len(spans), "Clojure slots", prose_index, "unchanged prose fragments")
    print("Located", count, "teaching code blocks; original files unchanged.")


if __name__ == "__main__":
    main()

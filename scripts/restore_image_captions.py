#!/usr/bin/env python3
"""Restore every original Chinese figure caption as visible Markdown prose.

Invoked after inline-markup repairs by normalize_translation.py. The source Org is
read-only. Source image order and paths are checked before editing a section;
already-restored captions make repeated invocations idempotent.
"""
from collections import Counter
from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[1] / "resources/translations/the-nature-of-code"
IMAGE = re.compile(r"(?m)^([ \t]*)!\[.*\]\(\.\./images/([^\n)]+)\)[ \t]*$")
SOURCE_IMAGE = re.compile(r"\[\[file:images/([^\]]+)\]\]")


def caption_markdown(caption):
    # Protect the original TeX from Pandoc's Org punctuation and subscript rules.
    caption = re.sub(r"\\\((.*?)\\\)", lambda m: "$" + m.group(1) + "$", caption)
    # Original captions contain simple Org verbatim tokens, without = operators.
    caption = re.sub(r"(?<!\S)=([^=\n]+)=(?=$|[\s.,;:!?、，。])", r"`\1`", caption)
    caption = re.sub(r"(?<!\S)/([^/\n]+)/(?=$|[\s.,;:!?、，。])", r"*\1*", caption)
    return caption


def source_captions(section):
    pending = None
    captions = []
    for line in section.splitlines():
        if line.upper().startswith("#+CAPTION:"):
            assert pending is None, "Caption without a preceding image"
            pending = line.split(":", 1)[1].strip()
        for match in SOURCE_IMAGE.finditer(line):
            assert pending is not None, f"Image without an original caption: {match.group(1)}"
            captions.append((match.group(1), caption_markdown(pending)))
            pending = None
    assert pending is None, "Trailing caption without an image"
    return captions


def main():
    original = (ROOT / "book.zh.org").read_text()
    records = json.loads((ROOT / "sections.json").read_text())
    updates = {}
    total = 0
    added = Counter()
    for record in records:
        bounds = record.get("source_char_range", record.get("source-char-range"))
        expected = source_captions(original[bounds[0]:bounds[1]])
        path = ROOT / record["path"]
        markdown = path.read_text()
        images = list(IMAGE.finditer(markdown))
        assert [m.group(2) for m in images] == [p for p, _ in expected], f"Image order/path mismatch: {record['id']}"
        pieces = []
        previous = 0
        for image, (_, caption) in zip(images, expected):
            pieces.append(markdown[previous:image.end()])
            following = markdown[image.end():].lstrip("\n")
            existing = following.split("\n", 1)[0].strip()
            if existing != caption:
                pieces.append("\n\n" + image.group(1) + caption)
                added[record["id"]] += 1
            previous = image.end()
            total += 1
        pieces.append(markdown[previous:])
        restored = "".join(pieces)
        if restored != markdown:
            updates[path] = restored

    # All source/path assertions have succeeded before the first file is written.
    for path, restored in updates.items():
        path.write_text(restored)
    print(f"Verified {total} original caption uses; restored {sum(added.values())}: "
          + (", ".join(f"{chapter}={count}" for chapter, count in added.items()) or "already applied"))


if __name__ == "__main__":
    main()

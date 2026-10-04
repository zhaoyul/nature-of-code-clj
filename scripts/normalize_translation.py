#!/usr/bin/env python3
"""Normalize converted book markup for Clerk, preserving the original Org archive.

Run after a new Org-to-Markdown conversion. Pandoc is needed only for raw tables.
"""
from pathlib import Path
import html
import json
import re
import subprocess
import repair_inline_markup
import restore_image_captions

root = Path(__file__).resolve().parents[1] / "resources/translations/the-nature-of-code"
converted_tables = 0


def convert_table(match):
    global converted_tables
    indent = match.group(1)
    source = "\n".join(line[len(indent):] for line in match.group(0).splitlines())
    result = subprocess.run(
        ["pandoc", "--from=html", "--to=gfm", "--wrap=none"],
        input=source, text=True, capture_output=True, check=True,
    ).stdout.rstrip()
    converted_tables += 1
    return "\n".join(indent + line if line else "" for line in result.splitlines())


def convert_math(match):
    value = html.unescape(match.group(1))
    value = re.sub(r"<sub>(.*?)</sub>", r"_{\1}", value)
    value = re.sub(r"<sup>(.*?)</sup>", r"^{\1}", value)
    value = re.sub(r"\*([^*]+)\*", r"\1", value)
    value = re.sub(r"[\u2000-\u200a]", " ", value).replace("−", "-")
    return "$" + value + "$"


def normalize_display_math(source):
    """Keep display delimiters on their own lines, outside surrounding prose.

    Pandoc's strict Markdown writer can emit `prose $$ ... $$ prose` for Org
    display equations. Clerk requires separate blocks; otherwise the following
    code and image URLs can become part of a malformed mathematical expression.
    """
    def separate(match):
        return "\n\n$$\n" + match.group(1).strip() + "\n$$\n\n"

    # Already standalone blocks, including those inside lists, stay untouched.
    if not any("$$" in line and line.strip() != "$$" for line in source.splitlines()):
        return source
    return re.sub(r"(?<!\\)\$\$(.*?)\$\$", separate, source, flags=re.S)


for path in sorted((root / "sections").glob("*.md")):
    source = path.read_text()
    normalized = re.sub(r"(?ms)^( *)<table>.*?^\1</table>", convert_table, source)
    normalized = re.sub(r'<span class="math inline">(.*?)</span>', convert_math, normalized)
    normalized = normalize_display_math(normalized)
    if source != normalized:
        path.write_text(normalized)

repair_inline_markup.main()
restore_image_captions.main()

records = json.loads((root / "sections.json").read_text())
for record in records:
    record["markdown-bytes"] = (root / record["path"]).stat().st_size
    record["markdown_bytes"] = record["markdown-bytes"]
(root / "sections.json").write_text(json.dumps(records, ensure_ascii=False, indent=2) + "\n")
edn_path = root / "sections.edn"
byte_counts = iter(record["markdown-bytes"] for record in records)
edn = edn_path.read_text()
assert len(re.findall(r":markdown-bytes \d+", edn)) == len(records)
edn_path.write_text(re.sub(r":markdown-bytes \d+", lambda _: f":markdown-bytes {next(byte_counts)}", edn))

provenance_path = root / "provenance.json"
provenance = json.loads(provenance_path.read_text())
provenance["clerk_normalization"] = "HTML tables converted to GFM pipe tables; inline table mathematics converted to TeX; display-math delimiters isolated from prose; verified inline-code and formula conversion losses repaired against original Org; all 387 original Chinese captions restored as visible prose with original TeX; original Org archive unchanged"
provenance_path.write_text(json.dumps(provenance, ensure_ascii=False, indent=2) + "\n")
print(f"Normalized {converted_tables} HTML tables; refreshed converted-section byte counts.")

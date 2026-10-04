#!/usr/bin/env python3
"""Repair verified Pandoc Org inline-markup losses in converted chapters.

Invoked by scripts/normalize_translation.py when rebuilding the Markdown.
Each repair is backed by a literal fragment of the unchanged Chinese Org.
This intentionally touches neither source code blocks nor source archive files.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "resources/translations/the-nature-of-code"

# chapter, converted fragment, faithful Markdown, exact original Org evidence
REPAIRS = [
    ("ch00", "`3 ==` '3'=", "`3 === '3'`", "=3 === '3'="),
    ("ch00", "`3 =` '3'=", "`3 == '3'`", "=3 == '3'="),
    ("ch01", "`x=、=y=、=z`", "`x`、`y`、`z`", "=x=、=y=、=z="),
    ("ch01", "`xspeed=、=yspeed=、=zspeed`", "`xspeed`、`yspeed`、`zspeed`", "=xspeed=、=yspeed=、=zspeed="),
    ("ch01", "`x=、=y`", "`x`、`y`", "=x=、=y="),
    ("ch01", "`xspeed=、=yspeed`", "`xspeed`、`yspeed`", "=xspeed=、=yspeed="),
    ("ch01", "`add()=、=sub()=、=mult()`", "`add()`、`sub()`、`mult()`", "=add()=、=sub()=、=mult()="),
    ("ch02", r"\$G\$、\$m<sub>1</sub>\$", "$G$、$m_1$", "$G$、$m_1$"),
    ("ch02", "`position1=、=position2`", "`position1`、`position2`", "=position1=、=position2="),
    ("ch02", "`mass1=、=mass2`", "`mass1`、`mass2`", "=mass1=、=mass2="),
    ("ch02", "`i ==` j=", "`i === j`", "=i === j="),
    ("ch03", "`PI=、=TWO_PI`", "`PI`、`TWO_PI`", "=PI=、=TWO_PI="),
    ("ch03", r"\$180^ˆ\$", r"$180^\circ$", r"$180^\circ$"),
    ("ch03", r"\$0\$、\$π\$、\$2π\$", r"$0$、$\pi$、$2\pi$", r"$0$、$\pi$、$2\pi$"),
    ("ch03", "`this.angle=、=this.angleVelocity`", "`this.angle`、`this.angleVelocity`", "=this.angle=、=this.angleVelocity="),
    ("ch03", "`beginShape()=、=vertex()=、=endShape()`", "`beginShape()`、`vertex()`、`endShape()`", "=beginShape()=、=vertex()=、=endShape()="),
    ("ch05", "`this ==` other=", "`this === other`", "=this === other="),
    ("ch05", "`this !=` other=", "`this !== other`", "=this !== other="),
    ("ch11", "`frameCount % 100 ==` 0=", "`frameCount % 100 === 0`", "=frameCount % 100 === 0="),
]


def main():
    original = (ROOT / "book.zh.org").read_text()
    sources = {}
    changes = {}
    for chapter, before, after, evidence in REPAIRS:
        assert evidence in original, f"Original Org evidence missing: {chapter}: {evidence}"
        path = ROOT / "sections" / f"{chapter}.md"
        if path not in sources:
            sources[path] = path.read_text()
        count = sources[path].count(before)
        if not count:
            assert after in sources[path], f"Neither original nor repaired markup exists: {chapter}: {before}"
            continue
        assert count == 1, f"Ambiguous converted fragment: {chapter}: {before}"
        sources[path] = sources[path].replace(before, after)
        changes[chapter] = changes.get(chapter, 0) + 1

    for path, converted in sources.items():
        if converted != path.read_text():
            path.write_text(converted)
    print("Verified inline-markup repairs: " + (", ".join(f"{chapter}={count}" for chapter, count in changes.items()) or "already applied"))


if __name__ == "__main__":
    main()

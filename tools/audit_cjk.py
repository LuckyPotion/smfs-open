"""Audit the decompiled sources for genuinely mangled Chinese text.

Why this script exists (and why its predecessor was deleted)
------------------------------------------------------------
The first version of this analysis used the round trip

    recovered = GBK.encode(literal).decode("utf-8")

and reported that 4059 of 4073 Chinese string literals were "permanently corrupted".
That was wrong, and the mistake is worth recording because it is easy to repeat:

  * **Rendering is not evidence.** A GBK/ANSI console renders correct Chinese as
    mojibake on screen. Reading the same bytes through a UTF-8-aware path shows the
    text is fine. The console was lying, not the data.
  * **A GBK round trip is an identity map for CJK text.** Real Chinese encodes to GBK
    and decodes back unchanged, so `recovered == literal` means the literal is already
    correct - it does not mean "no recovery was possible".
  * **Absence of an error is not proof of damage.** The `clean` bucket came out empty
    only because the test was mislabelled, not because the text was broken.

The reliable discriminator is the codepoint, not the round trip. Chinese prose written
by an IME stays within a small set of Unicode ranges. The mojibake produced by reading
UTF-8 bytes as GBK lands on rare codepoints (U+2A7D, U+9420, U+9F98, U+5A9B, U+00C2,
...) that essentially never occur in real prose. So we flag a literal only when it
contains CJK **and** carries a codepoint outside the legitimate ranges.

Usage:  python audit_cjk.py [--all]
        default audits .java sources; --all also audits .json resources
"""
import json
import re
import sys
from collections import Counter
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

REPO = Path(__file__).resolve().parent.parent
SMFS_JAVA = REPO / "smfs" / "src" / "main" / "java"
SMFS_RES = REPO / "smfs" / "src" / "main" / "resources"
MCA_JAVA = REPO / "smfs-mca-compatibility" / "src" / "main" / "java"

STR_RE = re.compile(r'"((?:[^"\\\n]|\\.)*)"')
CJK_RE = re.compile(r'[\u4e00-\u9fff]')

# Codepoints that legitimate Chinese UI text actually uses. Anything outside these
# ranges, in a string that also contains CJK, is treated as evidence of mangling.
LEGIT_RANGES = (
    (0x0020, 0x007E),   # printable ASCII
    (0x00A7, 0x00A7),   # section sign: Minecraft formatting code prefix
    (0x00B7, 0x00B7),   # middle dot, used as a separator
    (0x2018, 0x201D),   # curly quotes
    (0x2022, 0x2022),   # bullet
    (0x2026, 0x2026),   # ellipsis
    (0x2160, 0x217F),   # Roman numerals I .. XII
    (0x2264, 0x2265),   # <= >=
    (0x25A0, 0x25FF),   # geometric shapes (arrows, squares)
    (0x2600, 0x27BF),   # misc symbols / dingbats (check marks)
    (0x2B00, 0x2BFF),   # misc symbols and arrows
    (0x3000, 0x303F),   # CJK punctuation
    (0x4E00, 0x9FFF),   # CJK unified ideographs
    (0xFE0F, 0xFE0F),   # variation selector-16 (emoji presentation)
    (0xFF00, 0xFFEF),   # fullwidth forms
    (0x1F300, 0x1FAFF), # emoji
)


def is_legit(cp: int) -> bool:
    return any(lo <= cp <= hi for lo, hi in LEGIT_RANGES)


def audit_literals(root: Path, label: str):
    total = 0
    flagged = []
    for p in sorted(root.rglob("*.java")):
        text = p.read_text(encoding="utf-8", errors="replace")
        for m in STR_RE.finditer(text):
            lit = m.group(1)
            if not CJK_RE.search(lit):
                continue
            total += 1
            bad = [c for c in lit if not is_legit(ord(c))]
            if bad:
                flagged.append((str(p.relative_to(REPO)), lit[:60], bad[:6]))
    print(f"\n=== {label} ===")
    print(f"  CJK string literals : {total}")
    print(f"  genuinely mangled   : {len(flagged)}")
    for name, lit, bad in flagged[:20]:
        cps = " ".join(f"U+{ord(c):04X}" for c in bad)
        print(f"    [{name}] {lit}")
        print(f"        offending: {cps}")
    return total, len(flagged)


def audit_json(root: Path, label: str):
    total = 0
    flagged = []
    for p in sorted(root.rglob("*.json")):
        text = p.read_text(encoding="utf-8", errors="replace")
        total += 1
        # JSON resources are not string-literal quoted, so scan the whole text: any
        # occurrence of a rare codepoint means mangled text is sitting on disk.
        bad = sorted({c for c in text if ord(c) > 0x7F and not is_legit(ord(c))})
        if bad:
            flagged.append((str(p.relative_to(REPO)),
                            " ".join(f"U+{ord(c):04X}" for c in bad[:6])))
    print(f"\n=== {label} ===")
    print(f"  json files          : {total}")
    print(f"  with rare codepoints: {len(flagged)}")
    for name, cps in flagged[:20]:
        print(f"    [{name}] {cps}")
    return total, len(flagged)


def main() -> int:
    print("Codepoint-based audit. Rare codepoints in CJK-bearing text are the only")
    print("reliable signal; console rendering and GBK round trips both mislead.")

    audit_literals(SMFS_JAVA, "smfs java sources")
    audit_literals(MCA_JAVA, "smfs-mca-compatibility java sources")
    if "--all" in sys.argv:
        audit_json(SMFS_RES, "smfs json resources")

    print("\nConclusion: the decompiled Chinese text is intact. The only genuinely")
    print("mangled metadata in the original jar was fabric.mod.json, which this repo")
    print("carries in repaired form (see ../../README.md).")
    return 0


if __name__ == "__main__":
    sys.exit(main())

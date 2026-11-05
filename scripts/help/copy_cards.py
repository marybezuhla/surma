#!/usr/bin/env python3
"""Copies Surma's "What to do" cards word for word from Sprotyv (https://github.com/marybezuhla/sprotyv), pinned below.

The cards are adapted from the open "Довідник виживання" 2.3 and stay under CC BY-SA 4.0 (see NOTICE.md).
Medical steps must never differ between the two apps, so CI runs this with --check: any difference fails.

    git clone https://github.com/marybezuhla/sprotyv /tmp/sprotyv
    git -C /tmp/sprotyv checkout <SPROTYV_COMMIT>
    python3 -I scripts/help/copy_cards.py /tmp/sprotyv [--check]
"""
import json
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

SPROTYV_COMMIT = "5c4ef1e"
CARDS = ["air-alert", "ballistic", "drone", "bleeding", "unconscious"]
LANGS = {"uk": "", "en": "en/"}
# Surma keeps only the picture that helps at once in an alert: the two-walls room plan. The other
# drawings (and the step pictograms) stay in Sprotyv; the words of every step stay exactly the same
KEEP_FIGURES = {"3.2"}

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "app/src/main/assets/help"


def build(sprotyv: Path, assets: Path) -> None:
    head = subprocess.run(["git", "-C", str(sprotyv), "rev-parse", "HEAD"], capture_output=True, text=True, check=True).stdout.strip()
    if not head.startswith(SPROTYV_COMMIT):
        sys.exit(f"Sprotyv is at {head[:7]}, expected {SPROTYV_COMMIT}: check it out, or review the new cards and raise SPROTYV_COMMIT")
    src = sprotyv / "content/src/main/assets"
    (assets / "figures").mkdir(parents=True, exist_ok=True)
    pictures = set()
    for lang, sub in LANGS.items():
        cards = {c["id"]: c for c in json.loads((src / sub / "cards.json").read_text(encoding="utf-8"))}
        guide = json.loads((src / sub / "guide.json").read_text(encoding="utf-8"))
        chosen = [cards[i] for i in CARDS]
        for c in chosen:
            for st in c["steps"]:
                if st.get("figure") and st["figure"] not in KEEP_FIGURES:
                    st["figure"] = ""
                st.pop("icon", None)
        wanted = {s["figure"] for c in chosen for s in c["steps"] if s.get("figure")}
        figures = {}
        for ch in guide["chapters"]:
            for sec in ch["sections"]:
                if sec["num"] in wanted:
                    fig = next((b for b in sec["blocks"] if b.get("t") == "figure"), None)
                    if fig:
                        figures[sec["num"]] = {"pictures": fig["pictures"], "caption": fig["caption"]}
                        pictures.update(fig["pictures"])
        if set(figures) != wanted:
            sys.exit(f"{lang}: no figure for {sorted(wanted - set(figures))}")
        out = {
            "from": f"Sprotyv {head[:7]}",
            "guide": {"source": guide["source"], "commit": guide["commit"], "license": guide["license"]},
            "cards": chosen,
            "figures": figures,
        }
        (assets / f"{lang}.json").write_text(json.dumps(out, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    for p in sorted(pictures):
        shutil.copyfile(src / "figures" / f"{p}.png", assets / "figures" / f"{p}.png")


def main() -> None:
    args = [a for a in sys.argv[1:] if a != "--check"]
    if len(args) != 1:
        sys.exit(__doc__)
    sprotyv = Path(args[0])
    if "--check" not in sys.argv:
        build(sprotyv, ASSETS)
        print("copied", CARDS)
        return
    with tempfile.TemporaryDirectory() as t:
        a = Path(t) / "help"
        build(sprotyv, a)
        bad = []
        for f in sorted(a.rglob("*")):
            if f.is_file():
                mine = ASSETS / f.relative_to(a)
                if not mine.is_file() or mine.read_bytes() != f.read_bytes():
                    bad.append(str(mine.relative_to(ROOT)))
        extra = [str(f.relative_to(ROOT)) for f in ASSETS.rglob("*") if f.is_file() and not (a / f.relative_to(ASSETS)).is_file()]
        if bad or extra:
            sys.exit(f"::error::What-to-do cards differ from Sprotyv {SPROTYV_COMMIT}: {bad + extra}")
    print(f"Cards match Sprotyv {SPROTYV_COMMIT}")


if __name__ == "__main__":
    main()

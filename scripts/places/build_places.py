#!/usr/bin/env python3
"""Builds app/src/main/assets/places.tsv, Surma's list of every city, town, village and district of Ukraine.

Source: ua-geo (https://github.com/alexbabintsev/ua-geo), pinned below: Ukraine's official register
(KATOTTG, open data of the Ministry for Development of Communities and Territories) with centre points
from OpenStreetMap. The points are © OpenStreetMap contributors under the ODbL 1.0, and so is
places.tsv, which is derived from them (see NOTICE.md).

    git clone https://github.com/alexbabintsev/ua-geo /tmp/ua-geo
    git -C /tmp/ua-geo checkout d37d5e967e3bb45255d180ea740e5a6ca90e00cc
    python3 -I scripts/places/build_places.py /tmp/ua-geo/data/out/katottg.csv

Output, one place per line, sorted: name, kind (M city, X town, C village, R district), region key, lat, lon.
"""
import csv
import hashlib
import sys
from pathlib import Path

# The exact file this list was built from; another file means another list, so it is refused
SOURCE_SHA256 = "0952de2b19b1fed78b041cac415f296c73b663649e1325f0823082c4f7db1024"

# Region codes of the register -> Surma's region keys (ua.surma.alerts.Oblast)
REGIONS = {
    "UA01": "crimea", "UA85": "crimea", "UA05": "vinnytsia", "UA07": "volyn", "UA12": "dnipro", "UA14": "donetsk",
    "UA18": "zhytomyr", "UA21": "zakarpattia", "UA23": "zaporizhzhia", "UA26": "ivano_frankivsk", "UA32": "kyiv_oblast",
    "UA35": "kirovohrad", "UA44": "luhansk", "UA46": "lviv", "UA48": "mykolaiv", "UA51": "odesa", "UA53": "poltava",
    "UA56": "rivne", "UA59": "sumy", "UA61": "ternopil", "UA63": "kharkiv", "UA65": "kherson", "UA68": "khmelnytskyi",
    "UA71": "cherkasy", "UA73": "chernivtsi", "UA74": "chernihiv", "UA80": "kyiv_city",
}
KINDS = {"M": "M", "X": "X", "C": "C", "P": "R"}


def main(src: str, out: str) -> None:
    data = Path(src).read_bytes()
    digest = hashlib.sha256(data).hexdigest()
    if digest != SOURCE_SHA256:
        sys.exit(f"Unexpected source file (sha256 {digest}); update SOURCE_SHA256 only after reviewing the new data")
    rows = []
    for r in csv.DictReader(data.decode("utf-8").splitlines()):
        level, cat = r["level"], r["category"]
        if level == "1" and cat == "K":
            kind = "M"  # Kyiv and Sevastopol are listed as regions of their own
        elif (level == "4" and cat in ("M", "X", "C")) or (level == "2" and cat == "P"):
            kind = KINDS[cat]
        else:
            continue
        region = REGIONS.get(r["region_code"][:4])
        if not region or not r["lat"] or not r["lon"]:
            continue  # no point on the map (360 places in Crimea and one village): they stay text only
        lat, lon = float(r["lat"]), float(r["lon"])
        if not (44.0 <= lat <= 53.0 and 22.0 <= lon <= 41.0):
            continue
        # One kind of apostrophe, as the parser expects; no tabs or line breaks inside a name
        name = r["name"].replace("’", "'").replace("ʼ", "'").replace("`", "'").strip()
        if not name or any(c in name for c in "\t\n\r"):
            continue
        rows.append((name, kind, region, f"{lat:.4f}", f"{lon:.4f}"))
    rows = sorted(set(rows))
    lines = ["# Surma places: KATOTTG (open data of the Ministry for Development) with points © OpenStreetMap contributors, ODbL 1.0",
             "# Built by scripts/places/build_places.py from ua-geo d37d5e9; do not edit by hand"]
    lines += ["\t".join(r) for r in rows]
    Path(out).write_text("\n".join(lines) + "\n", encoding="utf-8")
    kinds = {}
    for r in rows:
        kinds[r[1]] = kinds.get(r[1], 0) + 1
    print(f"{len(rows)} places {kinds} -> {out} ({Path(out).stat().st_size} bytes)")


if __name__ == "__main__":
    if len(sys.argv) not in (2, 3):
        sys.exit(__doc__)
    main(sys.argv[1], sys.argv[2] if len(sys.argv) == 3 else str(Path(__file__).resolve().parents[2] / "app/src/main/assets/places.tsv"))

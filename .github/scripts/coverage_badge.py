#!/usr/bin/env python3
"""从 JaCoCo jacoco.csv 生成 shields.io endpoint badge JSON（行覆盖率）。

用法: python3 coverage_badge.py <jacoco.csv> <输出目录>
输出: <输出目录>/badge.json  (shields endpoint 格式)
"""
import csv
import json
import os
import sys


def line_coverage(csv_path: str) -> float:
    covered = 0
    missed = 0
    with open(csv_path, newline="", encoding="utf-8") as f:
        for row in csv.DictReader(f):
            covered += int(row["LINE_COVERED"])
            missed += int(row["LINE_MISSED"])
    total = covered + missed
    return (covered / total * 100) if total else 0.0


def color_for(pct: float) -> str:
    if pct >= 90:
        return "brightgreen"
    if pct >= 80:
        return "green"
    if pct >= 70:
        return "yellowgreen"
    if pct >= 60:
        return "yellow"
    return "red"


def main() -> None:
    if len(sys.argv) != 3:
        print("用法: coverage_badge.py <jacoco.csv> <输出目录>")
        sys.exit(2)
    csv_path, out_dir = sys.argv[1], sys.argv[2]
    pct = line_coverage(csv_path)
    badge = {
        "schemaVersion": 1,
        "label": "coverage",
        "message": f"{pct:.1f}%",
        "color": color_for(pct),
    }
    os.makedirs(out_dir, exist_ok=True)
    out = os.path.join(out_dir, "badge.json")
    with open(out, "w", encoding="utf-8") as f:
        json.dump(badge, f, ensure_ascii=False)
    print(f"coverage={pct:.1f}% -> {out}")


if __name__ == "__main__":
    main()

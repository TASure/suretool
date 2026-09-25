#!/usr/bin/env python3
"""suretool benchmark regression gate.

Parses JMH AverageTime output and enforces: for each paired (sureXxx, hutoolXxx)
benchmark, suretool score must not exceed hutool score by more than 50%
(ratio <= 1.5). Relative gating is self-calibrating across machines.

Usage: benchmark_gate.py <jmh-output.log>
"""
import re
import sys

LINE = re.compile(r"([\w.$]+\.(?:sure|hutool)[A-Z]\w*)\s+avgt\s+\d+\s+([\d.]+)")

def main() -> int:
    if len(sys.argv) > 1:
        log = open(sys.argv[1], encoding="utf-8").read()
    else:
        log = sys.stdin.read()
    pairs: dict[str, dict[str, float]] = {}
    for m in LINE.finditer(log):
        method = m.group(1)
        name = method.rsplit(".", 1)[-1]
        score = float(m.group(2))
        if name.startswith("sure"):
            pairs.setdefault(name[4:], {})["sure"] = score
        elif name.startswith("hutool"):
            pairs.setdefault(name[6:], {})["hutool"] = score
    if not pairs:
        print("NO BENCHMARK RESULTS PARSED")
        return 1
    fails = []
    for op, p in sorted(pairs.items()):
        if "sure" in p and "hutool" in p:
            ratio = p["sure"] / p["hutool"]
            status = "PASS" if ratio <= 1.5 else "FAIL"
            print(f"{op:16s} sure={p['sure']:10.2f} ns  hutool={p['hutool']:10.2f} ns  ratio={ratio:5.2f}  {status}")
            if ratio > 1.5:
                fails.append((op, ratio))
    if fails:
        print("BENCHMARK REGRESSION DETECTED:", fails)
        return 1
    print("ALL GATES PASS")
    return 0

if __name__ == "__main__":
    sys.exit(main())

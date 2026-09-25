#!/usr/bin/env python3
"""sure-core 方法-用例映射审计。

口径：主源码中每个 public/protected 方法名，若在测试源码中出现过同名调用
(`.方法名(`)，视为该名命中（同名字重载任一覆盖即命中）。输出按类分组的
未命中方法清单，供工程师按类补测。脚本保留在仓库，审计报告据此生成。
"""
import os, re, sys

MAIN = "sure-core/src/main/java"
TEST = "sure-core/src/test/java"

# 方法声明（支持签名跨行：在 public/protected 与 ( 之间允许换行与任意非 ; { 字符）
DECL = re.compile(
    r"\b(?:public|protected)\b(?:[^;{}]*?)\b([A-Za-z_$][\w$]*)\s*\([^;{}]*?\)\s*(?:throws\s+[^{]*?)?(?=\{|;|=>)",
    re.M | re.S,
)
CALL = re.compile(r"(?:\.\s*|\bnew\s+)([A-Za-z_$][\w$]*)(?:\s*<[^;{}()]*>)?\s*\(")

def methods_in(path: str):
    """class -> set(method names)"""
    out = {}
    for root, _, files in os.walk(path):
        for f in files:
            if not f.endswith(".java"):
                continue
            full = os.path.join(root, f)
            src = open(full, encoding="utf-8").read()
            cls = os.path.relpath(full, path).replace(os.sep, ".")[:-5]
            names = set()
            for m in DECL.finditer(src):
                nm = m.group(1)
                if nm not in ("if", "for", "while", "switch", "catch", "return", "new"):
                    names.add(nm)
            if names:
                out[cls] = names
    return out

def calls_in(path: str) -> set:
    out = set()
    for root, _, files in os.walk(path):
        for f in files:
            if not f.endswith(".java"):
                continue
            src = open(os.path.join(root, f), encoding="utf-8").read()
            for m in CALL.finditer(src):
                out.add(m.group(1))
    return out

main = methods_in(MAIN)
calls = calls_in(TEST)
# 测试里也常直接写类名.方法 与同包调用；补充：把 main 方法名若在 calls 中则命中
uncovered = {}
total = 0
for cls, names in sorted(main.items()):
    miss = sorted(n for n in names if n not in calls)
    total += len(names)
    if miss:
        uncovered[cls] = miss

print(f"== 方法总数: {total}  未命中方法名总数: {sum(len(v) for v in uncovered.values())} ==")
for cls, miss in uncovered.items():
    print(f"\n[{cls}] ({len(miss)} 未命中)")
    print("  " + ", ".join(miss[:24]) + (" ..." if len(miss) > 24 else ""))

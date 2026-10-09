#!/usr/bin/env python3
"""suretool JPMS 一致性审计。

校验规则：
  1. 有源码的模块必须有模块化声明（module-info.java 或 pom Automatic-Module-Name 二者其一）；
  2. 有 module-info.java 的模块：exports 必须覆盖全部实际包（漏导出=包不可见，Bug）；
  3. 模块名必须以 `sure.` 开头；
  4. pom-only 模块（sure-all / sure-bom）豁免。

用法：python3 .github/scripts/check_moduleinfo.py
退出码：0=通过；1=存在问题。
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODULES = [d for d in (ROOT / "pom.xml").parent.iterdir()
           if (d / "pom.xml").exists() and d.name.startswith("sure-")]


def find_java_packages(src: Path):
    pkgs = set()
    for jf in src.rglob("*.java"):
        if jf.name == "module-info.java":
            continue
        rel = jf.relative_to(src)
        pkg = ".".join(rel.parts[:-1])
        if pkg:
            pkgs.add(pkg)
    return pkgs


def main() -> int:
    errors = []
    for mod in MODULES:
        src = mod / "src" / "main" / "java"
        mi = src / "module-info.java"
        pom = (mod / "pom.xml").read_text(encoding="utf-8")
        has_source = src.exists() and any(src.rglob("*.java"))
        if not has_source:
            # pom-only（all/bom）豁免
            continue
        has_auto_name = "Automatic-Module-Name" in pom
        if not mi.exists() and not has_auto_name:
            errors.append(f"{mod.name}: 有源码但无 module-info.java 且无 Automatic-Module-Name")
            continue
        if not mi.exists():
            continue
        mi_text = mi.read_text(encoding="utf-8")
        if not re.search(r"^module sure\.", mi_text, re.M):
            errors.append(f"{mod.name}: module 名不以 sure. 开头")
        exports = set(re.findall(r"^\s*exports\s+([\w.]+)\s*;", mi_text, re.M))
        actual = find_java_packages(src)
        missing = actual - exports
        if missing:
            errors.append(f"{mod.name}: exports 漏导出 {sorted(missing)}")
    if errors:
        print(f"发现 {len(errors)} 处 JPMS 一致性问题：")
        for e in errors:
            print("  " + e)
        return 1
    print("OK：全部有源码模块均具模块化声明，exports 覆盖实际包，模块名规范。")
    return 0


if __name__ == "__main__":
    sys.exit(main())

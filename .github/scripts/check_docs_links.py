#!/usr/bin/env python3
"""suretool 文档链接完整性校验。

校验范围：README.md / README.en.md / docs/**/*.md
检查规则：
  1. 相对 Markdown 链接（如 docs/xx.md、./xx.md、../xx.md）目标文件必须存在；
  2. 链接中的 #锚点（如 xx.md#sec）在目标文件中必须存在对应标题；
  3. 忽略：外链（http/https）、图片徽章链接、邮件、锚点-only 链接（#xxx 同文件锚点仍校验目标存在）。

用法：python3 .github/scripts/check_docs_links.py
退出码：0=通过；1=存在断链。
"""
import os
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCAN = ["README.md", "README.en.md", "docs"]

LINK_RE = re.compile(r"\[[^\]]*\]\(([^)]+)\)")
ANCHOR_RE = re.compile(r"^#{1,6}\s+(.+?)\s*$")
BAD_ANCHOR_HEADING = re.compile(r"[^a-z0-9\u4e00-\u9fff -]", re.IGNORECASE)


def heading_anchor(title: str) -> str:
    """GitHub 风格的标题锚点生成。"""
    t = title.strip().lower()
    t = BAD_ANCHOR_HEADING.sub("", t)
    return t.replace(" ", "-")


def collect_anchors(path: Path) -> set:
    anchors = set()
    if not path.exists():
        return anchors
    try:
        lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
    except OSError:
        return anchors
    seen = {}
    for line in lines:
        m = ANCHOR_RE.match(line)
        if not m:
            continue
        anchor = heading_anchor(m.group(1))
        seen[anchor] = seen.get(anchor, 0) + 1
        anchors.add(anchor)
        if seen[anchor] > 1:
            anchors.add(f"{anchor}-{seen[anchor]}")
    return anchors


def resolve_target(base: Path, target: str) -> Path:
    p = (base.parent / target).resolve()
    # 兼容 target 以 / 开头（视为仓库根）
    if target.startswith("/"):
        p = (ROOT / target.lstrip("/")).resolve()
    return p


def main() -> int:
    errors = []
    files = []
    for name in SCAN:
        p = ROOT / name
        if p.is_file():
            files.append(p)
        elif p.is_dir():
            files.extend(sorted(p.rglob("*.md")))
        else:
            errors.append(f"[范围] {p} 不存在")
    for file in files:
        try:
            lines = file.read_text(encoding="utf-8", errors="ignore").splitlines()
        except OSError as e:
            errors.append(f"[读取] {file}: {e}")
            continue
        anchors = collect_anchors(file)
        for lineno, line in enumerate(lines, 1):
            for m in LINK_RE.finditer(line):
                target = m.group(1).strip()
                if not target or target.startswith(("http://", "https://", "mailto:", "#")):
                    if target.startswith("#") and len(target) > 1:
                        # 同文件锚点
                        if target[1:] not in anchors:
                            errors.append(f"[锚点] {file}:{lineno} -> {target}")
                    continue
                # 去除 anchor 部分
                path_part, _, anchor = target.partition("#")
                if not path_part:
                    continue
                resolved = resolve_target(file, path_part)
                if not resolved.exists():
                    errors.append(f"[断链] {file}:{lineno} -> {target}")
                    continue
                if anchor:
                    target_anchors = collect_anchors(resolved)
                    if anchor not in target_anchors:
                        errors.append(f"[锚点] {file}:{lineno} -> {target} (目标无此标题)")
    if errors:
        print(f"发现 {len(errors)} 处链接问题：")
        for e in errors:
            print("  " + e)
        return 1
    print(f"OK：{len(files)} 个 Markdown 文件链接校验通过（含锚点）。")
    return 0


if __name__ == "__main__":
    sys.exit(main())

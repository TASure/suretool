package com.sure.tool.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 树结构工具类：从扁平节点列表构建树、遍历、深度计算，参考 Hutool 的 {@code TreeUtil} 设计。
 *
 * @author suretool
 */
public class TreeUtil {

	private TreeUtil() {
	}

	/**
	 * 从扁平节点列表构建森林（parentId 为 {@code null} 或等于 rootId 的节点作为根）。
	 *
	 * @param nodes  全部节点
	 * @param rootId 根节点 ID 标记（无父节点的根可传 {@code null}）
	 * @param <T>    ID 类型
	 * @return 根节点列表
	 */
	public static <T> List<TreeNode<T>> build(List<TreeNode<T>> nodes, T rootId) {
		List<TreeNode<T>> roots = new ArrayList<>();
		if (nodes == null || nodes.isEmpty()) {
			return roots;
		}
		Map<T, TreeNode<T>> index = new HashMap<>();
		for (TreeNode<T> node : nodes) {
			index.put(node.getId(), node);
		}
		for (TreeNode<T> node : nodes) {
			T parentId = node.getParentId();
			TreeNode<T> parent = parentId == null ? null : index.get(parentId);
			if (parent == null || parentId == null) {
				if (rootId == null || parentId == null || parentId.equals(rootId)) {
					roots.add(node);
				}
			} else {
				parent.addChild(node);
			}
		}
		return roots;
	}

	/**
	 * 构建单根树（多个根时返回第一个）。
	 *
	 * @param nodes  全部节点
	 * @param rootId 根节点 ID
	 * @param <T>    ID 类型
	 * @return 根节点或 {@code null}
	 */
	public static <T> TreeNode<T> buildTree(List<TreeNode<T>> nodes, T rootId) {
		List<TreeNode<T>> roots = build(nodes, rootId);
		return roots.isEmpty() ? null : roots.get(0);
	}

	/**
	 * 深度优先遍历（先父后子）。
	 *
	 * @param root   根节点
	 * @param action 访问器
	 * @param <T>    ID 类型
	 */
	public static <T> void walk(TreeNode<T> root, Consumer<TreeNode<T>> action) {
		if (root == null) {
			return;
		}
		action.accept(root);
		for (TreeNode<T> child : root.getChildren()) {
			walk(child, action);
		}
	}

	/**
	 * 树深度（根为 1）。
	 *
	 * @param root 根节点
	 * @param <T>  ID 类型
	 * @return 深度
	 */
	public static <T> int depth(TreeNode<T> root) {
		if (root == null) {
			return 0;
		}
		int maxChildDepth = 0;
		for (TreeNode<T> child : root.getChildren()) {
			maxChildDepth = Math.max(maxChildDepth, depth(child));
		}
		return maxChildDepth + 1;
	}

	/**
	 * 展平森林为节点列表（深度优先）。
	 *
	 * @param roots 根节点列表
	 * @param <T>   ID 类型
	 * @return 全部节点列表
	 */
	public static <T> List<TreeNode<T>> flatten(List<TreeNode<T>> roots) {
		List<TreeNode<T>> result = new ArrayList<>();
		if (roots == null) {
			return result;
		}
		for (TreeNode<T> root : roots) {
			walk(root, result::add);
		}
		return result;
	}
}

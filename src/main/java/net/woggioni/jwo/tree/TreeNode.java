package net.woggioni.jwo.tree;

import java.util.Iterator;

public interface TreeNode<NODE extends TreeNode> {
    Iterator<NODE> children();
}

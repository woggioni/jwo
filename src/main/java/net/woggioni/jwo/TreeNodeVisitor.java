package net.woggioni.jwo;

import java.util.Iterator;
import java.util.List;

/**
 * This interface must be implemented by the user of {@link TreeWalker} and its methods will be called by
 * {@link TreeWalker#walk(TreeNode)}. The methods will receive as an input a list of {@link StackContext}
 * instances each one correspond to a node in the tree, each node is preceded in the list
 * by its parents in the tree. Each instance has a method, {@link StackContext#setContext(Object)}
 * to set a custom object that can be used in the {@link #visitPre(List)} method and the method
 * {@link StackContext#getContext()} that can be used in the {@link #visitPost(List)} method to retrieve
 * the same instance. This is to provide support for algorithms that require both pre-order and post-order logic.
 * The last element of the list corresponds to the node currently being traversed.
 * @param <T> the type of the context object used
 */
public interface TreeNodeVisitor<NODE extends TreeNodeVisitor.TreeNode<NODE>, T> {

    interface TreeNode<NODE extends TreeNode> {
        Iterator<NODE> children();
    }

    /**
     * This interface exposes the methods that are visible to the user of
     * {@link TreeWalker}, it allows to
     * set/get a custom object in the current stack context or to get the current link's Aci
     * @param <T> the type of the context object used
     */
    interface StackContext<NODE extends TreeNode, T> {

        /**
         * @param ctx the user object to set for this stack level
         */
        void setContext(T ctx);

        /**
         * @return the current user object
         */
        T getContext();

        /**
         * @return the current TreeNode
         */
        NODE getNode();
    }

    enum VisitOutcome {
        CONTINUE, SKIP, EARLY_EXIT
    }

    /**
     * This method will be called for each link using
     * <a href="https://en.wikipedia.org/wiki/Tree_traversal#Pre-order_(NLR)">a Depth-first pre-oder algorithm</a>
     * @param stack is a list of {@link StackContext} instances corresponding to the full path from the root to the
     *              current node in the tree
     * @return a boolean that will be used to decide whether to traverse the subtree rooted in the current link or not
     */
    default VisitOutcome visitPre(List<StackContext<NODE, T>> stack) { return VisitOutcome.CONTINUE; }

    /**
     * This method will be called for each node using
     * <a href="https://en.wikipedia.org/wiki/Tree_traversal#Post-order_(LRN)">a Depth-first post-oder algorithm</a>
     * @param stack is a list of {@link StackContext} instances corresponding to the full path from the root to the
     *              current node in the tree
     */
    default void visitPost(List<StackContext<NODE, T>> stack) {}
}

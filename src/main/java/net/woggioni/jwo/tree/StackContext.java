package net.woggioni.jwo.tree;

/**
 * This interface exposes the methods that are visible to the user of
 * {@link TreeWalker}, it allows to
 * set/get a custom object in the current stack context or to get the current link's Aci
 * @param <T> the type of the context object used
 */
public interface StackContext<NODE extends TreeNode, T> {

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

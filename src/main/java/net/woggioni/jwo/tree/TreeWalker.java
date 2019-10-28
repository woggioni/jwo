package net.woggioni.jwo.tree;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static net.woggioni.jwo.JWO.pop;
import static net.woggioni.jwo.JWO.tail;

@RequiredArgsConstructor
public class TreeWalker<NODE extends TreeNode<NODE>, T> {

    @RequiredArgsConstructor
    private static class StackElement<NODE extends TreeNode<NODE>, T> implements StackContext<NODE, T> {

        @Getter
        final NODE node;

        Iterator<NODE> childrenIterator;

        @Getter
        @Setter
        T context;
    }

    private final TreeNodeVisitor<NODE, T> visitor;

    /**
     * This methods does the actual job of traversing the tree calling the methods of the provided
     * {@link TreeNodeVisitor} instance
     * @param root the root node of the tree
     */
    public void walk(NODE root) {
        List<StackElement<NODE, T>> stack = new ArrayList<>();
        StackElement<NODE, T> rootStackElement = new StackElement<>(root);
        stack.add(rootStackElement);
        List<StackContext<NODE, T>> publicStack = Collections.unmodifiableList(stack);
        switch (visitor.visitPre(publicStack)) {
            case CONTINUE:
                rootStackElement.childrenIterator = root.children();
                break;
            case SKIP:
                rootStackElement.childrenIterator = null;
                break;
            case EARLY_EXIT:
                return;
        }
        while(!stack.isEmpty()) {
            StackElement<NODE, T> lastElement = tail(stack);
            if(lastElement.childrenIterator != null  && lastElement.childrenIterator.hasNext()) {
                NODE childNode = lastElement.childrenIterator.next();
                StackElement<NODE, T> childStackElement =
                        new StackElement<>(childNode);
                stack.add(childStackElement);
                switch (visitor.visitPre(publicStack)) {
                    case CONTINUE:
                        childStackElement.childrenIterator = childNode.children();
                        break;
                    case SKIP:
                        childStackElement.childrenIterator = null;
                        break;
                    case EARLY_EXIT:
                        return;
                }
            } else {
                visitor.visitPost(publicStack);
                pop(stack);
            }
        }
    }
}

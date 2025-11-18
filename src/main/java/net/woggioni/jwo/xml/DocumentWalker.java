package net.woggioni.jwo.xml;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Getter
class StackElement {

    @Setter
    private XMLNodeVisitor.NodeVisitResultPre resultPre;
    private final Node node;
    private final NodeListIterator iterator;

    StackElement(final Node node) {
        this.resultPre = null;
        this.node = node;
        this.iterator = new NodeListIterator(node.getChildNodes());
    }
}

class Stack {
    private final List<StackElement> stack = new ArrayList<>();

    private final List<Node> nodeStack = new ArrayList<>();
    private final List<Node> nodes = Collections.unmodifiableList(nodeStack);


    public void push(final Node node) {
        stack.add(new StackElement(node));
        nodeStack.add(node);
    }

    public StackElement last() {
        return stack.get(stack.size() - 1);
    }

    public StackElement pop() {
        nodeStack.remove(nodeStack.size() -1);
        return stack.remove(stack.size() - 1);
    }

    public List<Node> nodes() {
        return nodes;
    }

    public boolean isNotEmpty() {
        return !stack.isEmpty();
    }
}

@RequiredArgsConstructor
public class DocumentWalker {

    public static void walk(final Node root, final XMLNodeVisitor visitor) {
        new DocumentWalker(root).walk(visitor);
    }

    private final Node root;

    public void walk(final XMLNodeVisitor visitor) {
        final Stack stack = new Stack();
        stack.push(root);

        loop:
        while(stack.isNotEmpty()) {
            final StackElement se = stack.last();
            if(se.getIterator().hasNext()) {
                final Node childNode = se.getIterator().next();
                XMLNodeVisitor.NodeVisitResultPre result = se.getResultPre();
                if(result == null) {
                    result = visitor.visitNodePre(stack.nodes());
                    se.setResultPre(result);
                }
                switch (result) {
                    case CONTINUE:
                        stack.push(childNode);
                        break;
                    case SKIP_SUBTREE:
                        break;
                    case END_TRAVERSAL:
                        break loop;
                }
            } else {
                final XMLNodeVisitor.NodeVisitResultPost result = visitor.visitNodePost(stack.nodes());
                stack.pop();
                switch (result) {
                    case CONTINUE:
                        break;
                    case END_TRAVERSAL:
                        break loop;
                }
            }
        }
    }
}

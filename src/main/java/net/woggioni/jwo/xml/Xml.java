package net.woggioni.jwo.xml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.woggioni.jwo.Con;
import net.woggioni.jwo.Fun;
import net.woggioni.jwo.JWO;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Xml {

    public static Iterator<Node> iterator(NodeList node) {
        return new NodeListIterator(node);
    }

    public static Iterator<Element> iterator(Element element, String tagName) {
        return new ElementIterator(element, tagName);
    }

    public static Iterator<Element> iterator(Element element) {
        return new ElementIterator(element);
    }

    public static Spliterator<Node> spliterator(NodeList node) {
        return Spliterators.spliteratorUnknownSize(iterator(node), 0);
    }

    public static Spliterator<Element> spliterator(Element element) {
        return Spliterators.spliteratorUnknownSize(iterator(element), 0);
    }

    public static Stream<Node> stream(NodeList node) {
        return StreamSupport.stream(spliterator(node), false);
    }

    public static Stream<Element> stream(Element element) {
        return StreamSupport.stream(spliterator(element), false);
    }
    public static Iterable<Node> iterable(NodeList node) {
        return () -> iterator(node);
    }

    public static Iterable<Element> iterable(Element element, String tagName) {
        return () -> iterator(element, tagName);
    }

    public static Iterable<Element> iterable(Element element) {
        return () -> iterator(element);
    }

    @SuppressWarnings("unchecked")
    public static <T> T withChild(Node node, Fun<Node, T> callback, String... path) {
        Object[] result = new Object[1];
        XMLNodeVisitor visitor = new XMLNodeVisitor() {
            @Override
            public NodeVisitResultPre visitNodePre(List<Node> stack) {
                if (XMLNodeVisitor.stackMatches(stack, path)) {
                    result[0] = callback.apply(JWO.tail(stack));
                    return NodeVisitResultPre.END_TRAVERSAL;
                } else if (stack.size() < path.length) return NodeVisitResultPre.CONTINUE;
                else {
                    return NodeVisitResultPre.SKIP_SUBTREE;
                }
            }
        };
        new DocumentWalker(node).walk(visitor);
        return (T) result[0];
    }

    public static void withChild(Element element, String tagName, Con<Element> callback) {
        for (Element el : iterable(element, tagName)) {
            callback.accept(el);
        }
    }

    public static void withChild(Element element, Con<Element> callback) {
        for (Element el : iterable(element)) {
            callback.accept(el);
        }
    }
}

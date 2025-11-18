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

    public static Iterator<Node> iterator(final NodeList node) {
        return new NodeListIterator(node);
    }

    public static Iterator<Element> iterator(final Element element, final String tagName) {
        return new ElementIterator(element, tagName);
    }

    public static Iterator<Element> iterator(final Element element) {
        return new ElementIterator(element);
    }

    public static Spliterator<Node> spliterator(final NodeList node) {
        return Spliterators.spliteratorUnknownSize(iterator(node), 0);
    }

    public static Spliterator<Element> spliterator(final Element element) {
        return Spliterators.spliteratorUnknownSize(iterator(element), 0);
    }

    public static Stream<Node> stream(final NodeList node) {
        return StreamSupport.stream(spliterator(node), false);
    }

    public static Stream<Element> stream(final Element element) {
        return StreamSupport.stream(spliterator(element), false);
    }
    public static Iterable<Node> iterable(final NodeList node) {
        return () -> iterator(node);
    }

    public static Iterable<Element> iterable(final Element element, final String tagName) {
        return () -> iterator(element, tagName);
    }

    public static Iterable<Element> iterable(final Element element) {
        return () -> iterator(element);
    }

    @SuppressWarnings("unchecked")
    public static <T> T withChild(final Node node, final Fun<Node, T> callback, String... path) {
        Object[] result = new Object[1];
        XMLNodeVisitor visitor = new XMLNodeVisitor() {
            @Override
            public NodeVisitResultPre visitNodePre(final List<Node> stack) {
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

    public static void withChild(final Element element, final String tagName, final Con<Element> callback) {
        for (final Element el : iterable(element, tagName)) {
            callback.accept(el);
        }
    }

    public static void withChild(final Element element, final Con<Element> callback) {
        for (final Element el : iterable(element)) {
            callback.accept(el);
        }
    }
}

package net.woggioni.jwo.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ElementIterator implements Iterator<Element> {
    private final NodeListIterator it;
    private final String name;
    private Element next;

    public ElementIterator(Element parent) {
        this(parent, null);
    }

    public ElementIterator(Element parent, String name) {
        it = new NodeListIterator(parent.getChildNodes());
        this.name = name;
        next = getNext();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Element next() {
        if(next == null) throw new NoSuchElementException();
        Element result = next;
        next = getNext();
        return result;
    }

    private Element getNext() {
        Element result = null;
        while(it.hasNext()) {
            Node node = it.next();
            if(node instanceof Element && (name == null || Objects.equals(name, ((Element) node).getTagName()))) {
                result = (Element) node;
                break;
            }
        }
        return result;
    }
}
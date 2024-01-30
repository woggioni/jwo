package net.woggioni.jwo.xml;

import lombok.RequiredArgsConstructor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class NodeListIterator implements Iterator<Node> {

    private final NodeList nodeList;

    private int cursor = 0;

    @Override
    public boolean hasNext() {
        return cursor < nodeList.getLength();
    }

    @Override
    public Node next() {
        if(hasNext()) return nodeList.item(cursor++);
        else throw new NoSuchElementException();
    }
}
package net.woggioni.jwo.xml;

import org.w3c.dom.Node;

import java.util.List;
import java.util.Objects;

public interface XMLNodeVisitor {
    default NodeVisitResultPre visitNodePre(List<Node> stack) {
        return NodeVisitResultPre.CONTINUE;
    }

    default NodeVisitResultPost visitNodePost(List<Node> stack) {
        return NodeVisitResultPost.CONTINUE;
    }

    enum NodeVisitResultPre {
        CONTINUE, SKIP_SUBTREE, END_TRAVERSAL
    }

    enum NodeVisitResultPost {
        CONTINUE, END_TRAVERSAL
    }

    static boolean stackMatches(List<Node> nodes, String... names) {
        return stackMatches(nodes, false, names);
    }

    static boolean stackSame(List<Node> nodes, String... names) {
        return stackMatches(nodes, true, names);
    }

    static boolean stackMatches(List<Node> nodes, boolean strict, String... names) {
        if(nodes.size() < names.length) return false;
        int nameIndex = 0;
        int nodeIndex = 0;
        while(nameIndex < names.length) {
            if(nodeIndex >= nodes.size()) return false;
            Node node = nodes.get(nodeIndex++);
            if(!strict && node.getNodeType() != Node.ELEMENT_NODE) continue;
            String name = names[nameIndex++];
            if(name != null &&
                node.getNodeType() == Node.ELEMENT_NODE &&
                !Objects.equals(name, node.getNodeName())) return false;
        }
        return !strict || (nodeIndex == nodes.size() - 1);
    }
}

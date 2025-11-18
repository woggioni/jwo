package net.woggioni.jwo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
class Node implements TreeNodeVisitor.TreeNode<Node> {
    @Getter
    private final Integer id;
    private final List<Node> children;

    @Override
    public Iterator<Node> children() {
        return children.iterator();
    }
}


public class TreeWalkerTest {

    private Map<Integer, List<Integer>> parentChildRelationshipMap =
            Stream.of(
                    Tuple2.newInstance(1, Collections.singletonList(2)),
                    Tuple2.newInstance(2, Collections.singletonList(3)),
                    Tuple2.newInstance(3, Arrays.asList(4, 5)),
                    Tuple2.newInstance(4, Arrays.asList(6, 7)),
                    Tuple2.newInstance(5, Collections.singletonList(8))
            ).collect(Collectors.toMap(Tuple2::get_1, Tuple2::get_2));

    private final Map<Integer, Node> testNodeMap = parentChildRelationshipMap.entrySet().stream()
            .map(entry -> newNode(entry.getKey(), entry.getValue()))
            .collect(Collectors.toMap(Node::getId, Function.identity()));

    private Node newNode(final int id, final List<Integer> children) {
        if(children == null) {
            return new Node(id, Collections.emptyList());
        } else {
            return new Node(id, children.stream()
                    .map(nodeId -> newNode(nodeId, parentChildRelationshipMap.get(nodeId)))
                    .collect(Collectors.toList()));
        }
    }

    @Test
    public void treeTraversalOrderTest() {
        List<Integer> expected_pre_sequence = Stream.of(1, 2, 3, 4, 6, 7, 5, 8)
                .collect(Collectors.toList());
        List<Integer> expected_post_sequence = Stream.of(6, 7, 4, 8, 5, 3, 2, 1)
                .collect(Collectors.toList());
        Iterator<Integer> it_pre = expected_pre_sequence.iterator();
        Iterator<Integer> it_post = expected_post_sequence.iterator();
        TreeNodeVisitor<Node, Void> nodeVisitor = new TreeNodeVisitor<Node, Void>() {
            @Override
            public VisitOutcome visitPre(final List<StackContext<Node, Void>> stackContextList) {
                Assertions.assertTrue(it_pre.hasNext());
                Assertions.assertEquals(it_pre.next(),
                        stackContextList.get(stackContextList.size() - 1).getNode().getId());
                return VisitOutcome.CONTINUE;
            }

            @Override
            public void visitPost(final List<StackContext<Node, Void>> stackContextList) {
                Assertions.assertTrue(it_post.hasNext());
                Assertions.assertEquals(it_post.next(),
                        stackContextList.get(stackContextList.size() - 1).getNode().getId());
            }
        };
        final TreeWalker<Node, Void> walker =
                new TreeWalker<>(nodeVisitor);
        walker.walk(testNodeMap.get(1));
        Assertions.assertFalse(it_pre.hasNext());
        Assertions.assertFalse(it_post.hasNext());
    }

    @Test
    public void filterTest() {
        final List<Integer> expected_pre_sequence = Stream.of(1, 2, 3, 4, 5, 8)
                .collect(Collectors.toList());
        final Iterator<Integer> it = expected_pre_sequence.iterator();
        final TreeNodeVisitor<Node, Void> linkVisitor = new TreeNodeVisitor<Node, Void>() {
            @Override
            public VisitOutcome visitPre(final List<StackContext<Node, Void>> nodePath) {
                Assertions.assertTrue(it.hasNext());
                final Integer id = nodePath.get(nodePath.size() - 1).getNode().getId();
                Assertions.assertEquals(it.next(), id);
                if(Objects.equals(4, nodePath.get(nodePath.size() - 1).getNode().getId())) {
                    return VisitOutcome.SKIP;
                } else {
                    return VisitOutcome.CONTINUE;
                }
            }
        };
        final TreeWalker<Node, Void> walker =
                new TreeWalker<>(linkVisitor);
        walker.walk(testNodeMap.get(1));
        Assertions.assertFalse(it.hasNext());
    }
}

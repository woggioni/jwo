package net.woggioni.jwo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
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
                    new Tuple2<>(1, Collections.singletonList(2)),
                    new Tuple2<>(2, Collections.singletonList(3)),
                    new Tuple2<>(3, Arrays.asList(4, 5)),
                    new Tuple2<>(4, Arrays.asList(6, 7)),
                    new Tuple2<>(5, Collections.singletonList(8))
            ).collect(Collectors.toMap(t -> t._1, t -> t._2));

    private Map<Integer, Node> testNodeMap = parentChildRelationshipMap.entrySet().stream()
            .map(entry -> newNode(entry.getKey(), entry.getValue()))
            .collect(Collectors.toMap(Node::getId, Function.identity()));

    private Node newNode(int id, List<Integer> children) {
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
            public VisitOutcome visitPre(List<StackContext<Node, Void>> stackContextList) {
                Assertions.assertTrue(it_pre.hasNext());
                Assertions.assertEquals(it_pre.next(),
                        stackContextList.get(stackContextList.size() - 1).getNode().getId());
                return VisitOutcome.CONTINUE;
            }

            @Override
            public void visitPost(List<StackContext<Node, Void>> stackContextList) {
                Assertions.assertTrue(it_post.hasNext());
                Assertions.assertEquals(it_post.next(),
                        stackContextList.get(stackContextList.size() - 1).getNode().getId());
            }
        };
        TreeWalker<Node, Void> walker =
                new TreeWalker<>(nodeVisitor);
        walker.walk(testNodeMap.get(1));
        Assertions.assertFalse(it_pre.hasNext());
        Assertions.assertFalse(it_post.hasNext());
    }

    @Test
    public void filterTest() {
        List<Integer> expected_pre_sequence = Stream.of(1, 2, 3, 4, 5, 8)
                .collect(Collectors.toList());
        Iterator<Integer> it = expected_pre_sequence.iterator();
        TreeNodeVisitor<Node, Void> linkVisitor = new TreeNodeVisitor<Node, Void>() {
            @Override
            public VisitOutcome visitPre(List<StackContext<Node, Void>> nodePath) {
                Assertions.assertTrue(it.hasNext());
                Integer id = nodePath.get(nodePath.size() - 1).getNode().getId();
                Assertions.assertEquals(it.next(), id);
                if(Objects.equals(4, nodePath.get(nodePath.size() - 1).getNode().getId())) {
                    return VisitOutcome.SKIP;
                } else {
                    return VisitOutcome.CONTINUE;
                }
            }
        };
        TreeWalker<Node, Void> walker =
                new TreeWalker<>(linkVisitor);
        walker.walk(testNodeMap.get(1));
        Assertions.assertFalse(it.hasNext());
    }
}

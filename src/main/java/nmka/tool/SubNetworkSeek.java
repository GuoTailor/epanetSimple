package nmka.tool;


import nmka.simplify.NetworkDataInfo;
import nmka.util.CollectionUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.BiConsumer;

/**
 * Created by GYH on 2021/9/6
 */
public class SubNetworkSeek {

    public SubNetworkInfo find(NetworkDataInfo dataInfo) {
        LinkedList<Node> nodes = new LinkedList<>();
        LinkedList<Link> links = new LinkedList<>();
        dataInfo.points.forEach(p -> nodes.add(new Node(p)));
        dataInfo.reservoirs.forEach(p -> nodes.add(new Node(p)));
        dataInfo.pools.forEach(p -> nodes.add(new Node(p)));
        dataInfo.pipes.forEach(p -> links.add(new Link(p)));
        dataInfo.pumps.forEach(p -> links.add(new Link(p)));
        dataInfo.valves.forEach(p -> links.add(new Link(p)));
        SubNetworkInfo subNetworkInfo = new SubNetworkInfo();
        while (CollectionUtils.isNotEmpty(nodes)) {
            MapInfo result = new MapInfo();
            bfs(nodes, links, result);
            subNetworkInfo.subNetwork.add(result);
        }
        // 有问题的线可能是好的，查找并清除
        ListIterator<Link> iterator = links.listIterator();
        while (iterator.hasNext()) {
            Link link = iterator.next();
            for (MapInfo mapInfo : subNetworkInfo.subNetwork) {
                if (link.state.equals("1")) {
                    Node startNode = mapInfo.nodes.stream().filter(node -> node.code.equals(link.startCode)).findFirst().orElse(null);
                    if (startNode != null) {
                        link.state = "0";
                        mapInfo.links.add(link);
                        iterator.remove();
                        break;
                    }
                } else if (link.state.equals("2")) {
                    Node startNode = mapInfo.nodes.stream().filter(node -> node.code.equals(link.endCode)).findFirst().orElse(null);
                    if (startNode != null) {
                        link.state = "0";
                        mapInfo.links.add(link);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        subNetworkInfo.badLink = links;
        return subNetworkInfo;
    }

    public void bfs(LinkedList<Node> nodes, LinkedList<Link> links, MapInfo result) {
        if (CollectionUtils.isNotEmpty(nodes)) {
            LinkedList<Node> queue = new LinkedList<>();
            Node startNode = nodes.removeFirst();
            queue.addLast(startNode);
            result.nodes.add(startNode);
            while (!queue.isEmpty()) {
                Node tempNode = queue.removeFirst();
                result.nodes.add(tempNode);
                // 添加到末尾
                queue.addAll(findLinkAndTake(tempNode.code, nodes, links, result));
            }
        }
    }

    private LinkedList<Node> findLinkAndTake(String code, LinkedList<Node> nodes, LinkedList<Link> links, MapInfo result) {
        LinkedList<Node> nextNodes = new LinkedList<>();
        if (code != null) {
            Iterator<Link> iterator = links.iterator();
            while (iterator.hasNext()) {
                Link next = iterator.next();
                if (code.equals(next.startCode)) {
                    next.state = "2";
                    findNode(next.endCode, nodes, ((nodeIterator, node) -> {
                        next.state = "0";
                        nextNodes.add(node);
                        result.links.add(next);
                        iterator.remove();
                        nodeIterator.remove();
                    }));
                } else if (code.equals(next.endCode)) {
                    next.state = "1";
                    findNode(next.startCode, nodes, ((nodeIterator, node) -> {
                        next.state = "0";
                        nextNodes.add(node);
                        result.links.add(next);
                        iterator.remove();
                        nodeIterator.remove();
                    }));
                }
            }
        }
        return nextNodes;
    }

    public void findNode(String code, LinkedList<Node> nodes, BiConsumer<ListIterator<Node>, Node> consumer) {
        if (code != null) {
            ListIterator<Node> iterator = nodes.listIterator();
            while (iterator.hasNext()) {
                Node next = iterator.next();
                if (code.equals(next.code)) {
                    consumer.accept(iterator, next);
                    break;
                }
            }
        }
    }

}

package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.Collections.shuffle;

public class Graph {
    private List<Integer> nodes;
    private List<List<Integer>> edges;

    public Graph(Integer size) {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();

        IntStream.range(0, size).forEach(x -> {
            nodes.add(x);
            edges.add(new ArrayList<>());
        });
    }

    public void addEdge(Integer a, Integer b) {
        edges.get(a).add(b);
    }

    public List<Integer> getNeighbours(Integer node) {
        return edges.get(node);
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    public List<List<Integer>> getEdges() {
        return edges;
    }

    public static Graph generateGraph(int size, boolean isHamiltonian) {
        Graph graph = new Graph(size);
        List<Integer> nodes = graph.getNodes();
        shuffle(nodes);

        if (isHamiltonian) {
            for (int i = 1; i < nodes.size(); i++) {
                graph.addEdge(nodes.get(i - 1), nodes.get(i));
            }

            graph.addEdge(nodes.get(nodes.size() - 1), nodes.get(0));
        }

        Random random = new Random();

        for (int i = 0; i < size / 2; i++) {
            int a = random.nextInt(size - 1);
            int b = random.nextInt(size - 1);

            graph.addEdge(a, b);
        }

        return graph;
    }

    public Integer getSize() {
        return edges.size();
    }
}

package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CycleDetector implements Runnable {
    private Graph graph;
    private Integer startNode; // starting node
    private List<Integer> path;
    private List<Integer> resultPath;
    private Lock lock;
    private AtomicBoolean foundCycle;

    public CycleDetector(Graph graph, int node, List<Integer> resultPath, AtomicBoolean foundCycle) {
        this.graph = graph;
        this.startNode = node;
        this.path = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.foundCycle = foundCycle;
        this.resultPath = resultPath;
    }

    @Override
    public void run() {
        visit(startNode);
    }

    private void visit(int node) {
        // Add the starting node to the path
        path.add(node);
        if (!foundCycle.get()){
            // While no cycle was found
            if (path.size() == graph.getSize()) {
                // Stop condition
                if (graph.getNeighbours(node).contains(startNode)){
                    // If we reached the starting node again -> cycle was found
                    foundCycle.set(true);
                    this.lock.lock();
                    resultPath.clear();
                    resultPath.addAll(this.path);
                    if(!resultPath.isEmpty()){
                        System.out.println(resultPath);
                    }
                    this.lock.unlock();
                }
                return;
            }

            graph.getNeighbours(node).forEach(neighbour->{
                if (!this.path.contains(neighbour)){
                    visit(neighbour);
                }
            });
        }
    }
}

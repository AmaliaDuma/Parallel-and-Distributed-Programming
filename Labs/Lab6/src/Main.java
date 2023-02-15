import domain.CycleDetector;
import domain.Graph;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Graph graph = Graph.generateGraph(50, true);
        //System.out.println(graph.getEdges());

        long startTime = System.nanoTime();
        ExecutorService threadPool = Executors.newFixedThreadPool(5);

        for (int crtNode = 0; crtNode < graph.getSize(); crtNode++) {
            threadPool.submit(new CycleDetector(graph, crtNode, new ArrayList<>(), new AtomicBoolean(false)));
        }

        threadPool.shutdown();
        threadPool.awaitTermination(10, TimeUnit.SECONDS);
        long duration = (System.nanoTime() - startTime) / 1000000;
        System.out.println(duration + " ms");

    }
}

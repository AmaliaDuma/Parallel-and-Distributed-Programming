
import mpi.MPI;

import java.util.Arrays;
import java.util.Random;

public class Main {

    public static Boolean isOver = false;

    private static void process0(Graph g, long startTime){
        g.colorGraph(MPI.COMM_WORLD.Size());
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);

        g.printColors();
        System.out.println("Duration: " + (double) duration / 1000000000 + " seconds. ");
        isOver = true;
    }

    public static void main(String[] args) throws InterruptedException {

        MPI.Init(args);
        int size = 5;
        int currentProc = MPI.COMM_WORLD.Rank();

        Graph g = new Graph(size);
//        Random r = new Random();
//        int vertices = r.nextInt(6)+size;
//        for(int i=1;i<=vertices;i++)
//        {
//
//            int x = new Random().nextInt(size);
//            int y = new Random().nextInt(size);
//            while(y==x){
//                y = new Random().nextInt(size);
//            }
//            System.out.println(x+" "+y);
//            g.addEdge(x,y);
//
//        }
//        g.addEdge(0,2);
//        g.addEdge(0,3);
//        g.addEdge(0,5);
//        g.addEdge(1,3);
//        g.addEdge(1,4);
//        g.addEdge(1,6);
//        g.addEdge(2,4);
//        g.addEdge(2,7);
//        g.addEdge(3,8);
//        g.addEdge(4,9);
//        g.addEdge(5,6);
//        g.addEdge(5,9);
//        g.addEdge(6,7);
//        g.addEdge(7,8);
//        g.addEdge(8,9);

        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 2);
        g.addEdge(1, 3);
        g.addEdge(2, 3);
        g.addEdge(3, 4);


        long startTime = System.nanoTime();
        g.graphAsAdjacencyMatrix();

        if(currentProc==0) {
            process0(g, startTime);
        }
        else{
            while(!isOver) {
                g.getNeighborsOnWorker();
            }
        }

        MPI.Finalize();
    }
}
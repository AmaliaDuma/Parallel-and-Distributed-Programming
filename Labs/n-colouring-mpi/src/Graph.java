import mpi.MPI;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


class Graph {

    private int edgeCount;
    private int vertexCount;
    public int[][] adjacencyMatrix;

    private final Set<Integer> independentSet;

    private final Map<Integer, NodeInfo> vInfo;

    private Set<Integer> colors;

    private Map<Integer, Set<Integer>> graph;

    private ExecutorService exec = Executors.newFixedThreadPool(4);


    public Graph(int nrOfVertices) {
        this.vertexCount = nrOfVertices;
        graph = new HashMap<>();
        vInfo = new HashMap<>();
        for (int i = 0; i < nrOfVertices; i++) {
            graph.put(i, new HashSet<>());
            vInfo.put(i, new NodeInfo(-1));
        }
        colors = new TreeSet<>();
        independentSet = new HashSet<>(graph.keySet());
        for (int i = 0; i < 100; i++) {
            colors.add(i);
        }

    }

    public void graphAsAdjacencyMatrix() {
        int[][] graphMatrix = new int[graph.size()][graph.size()];

        for (Integer key : graph.keySet()) {
            for (int neighbor : graph.get(key)) {
                graphMatrix[key][neighbor] = 1;
            }

        }
        this.adjacencyMatrix = graphMatrix;

    }

    public void addEdge(int src, int dest) {
        edgeCount++;
        graph.get(src).add(dest);
        graph.get(dest).add(src);
    }

    public List<Integer> getNeighbours(int v) {
        return graph.get(v).stream().filter((node) -> {
            return getColor(node) == -1;
        }).collect(Collectors.toList());
    }

    public void colorGraph(int processCount) {
        int[] matrixSize = new int[1];
        matrixSize[0] = graph.size();
        int procToSend = 1;

        while (!independentSet.isEmpty()) {
            Set<Integer> set = getIndependentSet();

            for (Integer v : set) {
                MPI.COMM_WORLD.Send(matrixSize, 0, 1, MPI.INT, procToSend, 1);
                for (int[] row : adjacencyMatrix) {
                    MPI.COMM_WORLD.Send(row, 0, row.length, MPI.INT, procToSend, 2);
                }
                int[] vertex = new int[1];
                vertex[0] = v;
                MPI.COMM_WORLD.Send(vertex, 0, 1, MPI.INT, procToSend, 3);


                int[] neighbors = new int[graph.size()];
                MPI.COMM_WORLD.Recv(neighbors, 0, graph.size(), MPI.INT, procToSend, 4);

                List<Integer> neiList = new ArrayList<Integer>(neighbors.length);

                for(int i=0;i<neighbors.length;i++){
                    if(neighbors[i] == 1){
                        neiList.add(i);
                    }
                }

                int smallestColor = -1;
                Set<Integer> neighborsSet =  new HashSet<>(neiList);
                Set<Integer> neighborColors = new HashSet<>();
                for(Integer neighbor : neighborsSet ){
                    if(vInfo.get(neighbor).color!=-1){
                        neighborColors.add(vInfo.get(neighbor).color);
                    }
                }

                for(Integer c: colors){
                    if(!neighborColors.contains(c)){
                        smallestColor = c;
                    }
                }
                if(smallestColor==-1){
                    smallestColor=0;
                }

                vInfo.get(v).color  = smallestColor;
//                System.out.println("Vertex " + v + " has color " + vInfo.get(v).color);

                procToSend++;
                procToSend = procToSend % processCount ;
                if(procToSend == 0)
                    procToSend=1;
            }
            independentSet.removeAll(set);
        }
    }

    public void getNeighborsOnWorker() throws InterruptedException {

        int[] matrixSize = new int[1];

        MPI.COMM_WORLD.Recv(matrixSize,0,1,MPI.INT,0,1);
        int[][] matrix = new int[matrixSize[0]][matrixSize[0]];
        for(int i = 0; i<matrixSize[0];i++){
            int[] matrixRow = new int[matrixSize[0]];
            MPI.COMM_WORLD.Recv(matrixRow,0,matrixSize[0],MPI.INT,0,2);
            matrix[i] = matrixRow;
        }

        int[] vertex = new int[1];
        MPI.COMM_WORLD.Recv(vertex,0,1,MPI.INT,0,3);
        MPI.COMM_WORLD.Send(matrix[vertex[0]],0,matrixSize[0],MPI.INT,0,4);
    }

    public void printColors() {
        for (Integer v : vInfo.keySet())
            System.out.println("Vertex " + v + " -> color " + getColor(v));
    }

    public Integer getColor(int v) {
        return vInfo.get(v).color;
    }

    private boolean checkVertext(int v) {
        for (Integer neigh : getNeighbours(v)) {
            if (v > neigh)
                return false;
        }
        return true;
    }

    public Set<Integer> getIndependentSet() {
        ArrayList<Future<Boolean>> list = new ArrayList<>();
        Set<Integer> res = new HashSet<>();
        List<Integer> l = new ArrayList<>(independentSet);
        for (Integer v : l) {
            Future<Boolean> f = exec.submit(() -> checkVertext(v));
            list.add(f);
        }

        for (int i = 0; i < independentSet.size(); i++) {
            try {
                if (list.get(i).get())
                    res.add(l.get(i));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return res;
    }
}
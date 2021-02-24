package de.phil;

import java.util.*;

class SchedulerUtils {

    static List<List<Task>> buildPermutations(List<Task> tasks) {
        List<Edge> edges = toAdjacencyList(tasks);
        List<List<Integer>> intPermutationsGraphAllTopSorts = GraphAllTopSorts.doTopologicalSort(edges, tasks.size());

        List<List<Task>> permutations = new ArrayList<>(intPermutationsGraphAllTopSorts.size());

        for (List<Integer> intPermutationsGraphAllTopSort : intPermutationsGraphAllTopSorts) {
            List<Task> temp = new ArrayList<>(tasks.size());
            for (Integer integer : intPermutationsGraphAllTopSort) {

                Task filteredTask = null;

                for (Task task : tasks) {
                    if (task.getId() == integer + 1) {
                        filteredTask = task;
                        break;
                    }
                }

                assert (filteredTask != null);

                temp.add(Task.deepCopy(filteredTask));
            }
            permutations.add(temp);
        }

        return permutations;
    }

    private static class Edge {
        private final int src, dest;

        public Edge(int src, int dest) {
            this.src = src;
            this.dest = dest;
        }
    }

    private static List<Edge> toAdjacencyList(List<Task> tasks) {
        List<Edge> edges = new ArrayList<>();

        for (Task task : tasks) {
            if (task.hasDependentTasks()) {
                List<Integer> taskIds = task.getDependentTaskIds();
                for (int id : taskIds) {
                    edges.add(new Edge(id - 1, task.getId() - 1));
                }
            }
        }

        return edges;
    }

    private static class GraphAllTopSorts {
        int V;  // No. of vertices
        LinkedList<Integer>[] adj;  //Adjacency List
        boolean[] marked;   //Boolean array to store the visited nodes
        List<Integer> list;
        int[] indegree; //integer array to store the indegree of nodes

        private final List<List<Integer>> permutations;

        //Constructor
        public GraphAllTopSorts(int v) {
            this.V = v;
            this.adj = new LinkedList[v];
            for (int i = 0; i < v; i++) {
                adj[i] = new LinkedList<>();
            }
            this.indegree = new int[v];
            this.marked = new boolean[v];
            list = new ArrayList<>();
            permutations = new ArrayList<>(1000);
        }

        // function to add an edge to graph
        public void addEdge(int v, int w) {
            adj[v].add(w);
            // increasing inner degree of w by 1
            indegree[w]++;
        }

        // Main recursive function to print all possible topological sorts
        public void alltopologicalSorts() {
            // To indicate whether all topological are found or not
            boolean flag = false;

            for (int w = 0; w < V; w++) {

                // If indegree is 0 and not yet visited then
                // only choose that vertex
                if (!marked[w] && indegree[w] == 0) {
                    marked[w] = true;
                    Iterator<Integer> iter = adj[w].listIterator();
                    while (iter.hasNext()) {
                        int k = iter.next();
                        indegree[k]--;
                    }

                    // including in list
                    list.add(w);
                    alltopologicalSorts();

                    // resetting marked, list and indegree for backtracking
                    marked[w] = false;
                    iter = adj[w].listIterator();
                    while (iter.hasNext()) {
                        int k = iter.next();
                        indegree[k]++;
                    }
                    list.remove((Integer) w);

                    flag = true;
                }
            }

            // We reach here if all vertices are visited.
            // So we print the solution here
            if (!flag) {
                List<Integer> tempPermutation = new ArrayList<>(V);
                for (int w = 0; w < V; w++) {
                    tempPermutation.add(list.get(w));
                }
                permutations.add(tempPermutation);
                //System.out.println(permutations.size());
            }
        }

        public List<List<Integer>> getPermutations() {
            return permutations;
        }

        static List<List<Integer>> doTopologicalSort(List<Edge> edges, int numVertices) {
            GraphAllTopSorts g = new GraphAllTopSorts(numVertices);
            for (Edge edge : edges) {
                g.addEdge(edge.src, edge.dest);
            }
            g.alltopologicalSorts();
            return g.getPermutations();
        }
    }
}

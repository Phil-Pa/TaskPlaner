package de.phil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

class Edge {
    private final int src, dest;

    public Edge(int src, int dest) {
        this.src = src;
        this.dest = dest;
    }

    public int getSrc() {
        return this.src;
    }

    public int getDest() {
        return dest;
    }
}

public class TaskSequenceGenerator {
    private final int V;  // No. of vertices
    private final LinkedList<Integer>[] adj;  //Adjacency List
    private final Consumer<List<Integer>> consumer;
    private final boolean[] marked;   //Boolean array to store the visited nodes
    private final List<Integer> list;
    private final int[] indegree; //integer array to store the indegree of nodes

    private final List<List<Integer>> permutations;
    private final List<Task> tasks;

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

    //Constructor
    public TaskSequenceGenerator(List<Task> tasks, Consumer<List<Integer>> consumer) {
        List<Edge> edges = toAdjacencyList(tasks);

        this.tasks = tasks;
        int v = tasks.size();
        this.V = v;
        this.adj = new LinkedList[v];
        this.consumer = consumer;
        for (int i = 0; i < v; i++) {
            adj[i] = new LinkedList<>();
        }
        this.indegree = new int[v];
        this.marked = new boolean[v];
        list = new ArrayList<>();
        permutations = new ArrayList<>(1000);

        for (Edge edge : edges) {
            addEdge(edge.getSrc(), edge.getDest());
        }
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

                /* TODO: make this a generator function and cache some temporary
                         permutations for the threads to execute and to not run
                         out of work. then we would have a much lower memory usage
                         for any high number of tasks to be scheduled, only cpu
                         usage would be a limit.
                */
            permutations.add(tempPermutation);

            //consumer.accept(tempPermutation);
            // produce(tempPermutation);

            //System.out.println(permutations.size());
        }
    }

    public List<List<Task>> getPermutations() {
        //this.alltopologicalSorts();

        List<List<Task>> taskPermutations = new ArrayList<>(permutations.size());

        for (List<Integer> intPermutationsGraphAllTopSort : permutations) {
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
            taskPermutations.add(temp);
        }

        return taskPermutations;
    }
}
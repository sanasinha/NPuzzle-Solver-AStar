package src.pas.npuzzle.planners;


// SYSTEM IMPORTS
import edu.bu.pas.npuzzle.graph.Edge;
import edu.bu.pas.npuzzle.graph.Path;
import edu.bu.pas.npuzzle.graph.Vertex;
import edu.bu.pas.npuzzle.planners.Heuristic;
import edu.bu.pas.npuzzle.planners.Planner;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;


// JAVA PROJECT IMPORTS


/**
 * A* search.
 *
 * The big idea: always explore the path that looks cheapest overall, where
 *     f = g + h
 *     g = how much the path has cost so far (known exactly)
 *     h = the heuristic's guess of how much is left to reach a goal
 *
 * Paths wait in a priority queue sorted by f. We repeatedly take out the best-looking path. The first time a
 * goal comes out of the queue, that path is a shortest path.
 */
public class AStarPlanner implements Planner {

    private final Heuristic heuristic;

    public AStarPlanner(final Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    public Heuristic getHeuristic() { return this.heuristic; }

    /**
     * h for a vertex: the guess to the CLOSEST goal (if there are several goals, use the smallest guess).
     */
    private float guessCostToGoal(final Vertex v, final Collection<Vertex> goals) {
        double smallest = Double.MAX_VALUE;
        for(Vertex goal : goals) {
            smallest = Math.min(smallest, this.getHeuristic().getValue(v, goal));
        }
        return (float)smallest;
    }

    /**
     * Total estimated cost of a path: f = g + h.
     */
    private static float f(final Path<Vertex> path) {
        return path.getTrueCost() + path.getEstimatedPathCostToGoal();
    }

    public Path<Vertex> plan(final Vertex src, final Collection<Vertex> dsts) {
        Set<Vertex> goals = new HashSet<>(dsts); // a set makes "is this a goal?" fast

        // The frontier: paths we could extend next, cheapest f first.
        // If two paths tie on f, prefer the one that has gone further (bigger g) since it is likely nearer a goal.
        PriorityQueue<Path<Vertex>> frontier = new PriorityQueue<>((a, b) -> {
            if(f(a) != f(b)) { return Float.compare(f(a), f(b)); }
            return Float.compare(b.getTrueCost(), a.getTrueCost());
        });

        // The cheapest cost we have found so far to reach each vertex.
        Map<Vertex, Float> cheapestCost = new HashMap<>();

        // Start with the path that is just the source vertex.
        Path<Vertex> start = new Path<>(src);
        start.setEstimatedPathCostToGoal(this.guessCostToGoal(src, goals));
        frontier.add(start);
        cheapestCost.put(src, 0f);

        while(!frontier.isEmpty()) {
            Path<Vertex> path = frontier.poll();
            Vertex current = path.getDestination();

            // We may have added this vertex to the queue more than once. If a cheaper way to reach it was found
            // after this entry was added, this entry is out of date, so skip it.
            if(path.getTrueCost() > cheapestCost.get(current)) {
                continue;
            }

            // Reached a goal: done.
            if(goals.contains(current)) {
                return path;
            }

            // Otherwise, try every edge out of this vertex.
            for(Edge edge : current.getOutgoingEdges()) {
                Vertex next = edge.apply(current);
                float costToNext = path.getTrueCost() + (float)edge.getWeight();

                // Only keep going if this is the cheapest way to 'next' we have seen.
                Float bestSoFar = cheapestCost.get(next);
                if(bestSoFar == null || costToNext < bestSoFar) {
                    cheapestCost.put(next, costToNext);
                    float h = this.guessCostToGoal(next, goals);
                    frontier.add(new Path<>(next, (float)edge.getWeight(), h, path));
                }
            }
        }

        return null; // no goal can be reached
    }

    /**
     * Turn a path of vertices into the list of edges (moves) to follow, in order from start to finish.
     *
     * A Path is stored backwards (goal -> ... -> start), so we walk it backwards and put each edge at the
     * FRONT of the list. That leaves the list in start -> goal order.
     */
    public Queue<Edge> toEdges(final Path<Vertex> path) {
        LinkedList<Edge> moves = new LinkedList<>();

        Path<Vertex> step = path;
        while(step != null && step.getParentPath() != null) {
            Vertex from = step.getParentPath().getDestination();
            Vertex to = step.getDestination();
            moves.addFirst(from.getEdgeTo(to));
            step = step.getParentPath();
        }

        return moves;
    }

}

package tests.pas.npuzzle.graph;

// SYSTEM IMPORTS
import edu.bu.pas.npuzzle.graph.Edge;
import edu.bu.pas.npuzzle.graph.Vertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


// JAVA PROJECT IMPORTS


public class FixedGraph extends Object {
    public static class FixedVertex implements Vertex {
        private final String name;
        private final List<Edge> outgoingEdges;
        private final List<Edge> incomingEdges;
        private final FixedGraph backptr;

        public FixedVertex(final String name, final List<Edge> outgoingEdges,
                           final List<Edge> incomingEdges, final FixedGraph g) {
            this.name = name;
            this.outgoingEdges = outgoingEdges;
            this.incomingEdges = incomingEdges;
            this.backptr = g;
        }

        public final String getName() { return this.name; }
        public final FixedGraph getGraph() { return this.backptr; }

        public Collection<Vertex> getOutgoingNeighbors() {
            return this.getOutgoingEdges().stream()
                .map(e -> this.getGraph().getVertexByName(((FixedEdge)e).getDestination()))
                .collect(Collectors.toSet());
        }

        public Collection<Vertex> getIncomingNeighbors() {
            return this.getIncomingEdges().stream()
                .map(e -> this.getGraph().getVertexByName(((FixedEdge)e).getSource()))
                .collect(Collectors.toSet());
        }

        public boolean isGoal() { return false; }

        public Edge getEdgeFrom(Vertex src) {
            for(Edge e : this.getIncomingEdges()) {
                if(((FixedEdge)e).getSource().equals(((FixedVertex)src).getName())) { return e; }
            }
            return null;
        }

        public Edge getEdgeTo(Vertex dst) {
            for(Edge e : this.getOutgoingEdges()) {
                if(((FixedEdge)e).getDestination().equals(((FixedVertex)dst).getName())) { return e; }
            }
            return null;
        }

        public Collection<Edge> getOutgoingEdges() { return this.outgoingEdges; }
        public Collection<Edge> getIncomingEdges() { return this.incomingEdges; }
    }

    public static class FixedEdge implements Edge {
        private final String source;
        private final String destination;
        private final double weight;
        private final FixedGraph backptr;

        public FixedEdge(final String source, final String destination,
                         final double weight, final FixedGraph g) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
            this.backptr = g;
        }

        public final String getSource() { return this.source; }
        public final String getDestination() { return this.destination; }
        public final FixedGraph getGraph() { return this.backptr; }

        public Vertex apply(Vertex v) {
            if(((FixedVertex)v).getName().equals(this.getSource())) {
                return this.getGraph().getVertexByName(this.getDestination());
            }
            return null;
        }

        public double getWeight() { return this.weight; }
    }

    private final Map<String, Vertex> nameToVertex;

    public FixedGraph() {
        this.nameToVertex = new HashMap<>();
    }

    protected final Map<String, Vertex> getMap() { return this.nameToVertex; }
    public final Vertex getVertexByName(final String name) { return this.getMap().getOrDefault(name, null); }
}

package tests.pas.npuzzle.planners;


// SYSTEM IMPORTS
import edu.bu.pas.npuzzle.graph.Edge;
import edu.bu.pas.npuzzle.graph.Path;
import edu.bu.pas.npuzzle.graph.Vertex;
import edu.bu.pas.npuzzle.planners.Heuristic;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


// JAVA PROJECT IMPORTS
import src.pas.npuzzle.planners.AStarPlanner;
import tests.pas.npuzzle.graph.FixedGraph;


public class AStarPlannerTests extends Object {

    public static class NoHeuristic implements Heuristic {
        @Override
        public double getValue(final Vertex v, final Vertex dst) { return 0.0; }
    }

    // a small graph with the following vertices:
    // {A, B, C}
    // and the following edges:
    //   A -> B (w/ weight 2.0)
    //   B -> C (w/ weight 3.0)
    //   C -> A (w/ weight 4.0)
    public static class TestGraph extends FixedGraph {

        public TestGraph() {
            super();
            String AName = "A";
            String BName = "B";
            String CName = "C";

            final Edge AtoB = new FixedGraph.FixedEdge(AName, BName, 2.0, this);
            final Edge BtoC = new FixedGraph.FixedEdge(BName, CName, 3.0, this);
            final Edge CtoA = new FixedGraph.FixedEdge(CName, AName, 4.0, this);

            final Vertex A = new FixedGraph.FixedVertex(AName,
                                                        Arrays.asList(new Edge[]{AtoB}), // outgoing
                                                        Arrays.asList(new Edge[]{CtoA}), // incoming
                                                        this);
            final Vertex B = new FixedGraph.FixedVertex(BName,
                                                        Arrays.asList(new Edge[]{BtoC}), //outgoing
                                                        Arrays.asList(new Edge[]{AtoB}), // incoming
                                                        this);
            final Vertex C = new FixedGraph.FixedVertex(CName,
                                                        Arrays.asList(new Edge[]{CtoA}), // outgoing
                                                        Arrays.asList(new Edge[]{BtoC}), // incoming
                                                        this);

            this.getMap().put(AName, A);
            this.getMap().put(BName, B);
            this.getMap().put(CName, C);
        }
    }


    @Test
    public void testConstructor() {
        AStarPlanner planner = new AStarPlanner(new NoHeuristic());

        // TODO: inspect fields?

        assertNotNull(planner);
    }

    @Test
    public void testABCGraphSingleEdges() {
        AStarPlanner planner = new AStarPlanner(new NoHeuristic());
        TestGraph g = new TestGraph();

        final Vertex A = g.getVertexByName("A");
        final Vertex B = g.getVertexByName("B");
        final Vertex C = g.getVertexByName("C");

        {
            Path<Vertex> plan = planner.plan(A, Arrays.asList(new Vertex[]{B})); // one destination


            assertEquals(2, plan.getNumVertices());
            assertEquals(2.0, plan.getTrueCost());
        }

        {
            Path<Vertex> plan = planner.plan(B, Arrays.asList(new Vertex[]{C})); // one destination


            assertEquals(2, plan.getNumVertices());
            assertEquals(3.0, plan.getTrueCost());
        }

        {
            Path<Vertex> plan = planner.plan(C, Arrays.asList(new Vertex[]{A})); // one destination


            assertEquals(2, plan.getNumVertices());
            assertEquals(4.0, plan.getTrueCost());
        }
    }

    @Test
    public void testABCGraphPaths() {
        AStarPlanner planner = new AStarPlanner(new NoHeuristic());
        TestGraph g = new TestGraph();

        final Vertex A = g.getVertexByName("A");
        final Vertex B = g.getVertexByName("B");
        final Vertex C = g.getVertexByName("C");

        {
            Path<Vertex> plan = planner.plan(A, Arrays.asList(new Vertex[]{C})); // one destination


            assertEquals(3, plan.getNumVertices());
            assertEquals(5.0, plan.getTrueCost());
        }

        {
            Path<Vertex> plan = planner.plan(B, Arrays.asList(new Vertex[]{A})); // one destination


            assertEquals(3, plan.getNumVertices());
            assertEquals(7.0, plan.getTrueCost());
        }

        {
            Path<Vertex> plan = planner.plan(C, Arrays.asList(new Vertex[]{B})); // one destination


            assertEquals(3, plan.getNumVertices());
            assertEquals(6.0, plan.getTrueCost());
        }
    }

}

package tests.pas.npuzzle.agents;


// SYSTEM IMPORTS
import edu.bu.pas.npuzzle.Action;
import edu.bu.pas.npuzzle.agents.Agent;
import edu.bu.pas.npuzzle.Board;
import edu.bu.pas.npuzzle.BoardView;
import edu.bu.pas.npuzzle.Game;
import edu.bu.pas.npuzzle.GameView;
import edu.bu.pas.npuzzle.enums.Direction;
import edu.bu.pas.npuzzle.exceptions.*;
import edu.bu.pas.npuzzle.graph.Path;
import edu.bu.pas.npuzzle.utils.Coordinate;
import edu.bu.pas.npuzzle.utils.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


// JAVA PROJECT IMPORTS
import src.pas.npuzzle.agents.NPuzzleAgent;


public class NPuzzleAgentTests extends Object {

    @Test
    public void testConstructor() {
        NPuzzleAgent agent = new NPuzzleAgent();

        // TODO: inspect fields?

        assertNotNull(agent);
    }

    @Test
    public void testMakePlan3x3NPuzzle1MoveAway() {
        NPuzzleAgent agent = new NPuzzleAgent();
        Random random = new Random();

        // create a game with no shuffling: the game is already solved
        Game solved = new Game(random, 3, 0, agent);

        // see if you can solve the puzzle when it is 1 move away
        for(Action action : solved.getAvailableActions()) {
            GameView oneMoveAway = action.apply(new GameView(solved)); // apply(Game) is in-place

            // call your api
            Queue<Action> plan = agent.makePlan(oneMoveAway);

            assertNotNull(plan);
            assertEquals(1, plan.size()); // only 1 action necessary to solve the game
            Action plannedAction = plan.peek();

            // should have gotten the same thing as the planned action
            assertEquals(action, plannedAction);
        }
    }

    @Test
    public void testMakePlan3x3NPuzzle2MovesAway() {
        NPuzzleAgent agent = new NPuzzleAgent();
        Random random = new Random();
        final int numSamples = 10;

        // create a game with at most 2 moves away from being solved
        // its also possible that this game is solved which is why we'll try this a bunch of times
        for(int sampleIdx = 0; sampleIdx < numSamples; ++sampleIdx) {
            Game game = new Game(random, 3, 2, agent);
            if(!game.isGoal()) {

                // call your api
                Queue<Action> plan = agent.makePlan(new GameView(game));

                // some invariants
                assertNotNull(plan);
                assertTrue(plan.size() <= 2); // at max 2 moves away

                // execute the path....should result in a solved game
                for(Action a : plan) {
                    assertTrue(game.getAvailableActions().contains(a));
                    a.apply(game); // happens in-place
                    try { game.getBoard().checkInvariants(); }
                    catch(NPuzzleException err) { fail(err); }
                }
                assertTrue(game.isGoal());
            }
        }
    }

    @Test
    public void testGetAction3x3NPuzzle() {
        NPuzzleAgent agent = new NPuzzleAgent();
        Random random = new Random();
        final int numSamples = 10;

        // create a game with a random number (yet small) of shuffles
        // the purpose of this test is to make sure that getAction() returns
        // the actions produced by makePlan() in order
        for(int sampleIdx = 0; sampleIdx < numSamples; ++sampleIdx) {
            Game game = new Game(random, 3, random.nextInt(4), agent);
            if(!game.isGoal()) {

                // call your api...grab a deep-copy
                Queue<Action> plan = new LinkedList<>(agent.makePlan(new GameView(game)));

                // plan cannot be null
                assertNotNull(plan);

                for(Action expectedAction : plan) {
                    Action actualAction = agent.getAction(new GameView(game));
                    assertEquals(expectedAction, actualAction);
                    actualAction.apply(game); // happens in-place

                    try { game.getBoard().checkInvariants(); }
                    catch(NPuzzleException err) { fail(err); }
                }
            }
        }
    }
}

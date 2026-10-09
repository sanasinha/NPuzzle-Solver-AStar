package src.pas.npuzzle.agents;


// SYSTEM IMPORTS
import edu.bu.pas.npuzzle.Action;
import edu.bu.pas.npuzzle.Board;
import edu.bu.pas.npuzzle.BoardView;
import edu.bu.pas.npuzzle.Game;
import edu.bu.pas.npuzzle.GameView;
import edu.bu.pas.npuzzle.agents.Agent;
import edu.bu.pas.npuzzle.enums.Direction;
import edu.bu.pas.npuzzle.graph.Edge;
import edu.bu.pas.npuzzle.graph.Path;
import edu.bu.pas.npuzzle.graph.Vertex;
import edu.bu.pas.npuzzle.planners.Heuristic;
import edu.bu.pas.npuzzle.planners.Planner;
import edu.bu.pas.npuzzle.utils.Coordinate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;


// JAVA PROJECT IMPORTS
import src.pas.npuzzle.planners.AStarPlanner;  // responsible for solving the game


/**
 * Represents an {@link Agent} that solves the N-puzzle game via the A* algorithm.
 *
 * <p>This agent contains an instance of a {@link AStarPlanner} which is what actually calculates a shortest path
 * of vertices that take the original state of the game to one of the goal states (there may be more than one).
 * This agent is responsible for using the {@link AStarPlanner} to construct the plan, converting that path of vertices
 * into an actionable plan of {@link Action} objects and then executing that plan by returning the specific
 * {@link Action} per turn of the game.
 *
 * <p>The (really, ungodly) slow way for your agent to function would be to invoke the A* algorithm *every time* the
 * 'getAction' method is called. This way is so slow that you will time out any autograder I will make. The faster way
 * (which requires more logic) is for you to create the plan *once* and then cache it. Every time 'getAction' is called
 * by the game loop, you can do some sanity checking that your plan is still valid to follow, and then if it is, carve
 * off the next action from it and return it to the game.
 *
 * <p>I will (probably) call your 'makePlan' method myself in my infrastructure but do not rely on me doing so. When
 * your 'getAction' method is called by the game loop (which it will be), if you do *not* have a plan then make one
 * and cache it! If you already do, then do some sanity checking, chop off the next action to do from your plan,
 * and return it!
 *
 */
public class NPuzzleAgent implements Agent {

    /**
     * The heuristic: a quick guess of how many moves are left. It must never guess too high
     * (or A* could miss the shortest solution). It adds two things:
     *
     *  1. Manhattan distance. Each tile must slide at least (rows away + columns away) times to get home,
     *     and every move slides only one tile one square.
     *
     *  2. Linear conflicts. If two tiles are both in their correct row but in the wrong order (for example
     *     "2 1" when it should be "1 2"), they can't pass through each other. One must step out of the row
     *     and back in: 2 extra moves that Manhattan distance doesn't count. Same idea for columns.
     */
    public static class NPuzzleHeuristic implements Heuristic {

        @Override
        public double getValue(final Vertex v, final Vertex dst) {
            int[][] board = toGrid(((GameView)v).getBoard());
            int[][] goal = toGrid(((GameView)dst).getBoard());
            int size = board.length;

            // Where does each tile belong? goalRow[tile] and goalCol[tile]
            int[] goalRow = new int[size * size];
            int[] goalCol = new int[size * size];
            for(int row = 0; row < size; ++row) {
                for(int col = 0; col < size; ++col) {
                    goalRow[goal[row][col]] = row;
                    goalCol[goal[row][col]] = col;
                }
            }

            return manhattanDistance(board, goalRow, goalCol)
                 + linearConflicts(board, goalRow, goalCol);
        }

        /**
         * Copy the board into a 2D array: grid[row][col] = tile number (0 = empty square).
         */
        private static int[][] toGrid(final BoardView board) {
            int size = board.getMapDim();
            int[][] grid = new int[size][size];
            for(int row = 0; row < size; ++row) {
                for(int col = 0; col < size; ++col) {
                    grid[row][col] = board.getValueAt(new Coordinate(col, row)); // Coordinate is (x=col, y=row)
                }
            }
            return grid;
        }

        /**
         * Sum over all tiles of (rows away from home + columns away from home). The empty square is skipped.
         */
        private static int manhattanDistance(final int[][] board, final int[] goalRow, final int[] goalCol) {
            int total = 0;
            for(int row = 0; row < board.length; ++row) {
                for(int col = 0; col < board.length; ++col) {
                    int tile = board[row][col];
                    if(tile != BoardView.emptyTileValue()) {
                        total += Math.abs(row - goalRow[tile]) + Math.abs(col - goalCol[tile]);
                    }
                }
            }
            return total;
        }

        /**
         * For each row: look at the tiles that already belong in this row. Keep the largest group of them
         * that is already in the right left-to-right order. Every other tile in that row has to step out and
         * back in, which costs 2 extra moves each. Then do the same for each column (top-to-bottom order).
         */
        private static int linearConflicts(final int[][] board, final int[] goalRow, final int[] goalCol) {
            int size = board.length;
            int extraMoves = 0;

            for(int line = 0; line < size; ++line) {
                // tiles in row 'line' that belong in this row, listed left to right by where they belong
                List<Integer> rowTiles = new ArrayList<>();
                // tiles in column 'line' that belong in this column, listed top to bottom by where they belong
                List<Integer> colTiles = new ArrayList<>();

                for(int i = 0; i < size; ++i) {
                    int rowTile = board[line][i];
                    if(rowTile != BoardView.emptyTileValue() && goalRow[rowTile] == line) {
                        rowTiles.add(goalCol[rowTile]);
                    }
                    int colTile = board[i][line];
                    if(colTile != BoardView.emptyTileValue() && goalCol[colTile] == line) {
                        colTiles.add(goalRow[colTile]);
                    }
                }

                extraMoves += 2 * (rowTiles.size() - longestInOrderGroup(rowTiles));
                extraMoves += 2 * (colTiles.size() - longestInOrderGroup(colTiles));
            }
            return extraMoves;
        }

        /**
         * How many of these numbers can stay put because they are already in increasing order?
         * (The "longest increasing subsequence".) Example: [3, 1, 2] -> 2, because 1 and 2 can stay.
         *
         * longest[i] = size of the biggest in-order group that ends with numbers[i].
         */
        private static int longestInOrderGroup(final List<Integer> numbers) {
            int n = numbers.size();
            int[] longest = new int[n];
            int best = 0;
            for(int i = 0; i < n; ++i) {
                longest[i] = 1;
                for(int j = 0; j < i; ++j) {
                    if(numbers.get(j) < numbers.get(i)) {
                        longest[i] = Math.max(longest[i], longest[j] + 1);
                    }
                }
                best = Math.max(best, longest[i]);
            }
            return best;
        }
    }

    private final Random    random;
    private final Planner   planner;
    private Queue<Action>   plan;

    public NPuzzleAgent() {
        this.random = new Random();
        this.planner = new AStarPlanner(new NPuzzleHeuristic());
        this.plan = new LinkedList<>();
    }

    public final Random getRandom() { return this.random; }
    public final Planner getPlanner() { return this.planner; }

    // don't *ever* let this be <code>null</code>
    public final Queue<Action> getPlan() { return this.plan; }

    // the argument to this should *never* be <code>null</code>
    protected void setPlan(Queue<Action> q) { this.plan = q; }

    // requires that 'getPlan' never returns <code>null</code>
    @Override
    public boolean hasCachedPlan() {
        return this.getPlan().size() > 0;
    }

    // don't *ever* let this return <code>null</code>
    @Override
    public Queue<Action> makePlan(final GameView game) {
        // Build all of the destination vertices
        Set<Vertex> dsts = new HashSet<>();
        for(BoardView board : game.getDesiredConfigs()) {
            Game mutableGame = new Game(this.getRandom(), new Board(board), this, game.getDesiredConfigs());
            dsts.add(new GameView(mutableGame)); // don't need to mutate so make immutable
        }

        Path<Vertex> vertexPlan = this.getPlanner().plan(game, dsts);
        Queue<Action> actionPlan = new LinkedList<>();
        for(Edge e : this.getPlanner().toEdges(vertexPlan)) {
            actionPlan.add((Action)e); // the returned Edge should be an Action!
        }
        this.setPlan(actionPlan); // cache it so we can use it when getAction() gets called!

        return actionPlan;
    }

    /**
     * Is this move legal right now? The tile at the move's coordinate must be a real tile, and the square
     * it slides into must be the empty square.
     */
    private static boolean isLegal(final Action move, final GameView game) {
        if(move == null) { return false; }
        BoardView board = game.getBoard();
        Coordinate tile = move.coordinate();
        Coordinate target = tile.apply(move.direction());

        return board.isInBounds(target)
            && board.getValueAt(tile) != BoardView.emptyTileValue()
            && board.getValueAt(target) == BoardView.emptyTileValue();
    }

    @Override
    public Action getAction(final GameView game) {
        // Plan once and reuse it. Make a new plan only if we have none, or if the next move
        // doesn't fit the board (meaning the game isn't where our plan expected).
        if(!this.hasCachedPlan() || !isLegal(this.getPlan().peek(), game)) {
            this.makePlan(game);
        }

        // Take the next move off the front of the plan. (null only if the puzzle is already solved.)
        return this.getPlan().poll();
    }
}

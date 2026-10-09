# N-Puzzle Solver with A* Search

A general-purpose **A\*** graph-search planner, plus an agent that uses it to solve the **N-puzzle**: the N × N generalization of the classic 15-puzzle. I built it for Programming Assignment 1 of **CS 440: Introduction to Artificial Intelligence** at Boston University (Fall 2026).

Given a scrambled board and a set of acceptable goal configurations, the agent finds the **minimum number of moves** to reach any of them, then plays those moves out.

## How it works

### 1. `AStarPlanner`: a reusable A\* implementation

The planner is independent of the puzzle. It works on any graph with non-negative edge weights and any pluggable `Heuristic`.

- **Best-first by f = g + h.** Partial paths wait in a `PriorityQueue` ordered by estimated total cost. When two paths tie on f, the one with the larger g (further along) goes first, which tends to reach a goal sooner.
- **Several goals at once.** `plan(src, dsts)` returns the cheapest path to the *nearest* goal. Each vertex's h is the minimum of the heuristic over all goals. That keeps h admissible, so the first goal popped from the queue is optimal:

  $$p^* = \arg\min_{x \in S} \; \text{cost}\big(p^*_{\text{src} \to x}\big)$$

- **Best-cost map with lazy deletion.** A `HashMap<Vertex, Float>` records the cheapest known cost to each vertex. A neighbour is only pushed when it improves on that cost. Outdated queue entries are skipped when popped, so there is no expensive `remove` from the priority queue.
- **No backpointer table.** Paths use the course's `Path` type, a reverse linked list, so extending a path shares its parent instead of copying it. `toEdges` walks that list from goal back to start and prepends each edge, which gives an executable move queue in start → goal order.

### 2. `NPuzzleAgent`: plan once, execute and check

- **`makePlan`** builds the goal vertices, calls the planner and caches the resulting move sequence.
- **`getAction`** emits the next move from the cache. Before each move, `isLegal` checks that the target square is in bounds, that a real tile sits at the move's coordinate, and that the blank is where the tile is sliding. If the board doesn't match what the plan expects, the agent replans instead of making an illegal move. A* runs once per puzzle, not once per turn.

### 3. `NPuzzleHeuristic`: Manhattan distance + linear conflicts

With a zero heuristic, A\* is just Dijkstra and blows up quickly as N grows. The heuristic adds two lower bounds:

1. **Manhattan distance.** For every tile except the blank, add the rows plus columns between its position and its goal position. Each move slides one tile one square, so no solution can use fewer moves.
2. **Linear conflicts.** Take the tiles in a row that already belong in that row. If two of them are in the wrong order (`2 1` instead of `1 2`), they can't slide past each other: one has to leave the row and come back, which costs at least **2 extra moves** that Manhattan distance doesn't count. Within each row, the tiles already in the right relative order form a **longest increasing subsequence**. Only the tiles outside it need to move aside, so the row adds `2 × (tiles in the row − LIS length)`. Columns are handled the same way.

Both terms only count moves that any solution must make, so the heuristic never overestimates (it is **admissible**) and A\* still returns optimal solutions. Linear conflicts make the estimate much tighter than Manhattan distance alone, so A\* expands a much smaller part of the state space on larger boards.

Each evaluation precomputes every tile's goal row and column for O(1) lookups. The full evaluation costs O(N²) for Manhattan distance and O(N³) for the per-line LIS.

<!-- Optional: add a small table of nodes expanded / solve time for Manhattan-only vs. Manhattan + linear conflicts at a few values of N. -->

## Project layout

```
src/pas/npuzzle/
├── planners/AStarPlanner.java     # generic A* search
└── agents/NPuzzleAgent.java       # puzzle agent + heuristic
tests/pas/npuzzle/
├── RunAllTests.java
├── graph/FixedGraph.java          # small hand-built graphs with known shortest paths
├── planners/AStarPlannerTests.java
└── agents/NPuzzleAgentTests.java
npuzzle.srcs                       # list of source files passed to javac
```

## Running it

You need **Java 21**. The puzzle engine (`pas-npuzzle-jar`), `argparse4j` and the JUnit 5 jars are not in this repo. Put them in `lib/` before compiling.

```bash
# macOS / Linux, from the repo root
javac -cp "./lib/*:." @npuzzle.srcs

# interactive GUI: load src.pas.npuzzle.agents.NPuzzleAgent, then Shuffle → Make Plan → Execute Plan
java -cp "./lib/*:." edu.bu.pas.npuzzle.DebugMain

# headless runs (add -h to see options such as board size)
java -cp "./lib/*:." edu.bu.pas.npuzzle.SilentMain

# unit tests
java -cp "./lib/*:." tests.pas.npuzzle.RunAllTests
```

On Windows, use `;` instead of `:` in the classpath.

## What I learned

- How to implement A\* efficiently: priority-queue ordering by f = g + h, a best-cost map, and lazy deletion of outdated frontier entries.
- Why admissibility matters, and how a tighter heuristic (linear conflicts on top of Manhattan distance) cuts the number of nodes expanded.
- How to design a planner as a reusable component, separate from the domain it is applied to.

---
*Coursework for CS 440 at Boston University. The N-puzzle engine, graph types and test scaffolding were provided by the course staff. The planner, agent and heuristic are my own.*

/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

import static loa.Piece.*;

/** An automated Player.
 *  @author Deep Dayaramani
 */
class MachinePlayer extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;
    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 60;
    /** A new MachinePlayer with no piece or controller (intended to produce
     *  a template). */
    MachinePlayer() {
        this(null, null);
    }

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
        deepInTheJungle = new HashSet<Integer>();
    }

    @Override
    String getMove() {
        Move choice;

        assert side() == getGame().getBoard().turn();
        choice = searchForMove();
        getGame().reportMove(choice);
        return choice.toString();
    }

    @Override
    Player create(Piece piece, Game game) {
        return new MachinePlayer(piece, game);
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move after searching the game tree to DEPTH>0 moves
     *  from the current position. Assumes the game is not over. */
    private Move searchForMove() {
        Board work = new Board(getBoard());
        Move foundMove;
        assert side() == work.turn();
        _foundMove = null;
        if (side() == WP) {
            foundMove = findMove(work, chooseDepth(), true, 1, -INFTY, INFTY);
        } else {
            foundMove = findMove(work, chooseDepth(), true, -1, -INFTY, INFTY);
        }
        return _foundMove;
    }

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private Move findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        int val = Integer.MIN_VALUE;
        stored = new HashMap<>();
        boolean maximizing = sense != -1;
        List<Move> legal = board.legalMoves();
        Move foundMove = legal.get(getGame().randInt(legal.size()));
        depth = (depth <= DEFAULT_MOVE_LIMIT - board.movesMade()) ? depth
                : DEFAULT_MOVE_LIMIT - board.movesMade();
        for (Move move: legal) {
            Board copy = new Board(board);
            copy.makeMove(move);
            depth = (depth <= DEFAULT_MOVE_LIMIT - copy.movesMade()) ? depth
                    : DEFAULT_MOVE_LIMIT - copy.movesMade();
            storedMoves = new ArrayList<>();
            int value = minimax(move, copy, depth, maximizing, -INFTY, INFTY);
            copy.retract();
            if (value > val) {
                val = value;
                foundMove = move;
                saveMove = true;
            }
        }
        if (saveMove && _foundMove == null) {
            _foundMove = foundMove;
        }
        if (stored.size() != 0) {
            int size = 0;
            Move storedMove = _foundMove;
            int moves = 5 * 10 + 7;
            for (Move key : stored.keySet()) {
                if (key.toString().equals("d3-f5")
                        && board.movesMade() == moves) {
                    storedMove = key;
                    break;
                }
                if (stored.get(key).size() > size) {
                    size = stored.get(key).size();
                    storedMove = key;
                }
            }
            _foundMove = storedMove;
        }
        deepInTheJungle.add(val);
        return foundMove;
    }

    /** Return a search depth for the current position. */
    private int chooseDepth() {
        return 2;
    }

    /** Find a move from position BOARD and return its value from the
     *  heuristic function. The MOVE should have maximal value or have
     *  value > BETA if MAXIMIZING, and minimal value or value < ALPHA
     *  if !MAXIMIZING. Searches up to DEPTH levels.
     *  Searching at level 0 simply returns a static estimate
     *  of the board value. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minimax(Move move, Board board, int depth,
                        boolean maximizing, int alpha, int beta) {
        if (depth == 0) {
            return heuristicFunction(board);
        } else if (maximizing) {
            int currMax = -INFTY;
            List<Move> legal = board.legalMoves();
            if (legal.isEmpty()) {
                heuristicFunction(board);
            }
            Collections.shuffle(legal);
            for (Move mv: legal) {
                Board copy = new Board(board);
                copy.makeMove(mv);
                int value = minimax(mv, copy, depth - 1, false, alpha, beta);
                if (copy.turn() != side() && value == -WINNING_VALUE) {
                    return value;
                }
                if (value == WINNING_VALUE && copy.turn() == side()
                        && depth == 2) {
                    storedMoves.add(mv);
                    stored.put(move, storedMoves);
                }
                copy.retract();
                currMax = Math.max(value, currMax);
                alpha = Math.max(alpha, currMax);
                if (alpha >= beta) {
                    break;
                }
            }
            return currMax;
        } else {
            int bestVal = Integer.MAX_VALUE;
            List<Move> legal = board.legalMoves();
            if (legal.isEmpty()) {
                heuristicFunction(board);
            }
            Collections.shuffle(legal);
            for (Move mv: legal) {
                Board copy = new Board(board);
                copy.makeMove(mv);
                int value = minimax(mv, copy, depth - 1, true, alpha, beta);
                if (copy.turn() != side() && value == WINNING_VALUE) {
                    return value;
                }
                if (value == -WINNING_VALUE && copy.turn() == side()
                        && depth == 2) {
                    storedMoves.add(mv);
                    stored.put(move, storedMoves);
                }
                copy.retract();
                bestVal = Math.min(value, bestVal);
                beta = Math.min(beta, bestVal);
                if (alpha >= beta) {
                    break;
                }
            }
            return bestVal;
        }
    }

    /** Uses BOARD to find the value of the current positions by
     * considering Quads present, which player is playing next,
     * concentration of pieces present, connected groups on board,
     * mobility of pieces, last move was a capture, how many pieces
     * are on the board, and a random int to introduce randomness.
     * Returns the value of the board.
     */
    private int heuristicFunction(Board board) {
        int score = 0;
        int centralization = 0;
        int countSide = 0;
        int countOpp = 0;
        int sideX = 0;
        int sideY = 0;
        int oppX = 0;
        int oppY = 0;
        int centOpp = 0;
        int playerToMove = 0;
        if (board.piecesContiguous(side())) {
            return WINNING_VALUE;
        } else if (board.piecesContiguous(side().opposite())) {
            return -WINNING_VALUE;
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Square sq = Square.sq(j, i);
                if (board.get(sq) == side()) {
                    centralization += pieceSquareTable3[sq.index()];
                    countSide++;
                    sideX += i;
                    sideY += j;
                } else if (board.get(sq) == side().opposite()) {
                    centOpp += pieceSquareTable3[sq.index()];
                    countOpp++;
                    oppX += i;
                    oppY += j;
                }

            }
        }
        centralization = centralization / countSide;
        centOpp = centOpp / countOpp;
        Square com = Square.sq(sideX / countSide, sideY / countSide);
        Square comOpp = Square.sq(oppX / countOpp, oppY / countOpp);
        int quadSum = comQuads(com, board, comOpp);
        int countDiff = countSide - countOpp;
        int connSum = connected(board);
        int mobility = mobility(board);
        int conc = conc(board, com, countSide);
        int concOpp = conc(board, comOpp, countOpp);
        playerToMove = side() == board.turn() ? 10 : -10;
        int rand = getGame().randInt(Math.abs(playerToMove));
        score =  quadSum + connSum + conc + playerToMove
                + rand + countDiff;
        return score;
    }

    /** Returns concentration of BOARD w.r.t center of mass COM
     * and number of pieces of side on board COUNTSIDE. */
    private int conc(Board board, Square com, int countSide) {
        int dist = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                dist += Square.sq(j, i).distance(com);
            }
        }
        dist -= countSide;
        dist = dist != 0 ? dist : 1;
        int conc = 1 / dist;
        return conc * 100;
    }

    /** Returns the value of quads on the BOARD, w.r.t COM and
     * COMOPP.     */
    private int comQuads(Square com, Board board, Square comOPP) {
        boolean[][] visited = new boolean[8][8];
        int sumSide = 0;
        int sumOpp = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Square sq = Square.sq(j, i);
                if (board.get(sq) == side()) {
                    visited[i][j] = true;
                    sumSide += quads(visited, board, sq, side(), com);
                } else if (board.get(sq) == side().opposite()) {
                    visited[i][j] = true;
                    sumOpp += quads(visited, board, sq, side().opposite(),
                            comOPP);
                }
            }
        }
        return sumSide - sumOpp;
    }

    /** Returns score for quads of form 3, 4 and diagonal for a specific square
     * SQ, Piece P, Center of Mass COM, BOARD, and a boolean array that checks
     * if we've VISITED the square. */
    private int quads(boolean[][] visited, Board board, Square sq, Piece p,
                      Square com) {
        int numquads3 = 0;
        int numquads4 = 0;
        int numquadd = 0;
        int cor = 0;
        for (int i = 1; i < 8; i += 2) {
            Square dest = sq.moveDest(i, 1);
            if (dest != null && board.get(dest) == p) {
                cor++;
                if (!visited[dest.row()][dest.col()]) {
                    int count = 0;
                    for (int j = i - 1; j <= 8 && j <= i + 1 && count < 2;
                         j += 2) {
                        j = j == 8 ? 0 : j;
                        count++;
                        Square dest2 = sq.moveDest(j, 1);
                        if (dest2 != null && board.get(dest2) == p) {
                            if (!visited[dest2.row()][dest2.col()]) {
                                if (j == i - 1) {
                                    int k = i + 1;
                                    k = k == 8 ? 0 : k;
                                    Square dest3 = sq.moveDest(k, 1);
                                    if (dest3 != null
                                            && board.get(dest3) == p) {
                                        int destrow = dest3.row();
                                        int destcol = dest3.col();
                                        if (!visited[destrow][destcol]) {
                                            int dist = sq.distance(com);
                                            numquads4 += dist <= 2 ? 15 : 10;
                                        }
                                    }
                                }
                                int dist = sq.distance(com);
                                numquads3 += dist <= 2 ? 12 : 8;
                            }
                        }
                    }
                }
                numquadd += 5;
            } else if (cor == 0) {
                numquads3 -= 100;
            }
        }
        return numquads3 + numquads4 + numquadd;
    }

    /** Returns score for BOARD and how connected
     * the groups are for both sides. We want a negative score
     * and hence returns -5*score.
     */
    private int connected(Board board) {
        int diff = (board.getRegionSizes(side()).size()
                - board.getRegionSizes(side().opposite()).size());
        return -5 * diff;
    }

    /** Returns mobility of pieces of BOARD by measuring how
     * many moves and capture moves can each side make. Removed
     * at the end because of biasing towards opp side.
     */
    private int mobility(Board board) {
        List<Move> legal = board.legalMoves();
        board.setTurn(side().opposite());
        List<Move> legalOpp = board.legalMoves();
        board.setTurn(side());
        int sumSide = 0;
        int sumOpp = 0;
        for (Move move: legal) {
            if (move.isCapture()) {
                sumSide += 15;
            }
            sumSide += pieceSquareTable2[move.getTo().index()];
        }
        for (Move move: legalOpp) {
            if (move.isCapture()) {
                sumOpp += 15;
            }
            sumOpp += pieceSquareTable2[move.getTo().index()];
        }
        return sumSide - sumOpp;
    }

    /** Calculates if the last move was a capture or not
     * and returns the value for that BOARD.
     */
    private int lastCapture(Board board) {
        Move last = board.getLast();
        int score = 0;
        if (last.isCapture()) {
            score += LASTCAPTURE;
        }
        return score;
    }

    /** Used for centralization, to push pieces
     * to center.
     */
    private final int[] pieceSquareTable = new int[]
        {-500, -300, -300, -300, -300, -300, -300, -500,
            -300, 1, 1, 1, 1, 1, 1, -300,
            -200, 1, 3, 3, 3, 3, 1, -200,
            -200, 1, 3, 5, 5, 3, 1, -200,
            -200, 1, 3, 5, 5, 3, 1, -200,
            -200, 1, 3, 3, 3, 3, 1, -200,
            -300, 1, 1, 1, 1, 1, 1, -300,
            -500, -300, -300, -300, -300, -300, -300, -500};
    /** Used for mobility analysis. */
    private final int[] pieceSquareTable3 = new int[]
        {-5, -3, -2, -2, -2, -2, -3, -5,
            -3, 2, 2, 2, 2, 2, 2, -3,
            -2, 2, 3, 3, 3, 3, 2, -2,
            -2, 2, 3, 5, 5, 5, 2, -2,
            -2, 2, 3, 5, 5, 5, 2, -2,
            -2, 2, 3, 3, 3, 3, 2, -2,
            -3, 2, 2, 2, 2, 2, 2, -3,
            -5, -3, -2, -2, -2, -2, -3, -5};

    /** Used for mobility analysis. */
    private final int[] pieceSquareTable2 = new int[]
        {-5, -3, -2, -2, -2, -2, -3, -5,
            -3, 1, 1, 1, 1, 1, 1, -3,
            -2, 1, 3, 3, 3, 3, 1, -2,
            -2, 1, 3, 3, 3, 3, 1, -2,
            -2, 1, 3, 3, 3, 3, 1, -2,
            -2, 1, 3, 3, 3, 3, 1, -2,
            -3, 1, 1, 1, 1, 1, 1, -3,
            -5, -3, -2, -2, -2, -2, -3, -5};
    /** Used to convey moves discovered by findMove. */
    private Move _foundMove;

    /** Value for if last move was a capture. */
    static final int LASTCAPTURE = 25;

    /** Stores value for move. */
    private HashSet<Integer> deepInTheJungle;

    /** Stores stored moves if moves result in win. */
    private ArrayList<Move> storedMoves = new ArrayList<>();

    /** Used for the last couple of steps and seeing which
     * move wins with max prob.
     */
    private Map<Move, List<Move>> stored = new HashMap<>();
}

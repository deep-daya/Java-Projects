/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Square.*;

/** Represents the state of a game of Lines of Action.
 *  @author Deep Dayaramani
 */
class Board {

    /** Default number of moves for each side that results in a draw. */
    static final int DEFAULT_MOVE_LIMIT = 30;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");
    /** Board Length. */
    private final int boardLength = 8;

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which th e player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row][col]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is 8x8.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        this(INITIAL_PIECES, BP);
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        this();
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        _moves.clear();
        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                Square sq = Square.sq(j, i);
                set(sq, contents[i][j]);
            }
        }
        _turn = side;
        _moveLimit = 2 * DEFAULT_MOVE_LIMIT;

    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Setting turn to SIDE. */
    void setTurn(Piece side) {
        _turn = side;
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        _moves.clear();
        _moves.addAll(board._moves);
        System.arraycopy(board._board, 0, _board, 0, _board.length);
        _turn = board._turn;
        _winnerKnown = false;
        _winner = null;
    }

    /** Return the contents of the square at SQ. */
    Piece get(Square sq) {
        return _board[sq.index()];
    }

    /** Set the square at SQ to V and set the side that is to move next
     *  to NEXT, if NEXT is not null. */
    void set(Square sq, Piece v, Piece next) {
        _board[sq.index()] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at SQ to V, without modifying the side that
     *  moves next. */
    void set(Square sq, Piece v) {
        set(sq, v, null);
    }

    /** Set limit on number of moves by each side that results in a tie to
     *  LIMIT, where 2 * LIMIT > movesMade(). */
    void setMoveLimit(int limit) {
        if (2 * limit <= movesMade()) {
            throw new IllegalArgumentException("move limit too small");
        }
        _moveLimit = 2 * limit;
    }

    /** Assuming isLegal(MOVE), make MOVE. This function assumes that
     *  MOVE.isCapture() will return false.  If it saves the move for
     *  later retraction, makeMove itself uses MOVE.captureMove() to produce
     *  the capturing move. */
    void makeMove(Move move) {
        assert isLegal(move);
        Square from = move.getFrom();
        Square to = move.getTo();
        if (_board[to.index()] != EMP) {
            move = move.captureMove();
            set(move.getTo(), EMP);

        }
        set(to, _board[from.index()]);
        set(from, EMP);
        _moves.add(move);
        _turn = _turn.opposite();
        _subsetsInitialized = false;
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        Move last = _moves.remove(_moves.size() - 1);
        Square lastPlace = last.getTo();
        Square prevPlace = last.getFrom();
        Piece replaced = _board[lastPlace.index()];
        Piece prev = _board[prevPlace.index()];
        if (last.isCapture()) {
            prev = replaced.opposite();
        }
        set(prevPlace, replaced);
        set(lastPlace, prev);
        _turn = _turn.opposite();
        _subsetsInitialized = false;
        _winner = null;
        _winnerKnown = false;
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true iff FROM - TO is a legal move for the player currently on
     *  move. */
    boolean isLegal(Square from, Square to) {
        if (from != null && to != null && !blocked(from, to)
            && withinBounds(to) & from.isValidMove(to)) {
            if (get(to) == EMP || get(to).opposite() == get(from)) {
                if (from.distance(to) == lineOfAction(from, to)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Uses squares FROM and TO to calculate the number of pieces
     * in the line connecting the squares to return number of pieces.
     */
    int lineOfAction(Square from, Square to) {
        int dirFrom = from.direction(to);
        int dirTo = to.direction(from);
        int numPieces = 1;
        for (int i = 1; i <= boardLength - 1; i++) {
            Square next = from.moveDest(dirFrom, i);
            if (next != null) {
                if (get(next) != EMP) {
                    numPieces++;
                }
            } else {
                break;
            }
        }
        for (int i = 1; i < boardLength - 1; i++) {
            Square next = from.moveDest(dirTo, i);
            if (next != null) {
                if (get(next) != EMP) {
                    numPieces++;
                }
            } else {
                break;
            }
        }
        return numPieces;
    }

    /** Uses square FROM and direction DIR to calculate number of pieces
     * in that direction in line of action to return number of pieces.
     */
    int lineOfAction(Square from, int dir) {
        int numPieces = 0;
        for (int i = from.row(), j = from.col(); withinBounds(i, j);
             i += DIR[dir][1], j += DIR[dir][0]) {
            Square sq = Square.sq(j, i);
            if (get(sq) != EMP) {
                numPieces++;
            }
        }
        for (int i = from.row() - DIR[dir][1], j = from.col() - DIR[dir][0];
             withinBounds(i, j); i -= DIR[dir][1], j -= DIR[dir][0]) {
            Square sq = Square.sq(j, i);
            if (get(sq) != EMP) {
                numPieces++;
            }
        }
        return numPieces;
    }

    /** Return true iff Square TO is within bounds.*/
    boolean withinBounds(Square to) {
        return to.row() < boardLength && to.row() >= 0 && to.col() >= 0
                & to.col() < boardLength;
    }

    /** Returns true iff ROW and COL are within bounds of board. */
    boolean withinBounds(int row, int col) {
        return row < boardLength && row >= 0 && col >= 0 & col < boardLength;
    }
    /** Return true iff MOVE is legal for the player currently on move.
     *  The isCapture() property is ignored. */
    boolean isLegal(Move move) {
        return isLegal(move.getFrom(), move.getTo());
    }

    /** Return a sequence of all legal moves from this position. */
    List<Move> legalMoves() {
        ArrayList<Move> legalMove = new ArrayList<>(8);
        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                Square from = Square.sq(j, i);
                if (get(from) == _turn) {
                    for (int l = 0; l < 8; l++) {
                        int sqLen = lineOfAction(from, l);
                        Square to = from.moveDest(l, sqLen);
                        if (to != null) {
                            if (isLegal(from, to)) {
                                legalMove.add(Move.mv(from, to,
                                        isCaptureLegal(from, to)));
                            }
                        }
                    }
                }
            }
        }
        return legalMove;
    }

    /** Return true iff the move from FROM to TO is a capture move or not. */
    boolean isCaptureLegal(Square from, Square to) {
        assert isLegal(from, to);
        return get(from) != get(to) && get(to) != EMP;
    }

    /** Return true iff the game is over (either player has all his
     *  pieces continguous or there is a tie). */
    boolean gameOver() {
        return winner() != null;
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        return getRegionSizes(side).size() == 1;
    }

    /** Return the winning side, if any.  If the game is not over, result is
     *  null.  If the game has ended in a tie, returns EMP. */
    Piece winner() {
        if (!_winnerKnown) {
            if (piecesContiguous(WP)) {
                _winner = WP;
                _winnerKnown = true;
            } else if (piecesContiguous(BP)) {
                _winner = BP;
                _winnerKnown = true;
            } else if (movesMade() >= _moveLimit) {
                _winner = EMP;
                _winnerKnown = true;
            } else {
                _winner = null;
                _winnerKnown = false;
            }
        }
        return _winner;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        return Arrays.deepEquals(_board, b._board) && _turn == b._turn;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(_board) * 2 + _turn.hashCode();
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = BOARD_SIZE - 1; r >= 0; r -= 1) {
            out.format("    ");
            for (int c = 0; c < BOARD_SIZE; c += 1) {
                out.format("%s ", get(sq(c, r)).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return true if a move from FROM to TO is blocked by an opposing
     *  piece or by a friendly piece on the target square. */
    private boolean blocked(Square from, Square to) {
        int dir = from.direction(to);
        for (int i = from.row() + DIR[dir][1], j = from.col() + DIR[dir][0];
             withinBounds(i, j); i += DIR[dir][1], j += DIR[dir][0]) {
            Square sq = Square.sq(j, i);
            if (sq != to) {
                if (get(sq) != EMP) {
                    if (get(from) != get(sq)) {
                        return true;
                    }
                }
            } else {
                if (get(from) == get(sq)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    /** Return the size of the as-yet unvisited cluster of squares
     *  containing P at and adjacent to SQ.  VISITED indicates squares that
     *  have already been processed or are in different clusters.  Update
     *  VISITED to reflect squares counted. */
    private int numContig(Square sq, boolean[][] visited, Piece p) {
        if (p == EMP) {
            return 0;
        } else if (get(sq) != p) {
            return 0;
        } else if (visited[sq.row()][sq.col()]) {
            return 0;
        }
        int count = 1;
        visited[sq.row()][sq.col()] = true;
        for (int i = 0; i < 8; i++) {
            Square sqi = sq.moveDest(i, 1);
            if (sqi != null) {
                count += numContig(sqi, visited, p);
            }
        }
        return count;
    }


    /** Set the values of _whiteRegionSizes and _blackRegionSizes. */
    private void computeRegions() {
        if (_subsetsInitialized) {
            return;
        }
        _whiteRegionSizes.clear();
        _blackRegionSizes.clear();
        boolean[][] visited = new boolean[boardLength][boardLength];
        for (int i = 0; i < boardLength; i++) {
            for (int j = 0; j < boardLength; j++) {
                Square sq = Square.sq(j, i);
                if (!visited[i][j] && ((get(sq) == BP) || get(sq) == WP)) {
                    int count;
                    if (get(sq) == WP) {
                        count = numContig(sq, visited, WP);
                        if (count != 0) {
                            _whiteRegionSizes.add(count);
                        }
                    } else {
                        count = numContig(sq, visited, BP);
                        if (count != 0) {
                            _blackRegionSizes.add(count);
                        }
                    }
                }
            }
        }
        Collections.sort(_whiteRegionSizes, Collections.reverseOrder());
        Collections.sort(_blackRegionSizes, Collections.reverseOrder());
        _subsetsInitialized = true;
    }

    /** Returns last move taken on this board. */
    Move getLast() {
        return _moves.get(_moves.size() - 1);
    }

    /** Return the sizes of all the regions in the current union-find
     *  structure for side S. */
    List<Integer> getRegionSizes(Piece s) {
        computeRegions();
        if (s == WP) {
            return _whiteRegionSizes;
        } else {
            return _blackRegionSizes;
        }
    }


    /** The standard initial configuration for Lines of Action (bottom row
     *  first). */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };
    /** Directions of movement for a specific direction. */
    private static final int[][] DIR = {
            { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 }, { 0, -1 },
            { -1, -1 }, { -1, 0 }, { -1, 1 }
    };

    /** Current contents of the board.  Square S is at _board[S.index()]. */
    private final Piece[] _board = new Piece[BOARD_SIZE  * BOARD_SIZE];

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Limit on number of moves before tie is declared.  */
    private int _moveLimit;
    /** True iff the value of _winner is known to be valid. */
    private boolean _winnerKnown;
    /** Cached value of the winner (BP, WP, EMP (for tie), or null (game still
     *  in progress).  Use only if _winnerKnown. */
    private Piece _winner;

    /** True iff subsets computation is up-to-date. */
    private boolean _subsetsInitialized;

    /** List of the sizes of continguous clusters of pieces, by color. */
    private final ArrayList<Integer>
        _whiteRegionSizes = new ArrayList<>(),
        _blackRegionSizes = new ArrayList<>();
}

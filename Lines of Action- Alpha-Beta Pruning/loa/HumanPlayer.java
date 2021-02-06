/* Skeleton Copyright (C) 2015, 2020 Paul N. Hilfinger and the Regents of the
 * University of California.  All rights reserved. */
package loa;

/** A Player that prompts for moves and reads them from its Game.
 *  @author Deep Dayaramani
 */
class HumanPlayer extends Player {

    /** A new HumanPlayer with no piece or controller (intended to produce
     *  a template). */
    HumanPlayer() {
        this(null, null);
    }

    /** A HumanPlayer that plays the SIDE pieces in GAME.  It uses
     *  GAME.getMove() as a source of moves.  */
    HumanPlayer(Piece side, Game game) {
        super(side, game);
        _game = game;
        _side = side;
    }

    @Override
    String getMove() {
        String input = _game.readLine(true);
        return input;
    }

    @Override
    Player create(Piece piece, Game game) {
        return new HumanPlayer(piece, game);
    }

    /** Stores the game. */
    private final Game _game;

    /** Stores the Piece HumanPlayer is playing for. */
    private final Piece _side;
    @Override
    boolean isManual() {
        return true;
    }



}

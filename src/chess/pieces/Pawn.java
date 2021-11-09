package chess.pieces;

import board.Board;
import board.Position;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.Color;

public class Pawn extends ChessPiece {

    private ChessMatch chessMatch;

    public Pawn(Board board, Color color, ChessMatch chessMatch) {
        super(board, color);
        this.chessMatch = chessMatch;
    }

    @Override
    public String toString(){
        return "p";
    }

    @Override
    public boolean[][] possibleMoves() {
        boolean [][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];
        Position p = new Position(0, 0);
        Position p2 = new Position(0, 0);
        int oneMove, twoMoves, enPassantRow;


        if(getColor() == Color.WHITE){
            oneMove = -1;
            twoMoves = -2;
            enPassantRow = 3;
        }
        else {
            oneMove = 1;
            twoMoves = 2;
            enPassantRow = 4;
        }

        // Basic Pawn move
        p.setValues(position.getRow() + oneMove, position.getColumn());
        if(getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) mat[p.getRow()][p.getColumn()] = true;

        // First Pawn move
        p.setValues(position.getRow() + twoMoves, position.getColumn());
        p2.setValues(position.getRow() + oneMove, position.getColumn());
        if(getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)
                && getBoard().positionExists(p) && !getBoard().thereIsAPiece(p) && getMoveCount() == 0) {
            mat[p.getRow()][p.getColumn()] = true;
            mat[p2.getRow()][p2.getColumn()] = true;
        }

        // Capture to the right
        p.setValues(position.getRow() + oneMove, position.getColumn() - 1);
        if(getBoard().positionExists(p) && isThereOpponentPiece(p)) mat[p.getRow()][p.getColumn()] = true;

        // Capture to the right
        p.setValues(position.getRow() + oneMove, position.getColumn() + 1);
        if(getBoard().positionExists(p) && isThereOpponentPiece(p)) mat[p.getRow()][p.getColumn()] = true;

        // En passant
        if(position.getRow() == enPassantRow){

            // Opponent pawn on the left is vulnerable
            Position left = new Position(position.getRow(), position.getColumn() - 1);
            if(getBoard().positionExists(left) && isThereOpponentPiece(left) && getBoard().piece(left) == chessMatch.getEnPassantVulnerable()){
                mat[left.getRow() + oneMove][left.getColumn()] = true;
            }

            // Opponent pawn on the right is vulnerable
            Position right = new Position(position.getRow(), position.getColumn() + 1);
            if(getBoard().positionExists(right) && isThereOpponentPiece(right) && getBoard().piece(right) == chessMatch.getEnPassantVulnerable()){
                mat[left.getRow() + oneMove][left.getColumn()] = true;
            }
        }

        return mat;
    }
}

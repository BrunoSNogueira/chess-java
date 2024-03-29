package chess;

import board.Board;
import board.Piece;
import board.Position;
import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private final Board board;
    private boolean check;
    private boolean checkmate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;

    private final List<Piece> onBoardPieces = new ArrayList<>();
    private final List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch(){
        turn = 1;
        currentPlayer = Color.WHITE;
        board = new Board(8, 8);
        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCheck(){
        return check;
    }

    public boolean getCheckmate(){
        return checkmate;
    }

    public ChessPiece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted() {
        return promoted;
    }

    public ChessPiece[][] getPieces(){
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];

        for(int i = 0; i < board.getRows(); i++){
            for(int j = 0; j < board.getColumns(); j++){
                mat[i][j] = (ChessPiece) board.piece(i,j);
            }
        }
        return mat;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition){
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition){
        Position source = sourcePosition.toPosition();
        Position target = targetPosition.toPosition();
        validateSourcePosition(source);
        validateTargetPosition(source, target);
        Piece capturedPiece = makeMove(source, target);

        if(testCheck(currentPlayer) || testIllegalCastling(source, target)){
            undoMove(source, target, capturedPiece);
            throw new ChessException("You can't put yourself in check");
        }

        ChessPiece movedPiece = (ChessPiece) board.piece(target);

        // Pawn promotion
        promoted = null;
        if(movedPiece instanceof Pawn){
            if(target.getRow() == 0 || target.getRow() == 7){
                promoted = (ChessPiece) board.piece(target);
                promoted = replacePromotedPiece("Q");
            }
        }

        check = testCheck(opponent(currentPlayer));
        checkmate = testCheckmate(opponent(currentPlayer));

        if(checkmate) return null;
        nextTurn();

        // En passant
        if(movedPiece instanceof Pawn
                && (target.getRow() == source.getRow() + 2 || target.getRow() == source.getRow() - 2))
            enPassantVulnerable = movedPiece;

        return (ChessPiece)capturedPiece;
    }

    private boolean testIllegalCastling(Position source, Position target) {

        ChessPiece movedPiece = (ChessPiece) board.piece(target);

        boolean isIllegal = false;

        // position in-between
        Position inBet = new Position(source.getRow(),
                (source.getColumn() > target.getColumn()) ? source.getColumn() - 1 : target.getColumn() - 1);

        // identifies if a king has moved
        if (movedPiece instanceof King) {
            // identifies if a king has castled, once it moved two squares away
            if (Math.abs(source.getColumn() - target.getColumn()) == 2) {
                // checks if the position passed through by the king is attacked
                Piece capturedPiece = makeMove(target, inBet);

                if (testCheck(currentPlayer)) {
                    isIllegal = true;
                }


                undoMove(target, inBet, capturedPiece);
            }
        }
        return isIllegal;
    }


        public ChessPiece replacePromotedPiece(String type) {
        if(promoted == null) throw new IllegalStateException("There is no piece to be promoted");
        if(!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q") )
            return promoted;

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        onBoardPieces.remove(p);

        ChessPiece newPiece = newPiece(type, promoted.getColor());
        board.placePiece(newPiece, pos);
        onBoardPieces.add(newPiece);

        return newPiece;
    }

    private ChessPiece newPiece(String type, Color color){
        if(type.equals("B")) return new Bishop(board, color);
        if(type.equals("N")) return new Knight(board, color);
        if(type.equals("R")) return new Rook(board, color);
        return new Queen(board, color);
    }

    private Piece makeMove(Position source, Position target){
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);

        if(capturedPiece != null){
            onBoardPieces.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        // Castling
        // King's side
        if(p instanceof King && target.getColumn() == source.getColumn() + 2)     {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // Queen's side
        if(p instanceof King && target.getColumn() == source.getColumn() - 2)     {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // En passant
        if(p instanceof Pawn){
            if(source.getColumn() != target.getColumn() && capturedPiece == null){
                Position pawnPosition;
                if(p.getColor() == Color.WHITE) pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                else pawnPosition = new Position(target.getRow() - 1, target.getColumn());
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                onBoardPieces.remove(capturedPiece);
            }
        }

        board.placePiece(p, target);
        return capturedPiece;
    }

    private void undoMove(Position source, Position target, Piece capturedPiece){
        ChessPiece p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
        board.placePiece(p, source);

        if(capturedPiece != null){
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            onBoardPieces.add(capturedPiece);
        }

        // Castling
        // King's side
        if(p instanceof King && target.getColumn() == source.getColumn() + 2)     {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // Queen's side
        if(p instanceof King && target.getColumn() == source.getColumn() - 2)     {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // En passant
        if(p instanceof Pawn){
            if(source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable){
                ChessPiece pawn = (ChessPiece) board.removePiece(target);
                Position pawnPosition;
                if(p.getColor() == Color.WHITE) pawnPosition = new Position(3, target.getColumn());
                else pawnPosition = new Position(4, target.getColumn());
                board.placePiece(pawn, pawnPosition);
            }
        }
    }

    private void validateSourcePosition(Position position){
        if(!board.thereIsAPiece(position)) throw new ChessException("There is no piece on source position");
        if(currentPlayer != ((ChessPiece)board.piece(position)).getColor())
            throw new ChessException("Chosen piece color doesn't match the player");
        if(!board.piece(position).isThereAnyPossibleMove())
            throw new ChessException("There are no possible moves for the chosen piece");
    }

    private void validateTargetPosition(Position source, Position target) {
        if(!board.piece(source).possibleMove(target))
            throw new ChessException("Chosen piece can't move to target position");
    }

    private void nextTurn(){
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private void placeNewPiece(char column, int row, ChessPiece piece){
        board.placePiece(piece, new ChessPosition(row, column).toPosition());
        onBoardPieces.add(piece);
    }

    private Color opponent(Color color){
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color){
        List<Piece> list = onBoardPieces.stream()
                .filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());

        for (Piece p : list) {
            if(p instanceof King){
                return (ChessPiece) p;
            }
        }
        throw new IllegalStateException("There is no " + color + " king on the board.");
    }

    private boolean testCheck(Color color){
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = onBoardPieces.stream()
                .filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());

        for(Piece p : opponentPieces){
            boolean[][] mat = p.possibleMoves();
            if(mat[kingPosition.getRow()][kingPosition.getColumn()]) return true;
        }
        return false;
    }

    private boolean testCheckmate(Color color){
        if(!testCheck(color)) return false;

        // Creates a list of all pieces of the said color
        List<Piece> list = onBoardPieces.stream()
                .filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());

        for(Piece p : list){
            boolean[][] mat = p.possibleMoves();

            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if(mat[i][j]){
                        Position source = ((ChessPiece)p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, capturedPiece);
                        if(!testCheck) return false;
                    }
                }
            }
        }
        return true;
    }

    private void initialSetup(){
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
    }
}

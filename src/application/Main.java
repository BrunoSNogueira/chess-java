package application;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        ChessMatch chessMatch = new ChessMatch();
        List<ChessPiece> captured = new ArrayList<>();

        // Using this loop as placeholder until Check is implemented.
        while(true){
            try{
                UI.clearScreen();
                UI.printMatch(chessMatch, captured);
                System.out.print("\nSource: ");
                ChessPosition source = UI.readChessPosition(scan);

                boolean[][] possibleMoves = chessMatch.possibleMoves(source);
                UI.clearScreen();
                UI.printBoard(chessMatch.getPieces(), possibleMoves);

                System.out.print("\nTarget: ");
                ChessPosition target = UI.readChessPosition(scan);

                ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
                if (capturedPiece != null) captured.add(capturedPiece);
            }
            catch (ChessException e){
                System.out.println(e.getMessage());
                scan.nextLine();
            }
            catch (InputMismatchException e){
                System.out.println(e.getMessage());
                scan.nextLine();
            }
        }
    }
}

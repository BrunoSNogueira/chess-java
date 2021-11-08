package application;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        ChessMatch chessMatch = new ChessMatch();

        while(true){
            try{
                UI.clearScreen();
                UI.printBoard(chessMatch.getPieces());
                System.out.println("\nSource: ");
                ChessPosition source = UI.readChessPosition(scan);

                System.out.println("\nTarget: ");
                ChessPosition target = UI.readChessPosition(scan);

                // TO-DO display captured piece
                ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
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

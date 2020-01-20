import piece.*;

import java.util.ArrayList;
import java.util.List;

// represents a chess board
public class ChessBoard {
    private IPiece[][] board;
    private List<IPiece> whitePieces;
    private List<IPiece> blackPieces;
    private boolean whiteTurn;
    private List<IPiece[][]> history;
    private int movesSoFar;

    public ChessBoard() {
        this.whitePieces = new ArrayList<>();
        this.blackPieces = new ArrayList<>();
        this.board = generateChessBoard();
        this.whiteTurn = true;
        this.history = new ArrayList<>();
        this.movesSoFar = 0;
        this.recordHistory();
    }

    public ChessBoard(IPiece[][] board, boolean whiteTurn) {
        if (board.length != 8 || board[0].length != 8) {
            throw new IllegalArgumentException("Board is not regulation size");
        }
        this.whitePieces = new ArrayList<>();
        this.blackPieces = new ArrayList<>();
        this.board = board;
        this.whiteTurn = whiteTurn;
        this.history = new ArrayList<>();
        movesSoFar = 0;
    }

    public boolean playGame(int fromX, int fromY, int toX, int toY) {
        if (this.isValidMove(fromX, fromY, toX, toY)) {
            IPiece movedPiece = this.board[fromX][fromY];
            if (movedPiece instanceof King && Math.abs(fromX - toX) == 2) {
                this.executeCastle(fromX, toX, toY);
            }
            movedPiece.makeMove(toX, toY);
            this.removePiece(this.board[toX][toY]);
            this.board[toX][toY] = movedPiece;
            this.board[fromX][fromY] = null;
            this.nextTurn();
            return true;
        } else {
            return false;
        }
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 7; i > -1; i--) {
            for (int j = 0; j < 8; j++) {
                IPiece current = board[j][i];
                if (current == null)
                    sb.append("X ");
                else {
                    sb.append(current.toString());
                }
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public IPiece[][] getBoard() {
        IPiece[][] newBoard = new IPiece[8][8];

        for (IPiece p : this.whitePieces) {
            newBoard[p.getX()][p.getY()] = p.copy();
        }
        for (IPiece p : this.blackPieces) {
            newBoard[p.getX()][p.getY()] = p.copy();
        }
        return newBoard;
    }

    // Private Methods
    private IPiece[][] generateChessBoard() {
        IPiece[][] newBoard =
                {
                        {this.addPiece(new Rook(0, 0, false)), this.addPiece(new Pawn(0, 1, false)), null, null, null, null, this.addPiece(new Pawn(0, 6, true)), this.addPiece(new Rook(0, 7, true))},
                        {this.addPiece(new Knight(1, 0, false)), this.addPiece(new Pawn(1, 1, false)), null, null, null, null, this.addPiece(new Pawn(1, 6, true)), this.addPiece(new Knight(1, 7, true))},
                        {this.addPiece(new Bishop(2, 0, false)), this.addPiece(new Pawn(2, 1, false)), null, null, null, null, this.addPiece(new Pawn(2, 6, true)), this.addPiece(new Bishop(2, 7, true))},
                        {this.addPiece(new Queen(3, 0, false)), this.addPiece(new Pawn(3, 1, false)), null, null, null, null, this.addPiece(new Pawn(3, 6, true)), this.addPiece(new Queen(3, 7, true))},
                        {this.addPiece(new King(4, 0, false)), this.addPiece(new Pawn(4, 1, false)), null, null, null, null, this.addPiece(new Pawn(4, 6, true)), this.addPiece(new King(4, 7, true))},
                        {this.addPiece(new Bishop(5, 0, false)), this.addPiece(new Pawn(5, 1, false)), null, null, null, null, this.addPiece(new Pawn(5, 6, true)), this.addPiece(new Bishop(5, 7, true))},
                        {this.addPiece(new Knight(6, 0, false)), this.addPiece(new Pawn(6, 1, false)), null, null, null, null, this.addPiece(new Pawn(6, 6, true)), this.addPiece(new Knight(6, 7, true))},
                        {this.addPiece(new Rook(7, 0, false)), this.addPiece(new Pawn(7, 1, false)), null, null, null, null, this.addPiece(new Pawn(7, 6, true)), this.addPiece(new Rook(7, 7, true))}
                };
        return newBoard;
    }

    private void executeCastle(int fromX, int toX, int y) {
        int direction = toX - fromX;
        int fromCastleX = direction > 0 ? 7 : 0;
        int toCastleX = direction > 0 ? 5 : 3;

        if (isValidMove(fromCastleX, y, toCastleX, y)) {
            IPiece castle = this.board[fromCastleX][y];
            castle.makeMove(toCastleX, y);
            this.board[fromCastleX][y] = null;
            this.board[toCastleX][y] = castle;
        }
    }

    private IPiece addPiece(IPiece piece) {
        if (piece == null) throw new IllegalArgumentException("Piece cannot be null");
        (piece.getIsBlack() ? this.blackPieces : this.whitePieces).add(piece);
        return piece;
    }

    private boolean isInCheck(){
        int king_x;
        int king_y;
        for (IPiece x : (this.whiteTurn ? this.whitePieces : this.blackPieces) ){
            if ( x.toString().charAt(1) == 'K'){
                king_x = x.getX();
                king_y = x.getY();
                return isInDanger(king_x, king_y);
            }
        }
        return false;
    }

    private boolean isInDanger(int x, int y){
        for (IPiece p : (this.whiteTurn ? this.blackPieces : this.whitePieces) ){
            if ( this.isValidMove(p.getX(), p.getY(), x, y) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes sure:
     * - Both coordinates are inside board.
     * - From coordinate contains a piece.
     * - Coordinates are not the same.
     * - It is the correct player's turn.
     * - Not moving to a space occupied by space of same color.
     * - Delegates to individual piece logic.
     *
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @return
     */
    private boolean isValidMove(int fromX, int fromY, int toX, int toY) {
        if (!coordInsideBoard(fromX, fromY) || !coordInsideBoard(toX, toY)) {
            throw new IllegalArgumentException("Coordinate outside of board");
        } else {
            IPiece from = this.board[fromX][fromY];
            IPiece to = this.board[toX][toY];
            if (from == null) {
                throw new IllegalArgumentException("Must move a piece");
            } else if (fromX == toX && fromY == toY) {
                throw new IllegalArgumentException("Cannot move to same space");
            } else if (from.getIsBlack() && this.whiteTurn || !from.getIsBlack() && !this.whiteTurn) {
                throw new IllegalArgumentException("Other player's move");
            } else if (to != null && (from.getIsBlack() && to.getIsBlack() || !(from.getIsBlack() || to.getIsBlack()))) {
                throw new IllegalArgumentException("Cannot move to square occupied by piece of same color");
            } else if (!from.isValidMove(this.board, fromX, fromY, toX, toY)) {
                throw new IllegalArgumentException("Invalid move");
            }
            return true;
        }
    }

    private boolean coordInsideBoard(int x, int y) {
        return (x > -1 && x < 8) && (y > -1 && y < 8);
    }

    private void nextTurn() {
        this.whiteTurn = !this.whiteTurn;
        this.movesSoFar++;
        this.recordHistory();
    }

    private void recordHistory() {
        this.history.add(this.getBoard());
    }

    private void removePiece(IPiece p) {
        if (p != null) {
            (p.getIsBlack() ? this.blackPieces : this.whitePieces).remove(p);
        }
    }
}

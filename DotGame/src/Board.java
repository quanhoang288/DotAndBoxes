import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;

public class Board extends JPanel {

    private static final long serialVersionUID = 1L;
    public static final int HEIGHT = 800;
    public static final int WIDTH = (int) (HEIGHT * 0.8);
    public static final int SIZE = 6; // 3x3 board
    public static final int CELL = WIDTH / (SIZE + 2); // size of a box
    public static final int MARGIN = HEIGHT - (SIZE + 2) * CELL; // top margin
    public static final int DOT = CELL / 12; // radius of a dot

    public static final Color COLOR_COMP = Color.BLUE;
    public static final Color COLOR_COMP_HIGHLIGHT = Color.BLUE.brighter();
    public static final Color COLOR_PLAYER_HIGHLIGHT = Color.RED.brighter();
    public static final Color COLOR_PLAYER = Color.RED;

    private ArrayList<ArrayList<Box>> boxes;
    private ArrayList<Box> currentBoxes;
    private Game game;
    private int numSquaresFilled;
    private Box lastBox;
    private int timeComp;
    public Board(Game game) {
        this.game = game;
        super.setSize(WIDTH, HEIGHT);
        boxes = new ArrayList<>();
        currentBoxes = new ArrayList<>();
        for (int i = 0; i < SIZE; ++i) {
            boxes.add(new ArrayList<>());
            for (int j = 0; j < SIZE; ++j) {
                boxes.get(i).add(new Box(getX(j) + DOT / 2, getY(i) + DOT / 2, i, j));
            }
        }

    }

    public void paintComponent(Graphics g) {
        drawBoard(g);
        drawScore(g);
        //computerGo();
        drawBoxes(g);
        if (game.isGameOver())
            drawResult(g);

    }

    public void drawBoard(Graphics g) {
        // draw the board with all the dots
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);
        for (int i = 0; i < SIZE + 1; ++i) {
            for (int j = 0; j < SIZE + 1; ++j) {
                g.fillArc(getX(j), getY(i), DOT, DOT, 0, 360);
            }
        }
    }

    public void drawScore(Graphics g) {
        // display the score of 2 players
        g.setColor(COLOR_COMP);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Computer", WIDTH - 2 * CELL - 50, MARGIN / 4);
        g.drawString(Integer.toString(game.getCompScore()), WIDTH - 2 * CELL - 5, MARGIN / 4 + 30);
        g.setColor(COLOR_PLAYER);
        g.drawString("Player", 2 * CELL - 50, MARGIN / 4);
        g.drawString(Integer.toString(game.getPlayerScore()), 2 * CELL - 25, MARGIN / 4 + 30);
    }

    // public void computerGo() {

    //     if (game.isPlayerTurn() || timeComp > 0){
    //         timeComp--;
    //         return;
    //     }
    //     selectRandomSide();
    //     timeComp = 60;
    // }
    // public void selectRandomSide(){
    //     ArrayList<Box> candidates = new ArrayList<>();
    //     for (int i = 0; i < SIZE; ++i){
    //         for (int j = 0; j < SIZE; ++j){
    //             if (boxes.get(i).get(j).getNumOfSides() < 4 && boxes.get(i).get(j).getNumOfSides() != 2){
    //                 candidates.add(boxes.get(i).get(j));
    //             }
    //         }
    //     }
    //     Box.setPlayerTurn(game.isPlayerTurn());
    //     Box chosenBox = null;
    //     if (candidates.size() > 0){
    //         chosenBox = candidates.get((int)Math.floor(Math.random() * candidates.size())); 
    //     }
    //     else {
    //         for (int i = 0; i < SIZE; ++i){
    //             for (int j = 0; j < SIZE; ++j){
    //                 if (!boxes.get(i).get(j).allSidesDrawn()){
    //                     chosenBox = boxes.get(i).get(j);
    //                     break;
    //                 }
                    
    //             }
    //             if (chosenBox != null) break;
    //         }
                
    //     }
    //     if (chosenBox != null){
    //         int freeSide = chosenBox.getFreeSides().get(0);

    //         chosenBox.setCoords(freeSide);
    //         chosenBox.highlightSide();
    //         chosenBox.selectSide();
    //         game.setSideDrawn(true);
    //         if (chosenBox.allSidesDrawn()){
    //             numSquaresFilled++;
    //             game.setSquareFill(true);
    //         }
    
    //         Box neighbor = this.getNeighbor(chosenBox, freeSide);
    //         if (neighbor != null){
    //             this.drawNeighbor(neighbor, freeSide);
    //             if (neighbor.allSidesDrawn()) {
    //                 numSquaresFilled++;
    //                 game.setSquareFill(true);
    //             }
    //         }
    //     }

        
    // }
    public void drawNeighbor(Box neighbor, int side){
        //Box neighbor = this.getNeighbor(box, side);
        switch (side){
            case Box.LEFT:
                neighbor.setHighlight(Box.RIGHT);
                break;
            case Box.RIGHT:
                neighbor.setHighlight(Box.LEFT);
                break;
            case Box.TOP:
                neighbor.setHighlight(Box.BOT);
                break;
            case Box.BOT:
                neighbor.setHighlight(Box.TOP);
                break;
        }
        neighbor.selectSide();
        
    }
    public Box getNeighbor(Box box, int side){
        int row = Board.getRow(box.getTop());
        int col = Board.getCol(box.getLeft());
        if (side == Box.TOP && row > 0) return boxes.get(row - 1).get(col);
        else if (side == Box.LEFT && col > 0) return boxes.get(row).get(col - 1);
        else if (side == Box.RIGHT && col < SIZE - 1) return boxes.get(row).get(col + 1);
        else if (side == Box.BOT && row < SIZE - 1) return boxes.get(row + 1).get(col);
        return null;
    }

    public void drawBoxes(Graphics g){
        //draw sides and fill boxes

        for (int i = 0; i < SIZE; ++i){
            for (int j = 0; j < SIZE; ++j){
                boxes.get(i).get(j).drawSides(g);
                boxes.get(i).get(j).drawFill(g);
            }
        }
    }
    public void drawResult(Graphics g){
        //display final result
        String result;
        if (game.getPlayerScore() > game.getCompScore()) result = "Player";
        else if (game.getPlayerScore() < game.getCompScore()) result = "Computer";
        else result = "TIE";
        g.setColor(Color.BLACK);
        g.drawString(result, WIDTH/2 - 50, MARGIN/4 + 70);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        if (!result.equals("TIE")) g.drawString("WINS", WIDTH/2 - 30, MARGIN/4 + 70 + g.getFontMetrics().getHeight());
    }


    public int getX(int col){
        return CELL * (col + 1);
    }
    public int getY(int row){
        return MARGIN + CELL * row;
    }
    public static int getCol(int x){
        return (x - DOT/2)/CELL - 1;
    }
    public static int getRow(int y){
        return (y - DOT/2 - MARGIN)/CELL;
    }
    public void resetBoxes(){
        for (int i = 0; i < SIZE; ++i){
            for (int j = 0; j < SIZE; ++j)
                boxes.get(i).get(j).unhighlightSide();
        }
        currentBoxes = new ArrayList<>();
    }
    public void clearAll(){
        for (int i = 0; i < SIZE; ++i){
            for (int j = 0; j < SIZE; ++j)
                boxes.get(i).get(j).clearAll();
        }
    }

    public void checkMousePosition(int mouseX, int mouseY, boolean isHighlight, boolean isPlayerTurn){
        if (!game.isPlayerTurn()) return;
        resetBoxes();
        int row = Board.getRow(mouseY);
        int col = Board.getCol(mouseX);
        if (row < 0 || col < 0 || row >= SIZE || col >= SIZE) return;
        Box currentBox = boxes.get(row).get(col);
        Box.setPlayerTurn(isPlayerTurn);
        currentBox.setMouseX(mouseX);
        currentBox.setMouseY(mouseY);
        currentBox.highlightSide();
        if (currentBox.getHighlight() == -1) return;
        if (!isHighlight){
            game.setSideDrawn(true);
            currentBox.setLastSide(currentBox.getHighlight());
            Box neighbor = this.getNeighbor(currentBox, currentBox.getHighlight());
            if (neighbor != null){
                this.drawNeighbor(neighbor, currentBox.getHighlight());
                currentBoxes.add(neighbor);
            }
            currentBox.selectSide();
            currentBoxes.add(currentBox);
            
            for (Box box : currentBoxes){
                if (box.allSidesDrawn()){
                    numSquaresFilled++;
                    game.setSquareFill(true);
                }                           
            }
        }
        lastBox = currentBox;
        
        
    }

    public int getNumSquaresFilled() {
        return numSquaresFilled;
    }

    public void setNumSquaresFilled(int numSquaresFilled) {
        this.numSquaresFilled = numSquaresFilled;
    }

    public int getTimeComp() {
        return timeComp;
    }

    public void setTimeComp(int timeComp) {
        this.timeComp = timeComp;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public ArrayList<ArrayList<Box>> getBoxes() {
        return boxes;
    }

    public void setBoxes(ArrayList<ArrayList<Box>> boxes) {
        this.boxes = boxes;
    }

    public Box getLastBox() {
        return lastBox;
    }

    public void setLastBox(Box lastBox) {
        this.lastBox = lastBox;
    }

}

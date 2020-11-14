import javax.swing.JPanel;
import java.awt.*;
import java.util.ArrayList;

public class Board extends JPanel {

    private static final long serialVersionUID = 1L;
    public static final int HEIGHT = 800;
    public static final int WIDTH = (int) (HEIGHT * 0.8);
    public static final int SIZE = 4; // 3x3 board
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
    private int timeComp;
    public Board(Game game) {
        this.game = game;
        super.setSize(WIDTH, HEIGHT);
        boxes = new ArrayList<>();
        currentBoxes = new ArrayList<>();
        for (int i = 0; i < SIZE; ++i) {
            boxes.add(new ArrayList<>());
            for (int j = 0; j < SIZE; ++j) {
                boxes.get(i).add(new Box(getX(j) + DOT / 2, getY(i) + DOT / 2));
            }
        }
        // for (int i = 0; i < SIZE; ++i){
        // for (int j = 0; j < SIZE; ++j){
        // System.out.println("Row: " + this.getRow(boxes.get(i).get(j).getTop()) + ",
        // Col: " + this.getCol(boxes.get(i).get(j).getLeft()));
        // }
        // }
    }

    public void paintComponent(Graphics g) {
        drawBoard(g);
        drawScore(g);
        computerGo();
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

    public void computerGo() {

        if (game.isPlayerTurn() || timeComp > 0){
            timeComp--;
            return;
        }
        // System.out.println("Computer turn");

        selectRandomSide();
        timeComp = 60;
    }
    public void selectRandomSide(){
        ArrayList<Box> candidates = new ArrayList<>();
        for (int i = 0; i < SIZE; ++i){
            for (int j = 0; j < SIZE; ++j){
                if (boxes.get(i).get(j).getNumOfSides() < 4 && boxes.get(i).get(j).getNumOfSides() != 2){
                    candidates.add(boxes.get(i).get(j));
                }
            }
        }
        Box.setPlayerTurn(game.isPlayerTurn());
        if (candidates.size() > 0){
            Box chosenBox = candidates.get((int)Math.floor(Math.random() * candidates.size())); 
            //System.out.println("Player Turn: " + Box.isPlayerTurn());
            // todo select a random side
            //System.out.println("Row of chosen box: " + this.getRow(chosenBox.getTop()) + ", Col of chosen box: " + this.getCol(chosenBox.getLeft()));
            int freeSide = chosenBox.getFreeSide();
            chosenBox.setCoords(freeSide);
            chosenBox.highlightSide();
            chosenBox.selectSide();
            if (chosenBox.allSidesDrawn())
                game.setCompScore(game.getCompScore() + 1);
            else game.setPlayerTurn(!game.isPlayerTurn());
            this.drawNeighbor(chosenBox, freeSide);
        }
        else {
            for (int i = 0; i < SIZE; ++i){
                for (int j = 0; j < SIZE; ++j){
                    if (!boxes.get(i).get(j).allSidesDrawn()){
                        int side = boxes.get(i).get(j).getFreeSide();
                        boxes.get(i).get(j).setCoords(side);
                        boxes.get(i).get(j).highlightSide();
                        boxes.get(i).get(j).selectSide();
                        if (boxes.get(i).get(j).allSidesDrawn())
                            game.setCompScore(game.getCompScore() + 1);
                        else game.setPlayerTurn(true);
                        drawNeighbor(boxes.get(i).get(j), side);
                        return;
                    }
                    
                }
            }
                
        }
        
    }
    public void drawNeighbor(Box box, int side){
        Box neighbor = this.getNeighbor(box, side);
        if (neighbor == null) return;
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
        if (neighbor.allSidesDrawn()) game.setCompScore(game.getCompScore() + 1);
        else if (!box.allSidesDrawn()) game.setPlayerTurn(true);
        
    }
    public Box getNeighbor(Box box, int side){
        int row = this.getRow(box.getTop());
        int col = this.getCol(box.getLeft());
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
    public void drawSides(Box box){
        // todo
    }

    public int getX(int col){
        return CELL * (col + 1);
    }
    public int getY(int row){
        return MARGIN + CELL * row;
    }
    public int getCol(int x){
        return (x - DOT/2)/CELL - 1;
    }
    public int getRow(int y){
        return (y - DOT/2 - MARGIN)/CELL;
    }
    public void resetBoxes(){
        for (int i = 0; i < SIZE; ++i){
            for (int j = 0; j < SIZE; ++j)
                boxes.get(i).get(j).unhighlightSide();
        }
        currentBoxes = new ArrayList<>();
        game.setSquareFill(false);
    }
    public void clearAll(){
        game.setPlayerScore(0);
        game.setCompScore(0);
        game.setSideDrawn(false);
        game.setSquareFill(false);
        for (int i = 0; i < SIZE; ++i){
            for (int j = 0; j < SIZE; ++j)
                boxes.get(i).get(j).clearAll();
        }
    }

    public void checkMousePosition(int mouseX, int mouseY, boolean isHighlight, boolean isPlayerTurn){
        if (!game.isPlayerTurn()) return;
        resetBoxes();
        for (int i = 0; i < SIZE; ++i){
            for (int j = 0; j < SIZE; ++j){
                if (boxes.get(i).get(j).contains(mouseX, mouseY)){
                    //System.out.println("Position (" + mouseX + ", " + mouseY + ") in box (" + i + ", " + j + ')');
                    Box.setPlayerTurn(isPlayerTurn);
                    boxes.get(i).get(j).setMouseX(mouseX);
                    boxes.get(i).get(j).setMouseY(mouseY);
                    boxes.get(i).get(j).highlightSide(); // highlight a side if not already selected
                    if (boxes.get(i).get(j).getHighlight() == -1) return;
                    else currentBoxes.add(boxes.get(i).get(j));
                    // if (!boxes.get(i).get(j).allSidesDrawn()){
                    //     currentBoxes.add(boxes.get(i).get(j));
                    // }
                    // if (i > 0 && !boxes.get(i - 1).get(j).allSidesDrawn()){
                    //     currentBoxes.add(boxes.get(i - 1).get(j));
                    // }
                    // if (i < SIZE - 1 && !boxes.get(i + 1).get(j).allSidesDrawn()){
                    //     currentBoxes.add(boxes.get(i + 1).get(j));
                    // }
                    // if (j > 0 && !boxes.get(i).get(j - 1).allSidesDrawn()){
                    //     currentBoxes.add(boxes.get(i).get(j - 1));
                    // }
                    // if (j < SIZE - 1 && !boxes.get(i).get(j + 1).allSidesDrawn()){
                    //     currentBoxes.add(boxes.get(i).get(j + 1));
                    // }
                    if (!isHighlight) {
                        if (i > 0 && boxes.get(i).get(j).getHighlight() == Box.TOP){
                            boxes.get(i - 1).get(j).setHighlight(Box.BOT); // check for left neighbor
                            boxes.get(i - 1).get(j).selectSide();
                            currentBoxes.add(boxes.get(i - 1).get(j));
                            
                        }
                        else if (i < SIZE - 1 && boxes.get(i).get(j).getHighlight() == Box.BOT){
                            boxes.get(i + 1).get(j).setHighlight(Box.TOP); // check for right neighbor
                            boxes.get(i + 1).get(j).selectSide();
                            currentBoxes.add(boxes.get(i + 1).get(j));
                        }
                        else if (j > 0 && boxes.get(i).get(j).getHighlight() == Box.LEFT){
                            boxes.get(i).get(j - 1).setHighlight(Box.RIGHT); // check for top neighbor
                            boxes.get(i).get(j - 1).selectSide();
                            currentBoxes.add(boxes.get(i).get(j - 1));
                        }
                        else if (j < SIZE - 1 && boxes.get(i).get(j).getHighlight() == Box.RIGHT){
                            boxes.get(i).get(j + 1).setHighlight(Box.LEFT); // check for bottom neighbor
                            boxes.get(i).get(j + 1).selectSide();
                            currentBoxes.add(boxes.get(i).get(j + 1));
                            
                        }
                        game.setSideDrawn(boxes.get(i).get(j).selectSide()); // if click then fill that side  
                        for (Box box : currentBoxes){
                            //System.out.println(box.allSidesDrawn());
                            if (box.allSidesDrawn()){
                                numSquaresFilled++;
                                game.setSquareFill(true);
                            } 
                            
                        }
                    }
                    return;
                }
                
            }
        }
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

}

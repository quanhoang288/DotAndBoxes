import java.awt.*;
public class Box {
    public static final int THRESHOLD = Board.CELL/5;
    public static final int LEFT = 0, RIGHT = 1, TOP = 2, BOT = 3;
    private int left, right, top, bot, mouseX, mouseY;
    private boolean[] isDrawn;
    private boolean[] ownedByPlayer;
    private int highlight;
    private boolean owner;
    private static boolean playerTurn;
    private int numOfSides;
    public Box(int x, int y){
        isDrawn = new boolean[4];
        ownedByPlayer = new boolean[4];
        highlight = -1;
        this.left = x;
        this.top = y;
        this.right = x + Board.CELL;
        this.bot = y + Board.CELL;
    }
    public boolean contains(int x, int y){
        return x >= this.left && x < this.right &&  y >= this.top && y < this.bot;
    }
    public void drawFill(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        if (!this.allSidesDrawn()) return;
        if (owner) //player owns this box
            g2d.setColor(Board.COLOR_PLAYER);
        else
            g2d.setColor(Board.COLOR_COMP);  
        g2d.fillRect(this.left + 4, this.top + 4, Board.CELL - 8 , Board.CELL - 8);
        
        // g2d.setColor(Color.BLACK);
        // g2d.setStroke(new BasicStroke(4f));
        // g2d.drawRect(this.left, this.top, Board.CELL, Board.CELL);
    }
    public void drawSide(Graphics g, int edge, boolean isHighlight){
        Graphics2D g2d = (Graphics2D) g;
        if (isHighlight){
            float[] dash = {2f, 0f, 2f};
            if (playerTurn)
                g2d.setColor(Board.COLOR_PLAYER_HIGHLIGHT);
            else
                g2d.setColor(Board.COLOR_COMP_HIGHLIGHT);  
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dash, 2f));
            switch (edge){
                case LEFT:
                    g2d.drawLine(this.left, this.top, this.left, this.bot);
                    break;
                case RIGHT:
                    g2d.drawLine(this.right, this.top, this.right, this.bot);
                    break;
                case TOP:
                    g2d.drawLine(this.left, this.top, this.right, this.top);
                    break;
                case BOT:
                    g2d.drawLine(this.left, this.bot, this.right, this.bot);
                    break;
            }
        }
        else{
            g2d.setStroke(new BasicStroke(2f));
            switch (edge){
                case LEFT:
                    g2d.setColor(this.getColor(LEFT));
                    g2d.drawLine(this.left, this.top, this.left, this.bot);
                    break;
                case RIGHT:
                    g2d.setColor(this.getColor(RIGHT));
                    g2d.drawLine(this.right, this.top, this.right, this.bot);
                    break;
                case TOP:
                    g2d.setColor(this.getColor(TOP));
                    g2d.drawLine(this.left, this.top, this.right, this.top);
                    break;
                case BOT:
                    g2d.setColor(this.getColor(BOT));
                    g2d.drawLine(this.left, this.bot, this.right, this.bot);
                    break;
            }
            
            
        }
        
         
    }
    public void highlightSide(){
        // System.out.println("Highlighting a side");
        //setPlayerTurn(playerTurn);
        int disLeft = Math.abs(mouseX - this.left);
        int disRight = Math.abs(mouseX - this.right); 
        int disTop = Math.abs(mouseY - this.top);
        int disBot = Math.abs(mouseY - this.bot);
        int minDis = Math.min(disLeft, Math.min(disRight, Math.min(disTop, disBot)));
        if (minDis <= THRESHOLD){
            if (minDis == disLeft && !isDrawn[LEFT]) this.highlight = LEFT;
                
            else if (minDis == disRight && !isDrawn[RIGHT]) this.highlight = RIGHT;
            else if (minDis == disTop && !isDrawn[TOP]) this.highlight = TOP;
            else if (minDis == disBot && !isDrawn[BOT]) this.highlight = BOT;
            
        }
    }
    public void unhighlightSide(){
        this.highlight = -1;
    }
    public boolean selectSide(){
        if (this.highlight == -1) return false;
        // System.out.println("Selecting a side");
        numOfSides++;
        switch (this.highlight){
            
            case LEFT: 
                isDrawn[LEFT] = true;
                ownedByPlayer[LEFT] = playerTurn;
                break;
            case RIGHT: 
                isDrawn[RIGHT] = true;
                ownedByPlayer[RIGHT] = playerTurn;
                break;
            case TOP: 
                isDrawn[TOP] = true;
                ownedByPlayer[TOP] = playerTurn;
                break;
            case BOT: 
                isDrawn[BOT] = true;
                ownedByPlayer[BOT] = playerTurn;
                break;
        }
        this.highlight = -1;
        // if (!this.allSidesDrawn()) Game.setPlayerTurn(!playerTurn);
        // else this.owner = playerTurn; 
        if (this.allSidesDrawn()) this.owner = playerTurn;
        return true;
        // System.out.println("Player turn: " + Game.isPlayerTurn());
        
    }
    public void drawSides(Graphics g){
        if (this.highlight != -1) drawSide(g, this.highlight, true);
        if (isDrawn[LEFT]) this.drawSide(g, LEFT, false);
        if (isDrawn[RIGHT]) this.drawSide(g, RIGHT, false);
        if (isDrawn[TOP]) this.drawSide(g, TOP, false);
        if (isDrawn[BOT]) this.drawSide(g, BOT, false);

    }
    public Color getColor(int edge){
        Color res = null;
        
        if (edge == LEFT){
            if (ownedByPlayer[LEFT]) res = Board.COLOR_PLAYER;
            else res = Board.COLOR_COMP;
        }
        else if (edge == RIGHT){
            if (ownedByPlayer[RIGHT]) res = Board.COLOR_PLAYER;
            else res = Board.COLOR_COMP;
        }
        else if (edge == TOP){
            if (ownedByPlayer[TOP]) res = Board.COLOR_PLAYER;
            else res = Board.COLOR_COMP;
        }    
        else{
            if (ownedByPlayer[BOT]) res = Board.COLOR_PLAYER;
            else res = Board.COLOR_COMP;
        }
            
        return res;
    }
    public void clearAll(){
        for (int i = 0; i < 4; ++i) isDrawn[i] = false;
        numOfSides = 0;
    }
    public boolean allSidesDrawn(){
        for (int i = 0; i < 4; ++i)
            if (!isDrawn[i]) return false;
        return true;
    }
    public void setCoords(int edge){
        switch(edge){
            case(LEFT):
                mouseX = this.left + 1;
                mouseY = this.top + Board.CELL/2;
                break;
            case(RIGHT):
                mouseX = this.right + 1;
                mouseY = this.top + Board.CELL/2;
                break;
            case(TOP):
                mouseX = this.left + Board.CELL/2;
                mouseY = this.top + 1;
                break;
            case(BOT):
                mouseX = this.left + Board.CELL/2;
                mouseY = this.bot + 1;
                break;
        }         
    }
    public int getFreeSide(){
        if (!isDrawn[TOP]) return TOP;
        if (!isDrawn[LEFT]) return LEFT;
        if (!isDrawn[RIGHT]) return RIGHT;
        return BOT;
    }


    public int getMouseX() {
        return mouseX;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

    public int getHighlight() {
        return highlight;
    }

    public void setHighlight(int highlight) {
        this.highlight = highlight;
    }

    public static boolean isPlayerTurn() {
        return playerTurn;
    }

    public static void setPlayerTurn(boolean playerTurn) {
        Box.playerTurn = playerTurn;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public int getNumOfSides() {
        return numOfSides;
    }

    public void setNumOfSides(int numOfSides) {
        this.numOfSides = numOfSides;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    // @Override
    // public String toString() {
    //     return "Box []";
    // }


}

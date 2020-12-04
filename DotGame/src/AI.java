import java.util.ArrayList;

public class AI {
    GameState gamestate;
    Game game;
    Board board;
    int delay = 60;
    public AI(){

    }
    public AI(Game game, Board board){
        this.game = game;
        this.gamestate = game.getGamestate();
        this.board = board;
    }
    public void makeMove(){
        
        if (game.isPlayerTurn() || delay > 0){
            delay--;
            return;
        }
        selectRandomSide();
        delay = 60;
    }
    public ArrayList<Box> listMoves(){
        return null;
    }

    public void selectRandomSide(){
        ArrayList<Box> candidates = new ArrayList<>();
        for (int i = 0; i < Board.SIZE; ++i){
            for (int j = 0; j < Board.SIZE; ++j){
                if (board.getBoxes().get(i).get(j).getNumOfSides() < 4 && board.getBoxes().get(i).get(j).getNumOfSides() != 2){
                    candidates.add(board.getBoxes().get(i).get(j));
                }
            }
        }

        Box chosenBox = null;
        if (candidates.size() > 0){
            chosenBox = candidates.get((int)Math.floor(Math.random() * candidates.size())); 
        }
        else {
            for (int i = 0; i < Board.SIZE; ++i){
                for (int j = 0; j < Board.SIZE; ++j){
                    if (!board.getBoxes().get(i).get(j).allSidesDrawn()){
                        chosenBox = board.getBoxes().get(i).get(j);
                        break;
                    }
                    
                }
                if (chosenBox != null) break;
            }
                
        }
        // if (chosenBox != null){
        //     int freeSide = chosenBox.getFreeSides().get(0);

        //     chosenBox.setCoords(freeSide);
        //     chosenBox.highlightSide();
        //     chosenBox.selectSide();
        //     chosenBox.setLastSide(freeSide);
        //     board.setLastBox(chosenBox);
        //     game.setSideDrawn(true);
        //     if (chosenBox.allSidesDrawn()){
        //         board.setNumSquaresFilled(board.getNumSquaresFilled() + 1); 
        //         game.setSquareFill(true);
        //     }
    
        //     Box neighbor = board.getNeighbor(chosenBox, freeSide);
        //     if (neighbor != null){
        //         board.drawNeighbor(neighbor, freeSide);
        //         neighbor.setLastSide(freeSide);
        //         if (neighbor.allSidesDrawn()) {
        //             board.setNumSquaresFilled(board.getNumSquaresFilled() + 1);
        //             game.setSquareFill(true);
        //         }
        //     }
        // }
        if (chosenBox != null){
            int side = chosenBox.getFreeSides().get(0);
            drawBox(chosenBox, side);
        }

        
    }
    public void drawBox(Box box, int side){
        Box.setPlayerTurn(game.isPlayerTurn());
        if (box == null) return;
        box.setCoords(side);
        box.highlightSide();
        box.selectSide();
        box.setLastSide(side);
        board.setLastBox(box);
        game.setSideDrawn(true);
        if (box.allSidesDrawn()){
            board.setNumSquaresFilled(board.getNumSquaresFilled() + 1); 
            game.setSquareFill(true);
        }

        Box neighbor = board.getNeighbor(box, side);
        if (neighbor != null){
            board.drawNeighbor(neighbor, side);
            neighbor.setLastSide(side);
            if (neighbor.allSidesDrawn()) {
                board.setNumSquaresFilled(board.getNumSquaresFilled() + 1);
                game.setSquareFill(true);
            }
        }
    }
    public GameState getGamestate() {
        return gamestate;
    }

    public void setGamestate(GameState gamestate) {
        this.gamestate = gamestate;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }





}
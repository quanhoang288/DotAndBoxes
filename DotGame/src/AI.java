import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
        
        if (game.isPlayerTurn() || delay > 0 || game.isGameOver()){
            delay--;
            return;
        }
        ArrayList<ArrayList<Box> > doubleHandouts = gamestate.getDoubleHandouts();
        ArrayList<Box> handouts = gamestate.getSingleHandouts();
        ArrayList<Box> freeMoves = gamestate.getFreeMoves();
        if (doubleHandouts.size() > 0){
            System.out.println("double handouts");
            ArrayList<Box> handout = doubleHandouts.get(0);
            Box box = handout.get(0);
            int side = box.getFreeSides().get(0);
            drawBox(box, side);
        }
        else if (handouts.size() > 0){
            System.out.println("single handouts");
            Box box = handouts.get(0);
            ArrayList<Box> brokenChain = gamestate.findStructure("chains", box);
            if (brokenChain != null && brokenChain.size() == 2){
                for (Box bx : brokenChain) System.out.println(bx);
                makeDoubleDealingMove(box);
            }
            else{
                int side = box.getFreeSides().get(0);
                drawBox(box, side);
            }
        }
        
        else if (freeMoves.size() > 0){
            System.out.println("free moves");
            Random r = new Random();
            Box neighbor = null;
            Box box = null;
            int side = -1;

            do{
                box = freeMoves.get(r.nextInt(freeMoves.size()));
                ArrayList<Integer> sides = box.getFreeSides();
                side = sides.get(r.nextInt(sides.size()));
                neighbor = gamestate.getNeighbor(box, side);

                if (neighbor == null) break;
            }while (neighbor.getNumOfSides() > 1);
            System.out.println(box);
            
            drawBox(box, side);

        }
        else{
            System.out.println("performing minimax");
            findBestMove();
            // ArrayList<GameState> children = gamestate.listMoves();
            // for (GameState child : children) child.printInfo();
            // System.out.println("Children: " + children.size());
            // // for (GameState child : children) child.printInfo();
            // // System.out.println("root:");
            // gamestate.printInfo();
            // int[] scores = new int[children.size()];

            // for (int i = 0; i < children.size(); ++i){
            //     scores[i] = minimax(1, children.get(i), true, 3);
            // }

            // int bestVal = 1000;
            // int bestIndex = -1;
            // for (int i = 0; i < children.size(); ++i){
            //     if (scores[i] < bestVal){
            //         bestVal = scores[i];
            //         bestIndex = i;
            //         System.out.println("Best index: " + bestIndex);
            //     }
            // }
            // //for (int i = 0; i < children.size(); ++i) System.out.println(scores[i]);
            // GameState nextState = children.get(bestIndex);
            // nextState.printInfo();
            // Box nextBox = nextState.getLastBox();
            // int nextSide = nextState.getLastSide();
            // drawBox(nextBox, nextSide);
            //selectRandomSide();

        }
            //selectRandomSide();
        delay = 60;
    }

    public void findBestMove(){
        ArrayList<GameState> children = gamestate.listMoves();
        int[] scores = new int[children.size()];

        for (int i = 0; i < children.size(); ++i){
            scores[i] = minimax(1, children.get(i), true, 2);
        }

        int bestVal = 1000;
        int bestIndex = -1;
        for (int i = 0; i < children.size(); ++i){
            if (scores[i] < bestVal){
                bestVal = scores[i];
                bestIndex = i;
            }
        }
        System.out.println("Best Index: " + bestIndex);

        GameState nextState = children.get(bestIndex);
        // System.out.println("Original state: ");
        // gamestate.printInfo();
        // System.out.println("Next state");
        // nextState.printInfo();
        Box nextBox = nextState.getLastBox();
        int row = nextBox.getRow();
        int col = nextBox.getCol();
        int nextSide = nextState.getLastSide();
        Box box = gamestate.getBoxes().get(row).get(col);
        System.out.println("Chosen box: " + box);
        System.out.println("free side: " + nextSide);
        drawBox(box, nextSide);
    }

    public int minimax(int depth, GameState g, boolean isPlayer, int maxDepth){
        if (depth == maxDepth || g.isGameOver()){
            return evaluate(g);
        }
        ArrayList<GameState> listMoves = g.listMoves();
        if (isPlayer){
            int bestVal = -1000;
            for (GameState move : listMoves){
                bestVal = Math.max(bestVal, minimax(depth + 1, move, !isPlayer, maxDepth));
            }
            return bestVal;
            

        }
        else{
            int bestVal = 1000;
            for (GameState move : listMoves){
                bestVal = Math.min(bestVal, minimax(depth + 1, move, isPlayer, maxDepth));
            }
            return bestVal;
        }
    }
    public int evaluate(GameState g){
        return g.getPlayerScore() - g.getCompScore();
    }
    public void makeDoubleDealingMove(Box box){
        int side = box.getFreeSides().get(0);
        Box neighbor = gamestate.getNeighbor(box, side);
        int firstSide = neighbor.getFreeSides().get(0);
        int secondSide = neighbor.getFreeSides().get(1);
        if (Box.getOpposite(side) != firstSide) drawBox(neighbor, firstSide);
        else drawBox(neighbor, secondSide);
    }


    public void selectRandomSide(){
        System.out.println("selecting random side...");
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
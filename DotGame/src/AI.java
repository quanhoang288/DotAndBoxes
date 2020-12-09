import java.util.ArrayList;
import java.util.Random;


public class AI {
    GameState gamestate;
    Game game;
    Board board;
    int delay = 60;
    int cnt = 0;
    int randNum = 4;
    long time = 0;
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
        System.out.println("Looney stage: " + gamestate.isLooney());
        
        ArrayList<ArrayList<Box>> doubleHandouts = gamestate.getDoubleHandouts();
        ArrayList<Box> handouts = gamestate.getSingleHandouts();
        ArrayList<Box> freeMoves = gamestate.getFreeMoves();
        ArrayList<Box> joints = gamestate.getJoints();
        ArrayList<Box> length1Chains = gamestate.getLength1Chains();
        ArrayList<ArrayList<Box> > length2Chains = gamestate.getStructures().get("2-chains");
        ArrayList<ArrayList<Box> > chains = gamestate.getStructures().get("chains");
        ArrayList<ArrayList<Box> > loops = gamestate.getStructures().get("loops");
        System.out.println("Free moves: " + freeMoves.size());
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
            ArrayList<Box> brokenLoop = gamestate.findStructure("loops", box);
            ArrayList<Box> length2Broken = gamestate.findStructure("2-chains", box);
            if (brokenChain != null){
                System.out.println("chain length > 2");
                int side = box.getFreeSides().get(0);
                drawBox(box, side);        
            }
            else if(length2Broken != null){
                System.out.println("length 2 chain");
                if (!gamestate.isLooney()){
                    int side = box.getFreeSides().get(0);
                    drawBox(box, side);
                }
                else if (Board.SIZE * Board.SIZE - gamestate.getPlayerScore() - gamestate.getCompScore() == 2){
                    int side = box.getFreeSides().get(0);
                    drawBox(box, side);
                }
                else{
                    makeDoubleDealingMove(box);
                }
            }
            else if (brokenLoop != null){
                System.out.println("broken loop");
                if (brokenLoop.size() > 4){
                    int side = box.getFreeSides().get(0);
                    drawBox(box, side);
                }
                else{
                    if (Board.SIZE * Board.SIZE - gamestate.getPlayerScore() - gamestate.getCompScore() == 4){
                        int side = box.getFreeSides().get(0);
                        drawBox(box, side);
                    }
                    else
                        makeDoubleDealingMoveLoop(box);
                    //findBestMove();
                }
                    

            }
            else{
                int side = box.getFreeSides().get(0);
                drawBox(box, side);
            }
        }
        
        else if (freeMoves.size() > 0 && randNum > 0){
            randNum--;
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
            // System.out.println("random");
            // ArrayList<GameState> children = gamestate.listMoves();
            // System.out.println("root: ");
            // gamestate.printInfo();
            // System.out.println("children: " + children.size());
            // for (GameState child : children) child.printInfo();
            // selectRandomSide();
            if (gamestate.isLooney()){
                System.out.println("making handouts");
                // make handouts
                if (length1Chains.size() > 0){
                    Box handout = length1Chains.get(0);
                    int side = handout.getFreeSides().get(0);
                    drawBox(handout, side);
                }
                else if (length2Chains.size() > 0){
                    Box handout = length2Chains.get(0).get(0);
                    makeHardHeartedHandout(handout);

                }
                else if (loops.size() > 0){
                    int longestLoopIndex = 0;
                    for (int i = 0; i < loops.size(); ++i){
                        if (loops.get(i).size() > loops.get(longestLoopIndex).size())
                            longestLoopIndex = i;
                    }
                    Box handout = loops.get(longestLoopIndex).get(0);
                    int side = handout.getFreeSides().get(0);
                    drawBox(handout, side);
                }
                else{
                    int shortestChainIndex = 0;
                    for (int i = 0; i < loops.size(); ++i){
                        if (chains.get(i).size() > loops.get(shortestChainIndex).size())
                            shortestChainIndex = i;
                    }
                    Box handout = chains.get(shortestChainIndex).get(0);
                    int side = handout.getFreeSides().get(0);
                    drawBox(handout, side);
                } 

                
            }
            else {
                System.out.println("performing minimax");
                findBestMove();
            }
                

        }

        delay = 30;
    }

    public void findBestMove(){
        long timer = System.currentTimeMillis();
        ArrayList<GameState> children = gamestate.listMoves();
        for (GameState child : children) 
            System.out.println(child.getLastBox());
        System.out.println("Number of children: " + children.size());
        int[] scores = new int[children.size()];
        int alpha = -1000;
        int beta = 1000;
        for (int i = 0; i < children.size(); ++i){
            long timer2 = System.currentTimeMillis();
            System.out.println(children.get(i).getLastBox());
            scores[i] = minimax(1, children.get(i), true, 2, alpha, beta);
            System.out.println("Score: " + scores[i]);
            // System.out.println("Time taken:");
            // System.out.println((System.currentTimeMillis() - timer2));
        }

        int bestVal = 1000;
        int bestIndex = -1;
        for (int i = 0; i < children.size(); ++i){
            if (scores[i] < bestVal){
                bestVal = scores[i];
                bestIndex = i;
            }
        }
        // if (bestIndex == -1){
        //     selectRandomSide();
        //     return;
        // }
    
        System.out.println("Best value: " + bestVal);
        
        // System.out.println("Total time: ");
        // System.out.println(System.currentTimeMillis() - timer);
        GameState nextState = children.get(bestIndex);
        Box nextBox = nextState.getLastBox();
        System.out.println("Best box: " + nextBox);
        int row = nextBox.getRow();
        int col = nextBox.getCol();
        int nextSide = nextState.getLastSide();
        Box box = gamestate.getBoxes().get(row).get(col);
        drawBox(box, nextSide);
    }

    public int minimax(int depth, GameState g, boolean isPlayer, int maxDepth, int alpha, int beta){
        cnt++;
        long timer = System.currentTimeMillis();
        // System.out.println("Depth: " + depth);
        // System.out.println("Player turn: " + isPlayer);
        ArrayList<GameState> listMoves = g.listMoves();
        if (depth == maxDepth || g.isLooney() || listMoves.isEmpty()){
            // System.out.println("Time taken:");
            // System.out.println((System.currentTimeMillis() - timer));
            return evaluate(g);
        }
        
        if (isPlayer){
            int bestVal = -1000;
            for (GameState move : listMoves){
                bestVal = Math.max(bestVal, minimax(depth + 1, move, !isPlayer, maxDepth, alpha, beta));
                alpha = Math.max(alpha, bestVal);
                if (beta <= alpha) continue;
            }
            // System.out.println("Time taken:");
            // System.out.println((System.currentTimeMillis() - timer));      
            return bestVal;
            

        }
        else{
            int bestVal = 1000;
            for (GameState move : listMoves){
                bestVal = Math.min(bestVal, minimax(depth + 1, move, isPlayer, maxDepth, alpha, beta));
                beta = Math.min(beta, bestVal);
                if (beta <= alpha) continue;
            }
            // System.out.println("Time taken:");
            // System.out.println((System.currentTimeMillis() - timer));
            return bestVal;
        }
    }
    public int evaluate(GameState g){
        // System.out.println("Evaluating gamestate: ");
        //g.printInfo();
        int netScore = g.getPlayerScore() - g.getCompScore();
        if (g.isLooney()){
            ArrayList<ArrayList<Box> > longChains = g.getStructures().get("chains");
            int parity = (Board.SIZE + 1) * (Board.SIZE + 1) + longChains.size() - 1;
            if (parity % 2 == 0) netScore -= 10;
            else netScore += 10;
        }
        Box box = g.getLastBox();
        Box nextBox = g.getAdjacent(box, g.getLastSide());
        // System.out.println("Last box: " + box);
        //int side = box.getLastSide();
        ArrayList<Box> chain = g.findStructure("chains", box);
        ArrayList<Box> loop = g.findStructure("loops", box);
        ArrayList<Box> chain1 = g.findStructure("chains", nextBox);
        ArrayList<Box> loop1 = g.findStructure("loops", nextBox);
        ArrayList<Box> handout = g.getHandouts();
        ArrayList<Box> length2Chains = g.findStructure("2-chains", box);
        ArrayList<Box> length2ChainsNext = g.findStructure("2-chains", nextBox);
        if ((handout.indexOf(box) != -1 || handout.indexOf(nextBox) != -1) 
            && (chain != null || loop != null || chain1 != null || loop1 != null || length2Chains != null || length2ChainsNext != null)){
            if (g.isPlayerTurn()) netScore += 15;
            else netScore -= 15; 
        }
        // System.out.println("Score: " + netScore);
        return netScore;
    }
    public void makeDoubleDealingMove(Box box){
        int side = box.getFreeSides().get(0);
        Box neighbor = gamestate.getNeighbor(box, side);
        int firstSide = neighbor.getFreeSides().get(0);
        int secondSide = neighbor.getFreeSides().get(1);
        if (Box.getOpposite(side) != firstSide) drawBox(neighbor, firstSide);
        else drawBox(neighbor, secondSide);
    }
    public void makeDoubleDealingMoveLoop(Box box){
        int side = box.getFreeSides().get(0);
        Box neighbor = gamestate.getNeighbor(box, side);
        int first = neighbor.getFreeSides().get(0);
        int second = neighbor.getFreeSides().get(1);
        if (first != Box.getOpposite(side)) 
            drawBox(neighbor, first); 
        else
            drawBox(neighbor, second);
    }
    public void makeHardHeartedHandout(Box box){
        int first = box.getFreeSides().get(0);
        int second = box.getFreeSides().get(1);
        Box firstNeighbor = gamestate.getNeighbor(box, first);
        Box secondNeighbor = gamestate.getNeighbor(box, second);
        if (firstNeighbor != null){
            drawBox(box, first);
        } 
        else 
            drawBox(box, second);
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

        Box neighbor = gamestate.getAdjacent(box, side);
        if (neighbor != null){
            board.drawNeighbor(neighbor, side);
            neighbor.setLastSide(Box.getOpposite(side));
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

    public int getRandNum() {
        return randNum;
    }

    public void setRandNum(int randNum) {
        this.randNum = randNum;
    }





}
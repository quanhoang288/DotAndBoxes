import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

public class GameState implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;
    private boolean playerTurn;
    private ArrayList<ArrayList<Box>> boxes;
    private Map<String, ArrayList<ArrayList<Box>>> structures;
    private ArrayList<Box> freeMoves, joints; // degree = 3, 4
    private ArrayList<Box> handouts, length1Chains;
    private Box lastBox;
    private int lastSide;
    private int looneyValue;
    private int playerScore, compScore;

    // private boolean playerInControl;
    public GameState() {
        boxes = new ArrayList<ArrayList<Box>>();
        freeMoves = new ArrayList<Box>();
        joints = new ArrayList<Box>();
        handouts = new ArrayList<Box>();
        length1Chains = new ArrayList<Box>();
        structures = new HashMap<String, ArrayList<ArrayList<Box>>>();
        structures.put("chains", new ArrayList<ArrayList<Box>>());
        structures.put("loops", new ArrayList<ArrayList<Box>>());
        structures.put("2-chains", new ArrayList<ArrayList<Box>>());
        structures.put("possibleLoops", new ArrayList<ArrayList<Box>>());
        structures.put("doubleHandouts", new ArrayList<ArrayList<Box>>());

    }

    public GameState(ArrayList<ArrayList<Box>> boxes, boolean playerTurn) {
        this();
        this.boxes = boxes;
        for (int i = 0; i < Board.SIZE; ++i) {
            for (int j = 0; j < Board.SIZE; ++j)
                freeMoves.add(boxes.get(i).get(j));
        }
        this.playerTurn = playerTurn;
    }



    public Object clone() {
        GameState g = null;
        try {
            g = (GameState) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        g.freeMoves = new ArrayList<Box>();
        for (Box box : freeMoves) g.freeMoves.add((Box) box.clone());
        return g;
    } 
    public static Object deepCopy(Object object) {
        try {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
          outputStrm.writeObject(object);
          ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
          ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
          return objInputStream.readObject();
        }
        catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }
    
    public void update(Box box, int side){
        //System.out.println("Updating box: " + box + " with side " + side);
        lastBox = box;
        lastSide = side;
        int degree = 4 - box.getNumOfSides(); // number of free edges
        if (degree == 0){
            // box if filled
            // then remove the box from all structures it was part of 
            // this box was a handout before the move played
            removeFromList(box);
            if (playerTurn)
                playerScore++;
            else 
                compScore++;
        }
        else if (degree == 1){
            //playerTurn = !playerTurn;
            int freeSide = box.getFreeSides().get(0);
            Box neighbor = getNeighbor(box, freeSide);
            // either a side edge or connected to a joint
            if (neighbor == null || isJoint(neighbor)){
                // create a single handout 

                // ArrayList<Box> handout = new ArrayList<Box>();
                // handout.add(box);
                removeFromList(box);
                handouts.add(box);
                //createStructure("handouts", handout);
            }
            else{
                // there is another box connected to the handout box
                int neighborDegree = 4 - neighbor.getNumOfSides();
                if (neighborDegree == 1){
                    // add 2 boxes to double-handout list
                    ArrayList<Box> doubleHandout = new ArrayList<Box>();
                    doubleHandout.add(box);
                    doubleHandout.add(neighbor);
                    removeFromList(box);
                    removeFromList(neighbor);
                    createStructure("doubleHandouts", doubleHandout);
                }
                else{
                    // neighbor degree has value other than 0 and 1
                    //split the current structure into 2 new structures

                    removeFromList(box);
                    // ArrayList<Box> handout = new ArrayList<Box>();
                    // handout.add(box);
                    // createStructure("handouts", handout);
                    if (neighborDegree == 2){
                        ArrayList<Box> newChain = new ArrayList<Box>();
                        newChain.add(box);
                        ArrayList<Box> connectedPart = findConnected(neighbor);

                        if (connectedPart != null){
                            for (int i = 0; i < connectedPart.size(); ++i){
                                newChain.add(connectedPart.get(i));
                                removeFromList(connectedPart.get(i));
                            }
                        }
                        Box adjacent = getAdjacent(box, side);
                        if (adjacent != null && adjacent.getNumOfSides() == 3){
                            newChain.add(adjacent);
                            createStructure("loops", newChain);
                        }
                        else{
                            if (newChain.size() > 2)
                                createStructure("chains", newChain);
                            else
                                createStructure("2-chains", newChain);
                        }
                            
                    }
                    
                    handouts.add(box);
                    
                    

                }
            }
        }
        else if (degree == 2){
            //playerTurn = !playerTurn;
            // first remove the box from freeboxes list 
            // merge all structures connected to the box into a new structure and remove old ones from structure list
            ArrayList<Box> newChain = findConnected(box);
            // System.out.println("new chain");
            // for (Box bx : newChain){
            //     System.out.println(bx);
            // }
            
            for (int i = 0; i < newChain.size(); ++i){
                removeFromList(newChain.get(i));
            }
            if (newChain.size() == 1) 
                length1Chains.add(box);
            else if (newChain.size() == 2){
                createStructure("2-chains", newChain);
            }
            else
                createStructure("chains", newChain);
            

        }
        else{
            // degree = 3
            //playerTurn = !playerTurn;
        }
        // update the list of joints and free boxes
        for (int i = 0; i < freeMoves.size(); ++i){
            Box curBox = freeMoves.get(i);
            if (isJoint(curBox)){
                freeMoves.remove(curBox);
                joints.add(curBox);
            }
        }
        ArrayList<ArrayList<Box> > chains = structures.get("chains");
        for (int i = 0; i < chains.size(); ++i){
            ArrayList<Box> curChain = chains.get(i);
            if (curChain.size() > 0 && checkLoop(curChain)){
                // System.out.println("creating loop...");
                chains.remove(curChain);
                structures.get("loops").add(curChain);
            }
        }
        ArrayList<ArrayList<Box> > loops = structures.get("loops");
        for (int i = 0; i < loops.size(); ++i){
            ArrayList<Box> curLoop = loops.get(i);
            if (curLoop.size() < 4) loops.remove(curLoop);
        }
    }
    public boolean isGameOver(){
        for (ArrayList<Box> row : boxes){
            for(Box box : row){
                if (box.getNumOfSides() != 4) return false;
            }
        }
        return true;
    }
    public ArrayList<GameState> listMoves(){
        ArrayList<GameState> res = new ArrayList<GameState>();

        boolean isLooney = isLooney();
        for (int i = 0; i < Board.SIZE; ++i){
            for (int j = 0; j < Board.SIZE; ++j){
                Box box = boxes.get(i).get(j);
                int degree = 4 - box.getNumOfSides();
                if (degree == 0) continue;
                //if (degree < 3) continue;
                
                // System.out.println("Number of free sides: " + degree);
                ArrayList<Integer> sides = new ArrayList<>();
                int col = box.getCol();
                int row = box.getRow();
                ArrayList<Box> chain = findStructure("chains", box);
                ArrayList<Box> loop = findStructure("loops", box);
                

                if (box.checkFreeSide(Box.LEFT))  sides.add(Box.LEFT);
                if (box.checkFreeSide(Box.TOP)) sides.add(Box.TOP);
                if (i == Board.SIZE - 1 && box.checkFreeSide(Box.BOT)) sides.add(Box.BOT);
                if (j == Board.SIZE - 1 && box.checkFreeSide(Box.RIGHT)) sides.add(Box.RIGHT);
                for (int k = 0; k < sides.size(); ++k){
                    int side = sides.get(k);
                    // System.out.println("side: " + side);
                    GameState g = (GameState) deepCopy(this);
                    ArrayList<ArrayList<Box> > boxList = g.getBoxes();
                    Box move = boxList.get(row).get(col);
                    //System.out.println("Current move: " + move);
                    move.setHighlight(side);
                    move.selectSide();
                    g.update(move, side);
                    Box neighbor = g.getAdjacent(move, side);
                    if (neighbor != null){
                        //System.out.println("Neighbor: " + neighbor);
                        Board.drawNeighbor(neighbor, side);
                        g.update(neighbor, Box.getOpposite(side));
                        if (!neighbor.allSidesDrawn() && !move.allSidesDrawn()) g.setPlayerTurn(!playerTurn); 
                    }
                    else if (!move.allSidesDrawn()) g.setPlayerTurn(!playerTurn); 
                    res.add(g);


                }
            }

        }
        return res;

    }
    public boolean isLooney(){
        return (freeMoves.isEmpty() && joints.isEmpty());
    }
    public Box getAdjacent(Box box, int side){
        int row = box.getRow();
        int col = box.getCol();
        if (side == Box.TOP && row > 0) return boxes.get(row - 1).get(col);
        else if (side == Box.LEFT && col > 0) return boxes.get(row).get(col - 1);
        else if (side == Box.RIGHT && col < Board.SIZE - 1) return boxes.get(row).get(col + 1);
        else if (side == Box.BOT && row < Board.SIZE - 1) return boxes.get(row + 1).get(col);
        return null;
    }
    private ArrayList<Box> findBrokenChain(){
        ArrayList<ArrayList<Box> > chains = structures.get("chains");
        for (ArrayList<Box> chain : chains){
            if (chain.get(0).getNumOfSides() == 3) return chain;
        }
        return null;
    }

    private boolean isNeighbor(Box first, Box second){
        if (Math.abs(first.getRow() - second.getRow()) + Math.abs(first.getCol() - second.getCol()) > 1) return false;
        //ArrayList<Integer> firstSides = first.getFreeSides();
        if (Math.abs(first.getRow() - second.getRow()) == 1){
            if ((first.checkFreeSide(Box.TOP) && second.checkFreeSide(Box.BOT)) || (first.checkFreeSide(Box.BOT) && second.checkFreeSide(Box.TOP)))
                return true;
        }
        if (Math.abs(first.getCol() - second.getCol()) == 1){
            if ((first.checkFreeSide(Box.LEFT) && second.checkFreeSide(Box.RIGHT)) || (first.checkFreeSide(Box.RIGHT) && second.checkFreeSide(Box.LEFT)))
                return true;
        }
        // for (int i = 0; i < firstSides.size(); ++i){
        //     int side = firstSides.get(i);
        //     if (second.checkFreeSide(Box.getOpposite(side))) return true;
        // }
        return false;
    }
    private boolean checkLoop(ArrayList<Box> chain){
        if (chain.size() < 4) return false;
        for (Box box: chain){
            if (getNumNeighbors(box) < 2) return false;
        }
        return true;
        // Box firstBox = chain.get(0);
        // Box lastBox = chain.get(chain.size() - 1);
        // if (isNeighbor(firstBox, lastBox)) return true;
        // return false;
    }
    private boolean checkPossibleLoop(ArrayList<Box> chain){
        return false;
    }
    public ArrayList<Box> findStructure(String name, Box box){
        ArrayList<ArrayList<Box> > structure = structures.get(name);
        for (ArrayList<Box> struct: structure){
            if (inStructure(struct, box)) return struct;
        }
        return null;        
    }
    public ArrayList<Box> freeMoves(){
        return freeMoves;
    }
    public ArrayList<Box>  getSingleHandouts(){
        return handouts;
    }
    public ArrayList<ArrayList<Box> > getDoubleHandouts(){
        return structures.get("doubleHandouts");
    }
    private boolean isBroken(ArrayList<Box> struct){
        for (Box box : struct){
            if (box.getNumOfSides() == 3) return true;
        }
        return false;
    }
    public int numOfBroken(String name){
        int cnt = 0;
        ArrayList<ArrayList<Box>> structList = structures.get(name);
        for (ArrayList<Box> struct : structList){
            if (isBroken(struct)) cnt++;
        }
        return cnt;

    }
    private ArrayList<Box> findConnected(Box box){
        ArrayList<Box> res = new ArrayList<Box>();
        Stack<Box> q = new Stack<Box>();
        boolean flag[][] = new boolean[Board.SIZE][Board.SIZE];
        q.push(box);
        flag[box.getRow()][box.getCol()] = true;
        while (!q.isEmpty()){
            Box top = q.pop();
            // System.out.println("Top: " + top);
            //flag[top.getRow()][top.getCol()] = true;
            res.add(top);
            int firstSide = top.getFreeSides().get(0);
            int secondSide = top.getFreeSides().get(1);
            Box firstNeighbor = getNeighbor(top, firstSide);
            Box secondNeighbor = getNeighbor(top, secondSide);
            
            
            if (firstNeighbor != null && !flag[firstNeighbor.getRow()][firstNeighbor.getCol()] && firstNeighbor.getNumOfSides() == 2 ){
                q.add(firstNeighbor);
                flag[firstNeighbor.getRow()][firstNeighbor.getCol()] = true;
            } 
                
            if (secondNeighbor != null && !flag[secondNeighbor.getRow()][secondNeighbor.getCol()] && secondNeighbor.getNumOfSides() == 2){
                q.add(secondNeighbor);
                flag[secondNeighbor.getRow()][secondNeighbor.getCol()] = true;
                
            }
                
        }
        
        return res;
    }
    private boolean isJoint(Box box){
        int degree = 4 - box.getNumOfSides(); 
        if (degree < 3) return false;
        int cnt = 0;
        ArrayList<Integer> freeSides = box.getFreeSides();
        for (int i = 0; i < freeSides.size(); ++i){
            int side = freeSides.get(i);
            Box neighbor = getNeighbor(box, side);
            if (neighbor != null &&  neighbor.getNumOfSides() == 2){
                cnt++; // connected to more than 1 structure
            }
        }
        if (cnt >= 2) return true;
        return false;
    }
    private int getNumNeighbors(Box box){
        int cnt = 0;
        ArrayList<Integer> freeSides = box.getFreeSides();
        for (int i = 0; i < freeSides.size(); ++i){
            int curSide = freeSides.get(i);
            Box neighbor = getNeighbor(box, curSide);
            if (neighbor != null && neighbor.getNumOfSides() == 2) cnt++;
        }
        return cnt;
    }
    private void removeFromStructure(ArrayList<Box> structure, Box box){
        if (!inStructure(structure, box)) return;
        structure.remove(box);
    }
    private void addToStructure(ArrayList<Box> structure, Box box){
        if (inStructure(structure, box)) return;
        structure.add(box);
    }
    public boolean inStructure(ArrayList<Box> structure, Box box){
        return structure.indexOf(box) != -1;
    }
    private void createStructure(String name, ArrayList<Box> boxes){

        structures.get(name).add(boxes);
    }
    public void removeFromList(Box box){
        if (freeMoves.indexOf(box) != -1) freeMoves.remove(box);
        if (joints.indexOf(box) != -1) joints.remove(box);
        if (handouts.indexOf(box) != -1) handouts.remove(box);
        if (length1Chains.indexOf(box) != -1) length1Chains.remove(box);
        for (Map.Entry<String, ArrayList<ArrayList<Box> > > structure : structures.entrySet()){
            ArrayList<ArrayList<Box> > curStruct = structure.getValue();
            for (ArrayList<Box> struct : curStruct){
                removeFromStructure(struct, box);      
            }
            for (int i = 0; i < curStruct.size(); ++i){
                if (curStruct.get(i).size() == 0) curStruct.remove(i);
            }
        }
    }

    public boolean inList(Box box){
        for (Map.Entry<String, ArrayList<ArrayList<Box> > > structure : structures.entrySet()){
            ArrayList<ArrayList<Box> > curStruct = structure.getValue();
            for (ArrayList<Box> struct : curStruct){
                if (inStructure(struct, box)){
                    return true;
                }
            }
        }
        return false;
    }


    public Box getNeighbor(Box box, int side){
        int row = box.getRow();
        int col = box.getCol();
        Box res = null;
        int oppSide = Box.getOpposite(side);
        switch(side){
            case Box.TOP:
                if (row > 0 && boxes.get(row - 1).get(col).checkFreeSide(oppSide)) res = boxes.get(row - 1).get(col);
                break; 
            case Box.LEFT:
                if (col > 0 && boxes.get(row).get(col - 1).checkFreeSide(oppSide)) res = boxes.get(row).get(col - 1);
                break;
            case Box.BOT:
                if (row < Board.SIZE - 1 && boxes.get(row + 1).get(col).checkFreeSide(oppSide)) res = boxes.get(row + 1).get(col);
                break;
            case Box.RIGHT:
                if (col < Board.SIZE - 1 && boxes.get(row).get(col + 1).checkFreeSide(oppSide)) res = boxes.get(row).get(col + 1);
        }
        return res;
    }

    // public boolean isLooney(){
        
    // }


    public void printInfo(){
        // list of free moves
        System.out.println("Free moves: " + freeMoves.size());
        for (Box box : freeMoves){
            System.out.println(box);
            // System.out.println("---------------------------");
        }
        // list of joints
        System.out.println("Joints: " + joints.size());
        for (Box box : joints){
            System.out.println(box);
            // System.out.println("---------------------------");
        }
        // handouts

        //ArrayList<ArrayList<Box> > singleHandouts = structures.get("handouts");
        System.out.println("Single handouts: " + handouts.size());
        for (Box box : handouts){
            System.out.println(box);
            // System.out.println("---------------------------");
        }
        // double handouts

        ArrayList<ArrayList<Box> > doubleHandouts = structures.get("doubleHandouts");
        System.out.println("Double handouts: " + doubleHandouts.size());
        for (ArrayList<Box> handout : doubleHandouts){
            for (Box box : handout) System.out.println(box);
        }
        // 1 chains 
        ArrayList<Box> length1Chains = this.length1Chains;
        System.out.println("Length 1 chains: " + length1Chains.size());
        for (Box box : length1Chains){
            System.out.println(box);
        }
        // 2 chains 
        ArrayList<ArrayList<Box> > length2Chains = structures.get("2-chains");
        System.out.println("Length 2 chains: " + length2Chains.size());
        for (ArrayList<Box> chain : length2Chains){
            for (Box box : chain) System.out.println(box);
        }
        
        // chains 

        ArrayList<ArrayList<Box> > chains = structures.get("chains");
        System.out.println("Chains: " + chains.size()); 
        for (ArrayList<Box> chain : chains){
            for (Box box : chain) System.out.println(box);
            System.out.println("---------------------------");
        }

        // loops

        ArrayList<ArrayList<Box> > loops = structures.get("loops");
        System.out.println("Loops: " + loops.size());
        for (ArrayList<Box> loop : loops){
            for (Box box : loop) System.out.println(box);
            System.out.println("---------------------------");
        }
        // scores
        System.out.println("Player score: " + playerScore);
        System.out.println("Computer score: " + compScore);

        // turn
        System.out.println("Player next: " + playerTurn);
        System.out.println("-------------------------------");
    }

    public ArrayList<Box> getFreeMoves() {
        return freeMoves;
    }

    public void setFreeMoves(ArrayList<Box> freeMoves) {
        this.freeMoves = freeMoves;
    }

    public ArrayList<Box> getJoints() {
        return joints;
    }

    public void setJoints(ArrayList<Box> joints) {
        this.joints = joints;
    }

    public ArrayList<Box> getLength1Chains() {
        return length1Chains;
    }

    public void setLength1Chains(ArrayList<Box> length1Chains) {
        this.length1Chains = length1Chains;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean playerTurn) {
        this.playerTurn = playerTurn;
    }


    public ArrayList<ArrayList<Box>> getBoxes() {
        return boxes;
    }

    public void setBoxes(ArrayList<ArrayList<Box>> boxes) {
        this.boxes = boxes;
    }

    public Map<String, ArrayList<ArrayList<Box>>> getStructures() {
        return structures;
    }

    public void setStructures(Map<String, ArrayList<ArrayList<Box>>> structures) {
        this.structures = structures;
    }

    public ArrayList<Box> getHandouts() {
        return handouts;
    }

    public void setHandouts(ArrayList<Box> handouts) {
        this.handouts = handouts;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public void setPlayerScore(int playerScore) {
        this.playerScore = playerScore;
    }

    public int getCompScore() {
        return compScore;
    }

    public void setCompScore(int compScore) {
        this.compScore = compScore;
    }

    public Box getLastBox() {
        return lastBox;
    }

    public void setLastBox(Box lastBox) {
        this.lastBox = lastBox;
    }

    public int getLastSide() {
        return lastSide;
    }

    public void setLastSide(int lastSide) {
        this.lastSide = lastSide;
    }

    public int getLooneyValue() {
        return looneyValue;
    }

    public void setLooneyValue(int looneyValue) {
        this.looneyValue = looneyValue;
    }


    
}

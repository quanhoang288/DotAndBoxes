import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;


public class GameState {
    private boolean playerTurn;
    private ArrayList<ArrayList<Box>> boxes;
    private Map<String, ArrayList<ArrayList<Box> > > structures;
    private ArrayList<Box> freeMoves, joints; // degree = 3, 4
    private ArrayList<Box> handouts;
    // private int looneyValue;
    // private int netScore;
    // private boolean playerInControl;
    public GameState(){
        boxes = new ArrayList<ArrayList<Box> >();
        freeMoves = new ArrayList<Box>();
        joints = new ArrayList<Box>();
        handouts = new ArrayList<Box>();
        structures = new HashMap<String, ArrayList<ArrayList<Box> > >();
        structures.put("chains", new ArrayList<ArrayList<Box> >());
        structures.put("loops", new ArrayList<ArrayList<Box> >());
        // structures.put("handouts", new ArrayList<ArrayList<Box> >());
        structures.put("1-chains", new ArrayList<ArrayList<Box> >());
        structures.put("2-chains", new ArrayList<ArrayList<Box> >());
        structures.put("possibleLoops", new ArrayList<ArrayList<Box> >());
        structures.put("doubleHandouts", new ArrayList<ArrayList<Box> >());
       
    }
    public GameState(ArrayList<ArrayList<Box>> boxes, boolean playerTurn){
        this();
        this.boxes = boxes;
        for (int i = 0; i < Board.SIZE; ++i){
            for (int j = 0; j < Board.SIZE; ++j)
                freeMoves.add(boxes.get(i).get(j));
        }
        this.playerTurn = playerTurn;
    }
    public void update(Box box, int side){
        // System.out.println("Updating gamestate...");
        // System.out.println("Processing " + box);
        int degree = 4 - box.getNumOfSides(); // number of free edges
        if (degree == 0){
            // box if filled
            // then remove the box from all structures it was part of 
            // this box was a handout before the move played
            removeFromList(box);
        }
        else if (degree == 1){
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
                    
                    ArrayList<Box> newChain;
                    newChain = new ArrayList<Box>();
                    newChain.add(box);
                    ArrayList<Box> connectedPart = findConnected(neighbor);

                    if (connectedPart != null){
                        for (int i = 0; i < connectedPart.size(); ++i){
                            newChain.add(connectedPart.get(i));
                            removeFromList(connectedPart.get(i));
                        }
                    }
                    // System.out.println("new chain: ");
                    // for (Box bx: newChain) System.out.println(bx);
                    handouts.add(box);
                    createStructure("chains", newChain);
                    

                }
            }
        }
        else if (degree == 2){
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
            createStructure("chains", newChain);
            

        }
        else{
            // degree = 3
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
    private ArrayList<Box> findStructure(Box box){
        ArrayList<Box> res = null;
        for (Map.Entry<String, ArrayList<ArrayList<Box> > > structure : structures.entrySet()){
            ArrayList<ArrayList<Box> > curStruct = structure.getValue();
            for (ArrayList<Box> struct : curStruct){
                if (inStructure(struct, box)){
                    res = struct;
                }
            }
        }
        
        return res;
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

    private ArrayList<Box> findConnected(Box box){

        ArrayList<Box> res = new ArrayList<Box>();
        Stack<Box> q = new Stack<Box>();
        boolean flag[][] = new boolean[Board.SIZE][Board.SIZE];
        q.push(box);
        flag[box.getRow()][box.getCol()] = true;
        while (!q.isEmpty()){
            Box top = q.pop();
            System.out.println("Top: " + top);
            //flag[top.getRow()][top.getCol()] = true;
            res.add(top);
            int firstSide = top.getFreeSides().get(0);
            int secondSide = top.getFreeSides().get(1);
            Box firstNeighbor = getNeighbor(top, firstSide);
            Box secondNeighbor = getNeighbor(top, secondSide);
            
            
            if (firstNeighbor != null && !flag[firstNeighbor.getRow()][firstNeighbor.getCol()] && firstNeighbor.getNumOfSides() == 2){
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
        boolean res = false;
        int degree = 4 - box.getNumOfSides(); 
        // if (degree > 2 && connectedToStructure(box)) res = true;
        // return res;
        return degree > 2;
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
    private boolean inStructure(ArrayList<Box> structure, Box box){
        return structure.indexOf(box) != -1;
    }
    private void createStructure(String name, ArrayList<Box> boxes){

        structures.get(name).add(boxes);
    }
    public void removeFromList(Box box){
        if (freeMoves.indexOf(box) != -1) freeMoves.remove(box);
        if (joints.indexOf(box) != -1) joints.remove(box);
        if (handouts.indexOf(box) != -1) handouts.remove(box);
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
    public void addToList(Box box){
        for (Map.Entry<String, ArrayList<ArrayList<Box> > > structure : structures.entrySet()){
            ArrayList<ArrayList<Box> > curStruct = structure.getValue();
            for (ArrayList<Box> struct : curStruct){
                addToStructure(struct, box);
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
    public void printInfo(){
        // list of free moves
        // System.out.println("Free moves: " + freeMoves.size());
        // for (Box box : freeMoves){
        //     System.out.println(box);
        // }
        // // list of joints
        // System.out.println("Joints: " + joints.size());
        // for (Box box : joints){
        //     System.out.println(box);
        // }
        // handouts

        //ArrayList<ArrayList<Box> > singleHandouts = structures.get("handouts");
        System.out.println("Single handouts: " + handouts.size());
        for (Box box : handouts){
            System.out.println(box);
            System.out.println("---------------------------");
        }
        // double handouts

        ArrayList<ArrayList<Box> > doubleHandouts = structures.get("doubleHandouts");
        System.out.println("Double handouts: " + doubleHandouts.size());
        for (ArrayList<Box> handout : doubleHandouts){
            for (Box box : handout) System.out.println(box);
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
        System.out.println("-------------------------------");
    }


    
}

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.MouseInputListener;

import java.awt.event.*;

public class Game extends JFrame implements MouseInputListener, ActionListener, Runnable {

    private static final long serialVersionUID = 1L;
    private Board board;
    private boolean playerTurn;
    private int playerScore, compScore;
    private boolean isSquareFill, isSideDrawn, running;
    private Thread thread;

    public Game() {
        // playerTurn = Math.random() >= 0.5;
        playerTurn = true;
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem newGame = new JMenuItem("New Game");
        JMenuItem setting = new JMenuItem("Setting");
        JMenuItem exit = new JMenuItem("Exit");

        newGame.addActionListener(this);
        newGame.setActionCommand("new");
        setting.addActionListener(this);
        setting.setActionCommand("setting");
        exit.addActionListener(this);
        exit.setActionCommand("exit");

        menu.add(newGame);
        menu.add(setting);
        menu.add(exit);
        menuBar.add(menu);
        board = new Board(this);
        board.addMouseListener(this);
        board.addMouseMotionListener(this);
        this.setTitle("Dots and Boxes");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(menuBar);
        this.add(board);
        this.setSize(Board.WIDTH, Board.HEIGHT);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
        this.start();
    }

    public static void main(String[] args) throws Exception {
        new Game();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        int x = e.getX();
        int y = e.getY();
        board.checkMousePosition(x, y, false, playerTurn);
        // board.repaint();


        if (isSideDrawn) board.setTimeComp(60);
        if (!isSquareFill && isSideDrawn) {
            // playerTurn = !playerTurn;
            playerTurn = false;
            // System.out.println("Switch turn");
        } else if (isSquareFill) {
            playerScore += board.getNumSquaresFilled();
            board.setNumSquaresFilled(0);

        }
        isSideDrawn = false;
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

        // board.checkMousePosition(x, y);
        // board.repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        board.checkMousePosition(x, y, true, playerTurn);
        // board.repaint();
        // System.out.println("inside mouse move");
        // board.update(board.getGraphics());
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean isPlayerTurn) {
        this.playerTurn = isPlayerTurn;
    }

    public boolean isGameOver() {
        return (playerScore + compScore) == (Board.SIZE * Board.SIZE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("new")) {
            board.clearAll();

            // board.repaint();
        } else if (cmd.equals("setting")) {
            // todo
        } else {
            System.exit(0);
        }
    }

    public boolean isSquareFill() {
        return isSquareFill;
    }

    public void setSquareFill(boolean isSquareFill) {
        this.isSquareFill = isSquareFill;
    }

    public boolean isSideDrawn() {
        return isSideDrawn;
    }

    public void setSideDrawn(boolean isSideDrawn) {
        this.isSideDrawn = isSideDrawn;
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

    public void update() {

    }

    public void render() {
        board.repaint();
    }

    public synchronized void start() {
        thread = new Thread(this);
        thread.start();
        running = true;
    }

    public synchronized void stop() {
        try {
            thread.join();
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double numOfTicks = 60.0;
        double ns = 1000000000 / numOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                update();
                delta--;
                render();
                frames++;
            }

            if (System.currentTimeMillis() - timer > 1000){
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }
        }
        stop();

    }
}

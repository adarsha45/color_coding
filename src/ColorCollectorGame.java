import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.swing.*;
import java.io.File;

public class ColorCollectorGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;
        Color color;

        Tile(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

    int tileSize = 25;
    int boardWidth;
    int boardHeight;

    Tile head;
    Tile food;
    Random random;
    Timer gameLoop;
    int velocityX;
    int velocityY;
    boolean gameOver = false;
    int score = 0;
    int tileCounter = 0;

    ArrayList<Tile> placedTiles = new ArrayList<>();

    ColorCollectorGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        head = new Tile(5, 5, Color.WHITE);
        food = new Tile(10, 10, Color.BLUE);
        random = new Random();
        placeFood(random);

        velocityX = 0;
        velocityY = 0;

        gameLoop = new Timer(60, this);
        gameLoop.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        for (int i = 0; i < boardWidth / tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, boardHeight);
            g.drawLine(0, i * tileSize, boardWidth, i * tileSize);
        }

        // Draw collected tiles
        for (Tile t : placedTiles) {
            g.setColor(t.color);
            g.fillRect(t.x * tileSize, t.y * tileSize, tileSize, tileSize);
        }

        // Draw head
        g.setColor(head.color);
        g.fillRect(head.x * tileSize, head.y * tileSize, tileSize, tileSize);

        // Draw food
        g.setColor(food.color);
        g.fillRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize);

        // Draw score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + score, 10, 20);

        if (gameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over", boardWidth / 2 - 100, boardHeight / 2);
        }
    }

    public void placeFood(Random random) {
        int x = random.nextInt(boardWidth / tileSize);
        int y = random.nextInt(boardHeight / tileSize);
        food.x = x;
        food.y = y;
    }

    public boolean collision(Tile t1, Tile t2) {
        return t1.x == t2.x && t1.y == t2.y;
    }

    public void move() {
        if (collision(head, food)) {
            Color newTileColor;
            boolean specialTile = false;

            if ((tileCounter + 1) % 4 == 0) {
                newTileColor = random.nextBoolean() ? Color.RED : Color.GREEN;
                specialTile = true;
            } else {
                newTileColor = Color.YELLOW;
                score += 5;
            }

            Tile newTile = new Tile(food.x, food.y, newTileColor);
            placedTiles.add(newTile);
            tileCounter++;
            placeFood(random);

            if (specialTile && newTileColor == Color.GREEN && collision(head, newTile)) {
                score += 10;
                playSound("ding.wav");
            }
        }

        head.x += velocityX;
        head.y += velocityY;

        //setting up the boundry

        if (head.y >= boardHeight / tileSize) {
            head.y = 0;
        } else if (head.y < 0) {
            head.y = boardHeight / tileSize - 1;
        } else if (head.x >= boardWidth / tileSize) {
            head.x = 0;
        } else if (head.x < 0) {
            head.x = boardWidth / tileSize - 1;
        }

        for (Tile t : placedTiles) {
            if (collision(head, t)) {
                if (t.color == Color.RED) {
                    gameOver = true;
                    gameLoop.stop();
                    break;
                } else if (t.color == Color.GREEN) {
                    score += 10;
                    playSound("ding.wav");
                    t.color = Color.YELLOW; // Avoid scoring multiple times
                }
            }
        }
    }

    public void playSound(String soundFile) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(new File(soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            velocityX = 0; velocityY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            velocityX = 0; velocityY = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            velocityX = -1; velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            velocityX = 1; velocityY = 0;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Color Collector Game");
        ColorCollectorGame game = new ColorCollectorGame(600, 600);
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

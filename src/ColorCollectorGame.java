import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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
    int scorePerTile;
    int specialTileFrequency;
    int gameSpeed;

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
        loadEnv();
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
    
        gameLoop = new Timer(gameSpeed, this); // gameSpeed is now correctly set
        gameLoop.start();
    }
    

    private void loadEnv() {
        try {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream("config.env");
            props.load(fis);
            fis.close();
            scorePerTile = Integer.parseInt(props.getProperty("SCORE_PER_TILE", "5"));
            specialTileFrequency = Integer.parseInt(props.getProperty("SPECIAL_TILE_FREQUENCY", "4"));
            gameSpeed = Integer.parseInt(props.getProperty("GAME_SPEED_MS", "500"));
        } catch (IOException e) {
            System.out.println("Error loading environment variables, using defaults.");
            scorePerTile = 5;
            specialTileFrequency = 4;
            gameSpeed = 60;
        }
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

        for (Tile t : placedTiles) {
            g.setColor(t.color);
            g.fillRect(t.x * tileSize, t.y * tileSize, tileSize, tileSize);
        }

        g.setColor(head.color);
        g.fillRect(head.x * tileSize, head.y * tileSize, tileSize, tileSize);

        g.setColor(food.color);
        g.fillRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize);

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
            if ((tileCounter + 1) % specialTileFrequency == 0) {
                newTileColor = random.nextBoolean() ? Color.RED : Color.GREEN;
            } else {
                newTileColor = Color.YELLOW;
                score += scorePerTile;
            }

            placedTiles.add(new Tile(food.x, food.y, newTileColor));
            tileCounter++;
            placeFood(random);
        }

        head.x += velocityX;
        head.y += velocityY;

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
                    t.color = Color.YELLOW;
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

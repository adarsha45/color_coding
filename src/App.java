
import javax.swing.*;
public class App {
    public static void main(String[] args) throws Exception {
        int width = 600;
        int height = 600;
        JFrame frame = new JFrame("Snake Game 103");
        frame.setVisible(true);
        frame.setSize(width, height);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); // Center the window on the screen
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       //12:22 
       ColorCollectorGame snakeGame = new ColorCollectorGame(width, height);
        frame.add(snakeGame);    
        frame.pack();
        snakeGame.requestFocus(true); // Make the game panel 
        
    }
}

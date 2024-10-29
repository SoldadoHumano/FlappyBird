package FlappyBird;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) throws Exception {
        int borderWidth = 360;
        int borderHeight = 640;
        Image icon = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/Images/flappybird.png"));
        JFrame frame = new JFrame("Flappy Bird");
        frame.setSize(borderWidth, borderHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(icon);
        FlappyBird flappyBird = new FlappyBird();
        frame.add(flappyBird);
        frame.pack();
        flappyBird.requestFocus();
        frame.setVisible(true);
    }
}
package FlappyBird;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Monitor refresh rate
    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    DisplayMode dm = gd.getDisplayMode();
    int refreshRate = dm.getRefreshRate();

    // Images
    Image backGroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidgh = 34;
    int birdHeight = 24;
    double rotation = 0;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidgh;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64; // Scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game Logic
    Bird bird;
    int velocityY = -9; // Move bird up/down speed
    int velocityX = -4; // Move pipes to the left speed (simulates bird movig right)
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;


    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);
        setFocusable(true);
        addKeyListener(this);

        // Images load
        backGroundImg = new ImageIcon(getClass().getResource("/Images/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/Images/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/Images/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/Images/bottompipe.png")).getImage();

        // Bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        /// Place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Code to be executed
                placePipes();
            }
        });
        placePipeTimer.start();

        if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
            // Game timer "fps"
            gameLoop = new Timer(1000 / 60, this);
            gameLoop.start();
        } else {
            // Game timer "fps"
            gameLoop = new Timer(1000 / refreshRate, this);
            gameLoop.start();
        }

    }

    // Game sounds logic
    public void playSound(String soundFile) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void placePipes() {
        // (0-1) * pipeHeight / 2 -> (0-256)
        // 128
        // 0 - 128 - (0-256) -->  1/4 pipeHeight / 4 -> 3/4 pipeHeight
        int randomPipeY = (int) (pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y  + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    public void draw(Graphics g) {
        // System.out.println("draw"); // debug
        Graphics2D g2d = (Graphics2D) g.create();

        // Background
        g.drawImage(backGroundImg, 0, 0, boardWidth, boardHeight, null);

        // Bird
        g2d.rotate(Math.toRadians(rotation), bird.x + bird.width / 2, bird.y + bird.height / 2);
        g2d.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
        g2d.dispose();

        // Pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        } else {
            g.drawString(String.valueOf("Score: " + (int) score), 10, 35);
        }
    }


    public void move() {
        // Bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        // Pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (velocityY < 0) {
                rotation = Math.max(-25, rotation - 5); // Maximum upward tilt of -25 degrees
            } else {
                rotation = Math.min(25, rotation + 5); // Maximum downward tilt of 25 degrees
            }

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                playSound("src/Sounds/PipePass.wav");
                score += 0.5; // 0.5 because there are 2 pipes (0.5 * 2 = 1)
            }

            if (collision(bird, pipe)) {
                gameOver = true;
                playSound("src/Sounds/PipeHit.wav");
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
            playSound("src/Sounds/Fall.wav");
        }
    }

    public boolean collision(Bird a, Pipe b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }


    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            playSound("src/Sounds/Jump.wav");
            velocityY = -9;
            if (gameOver) {
                // Restart the by resetting the conditions
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}

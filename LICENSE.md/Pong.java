import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicInteger;

public class Pong {

    public static final int BALL_WIDTH = 10, PADDLE_WIDTH = 80;
    public static final double AI_SPEED = 4.1,HUMAN_SPEED=AI_SPEED*.5, BALL_SPEED = 3.3;
    public static final int SIZE = 480, PADDLE_THICKNESS = 15, PADDLE_SPRINT_DIST = 60;
    public static AtomicInteger currentKey = new AtomicInteger(KeyEvent.VK_UNDEFINED);
    public static final Color ATARI_BLUE = new Color(51, 102, 255);
    public static final Color WHITE = Color.white;

    public static int aiScore, humanScore;
    public static final int SCORE_FONT_SIZE = 48;
    public static void main(String... args) {
        Ball.updateBallPosition();

        GameFrame gameFrame = new GameFrame();
        gameFrame.repaint();



        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try{
                        gameFrame.repaint();
                    }catch(Throwable t){
                        System.err.println(t);
                    }
                }
            }}).start();

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ie) {
                        System.err.println("Thread interrupted");
                        //nil
                    }
                    Ball.updateBallPosition();
                    AiPaddle.MAKE_AI_MOVE();
                    HumanPaddle.moveHuman();
                }
            }
        }).start();




    }

    public static int i(double x) {
        return (int) x;
    }

    public static class Ball {
        public static double x,y;
        public static double dX, dY;

        static{ respawnBall();

        }
        public static void respawnBall(){
            x = SIZE / 2; y = SIZE / 2;
            double theta = Math.random() * Math.PI * .8 + (Math.random() > .5 ? Math.PI * .1 : Math.PI * 1.1);
            dY = Math.cos(theta);
            dX = Math.sin(theta);
        }

        public static void updateBallPosition() {
            if(x<0){
                humanScore++;
                respawnBall();
                return;
            }
            if(x>SIZE){
                aiScore++;
                respawnBall();
                return;
            }
            if((x<PADDLE_THICKNESS&&y>AiPaddle.yPos&&y<AiPaddle.yPos+PADDLE_WIDTH)
            ||(x>SIZE-PADDLE_THICKNESS&&y>HumanPaddle.yPos&&y<HumanPaddle.yPos+PADDLE_WIDTH)
            ){
                dX *= -1.08;
                dY += Math.random()*dX - dX/2;
            }

            if (y < 0 || y > SIZE) {
                dY *= -1;
                dY += Math.random()*.1 - .05;
            }
            x += dX * BALL_SPEED;
            y += dY * BALL_SPEED;
        }
    }

    public static class AiPaddle {
        public static int xPos = 0, yPos = SIZE / 2 - PADDLE_WIDTH / 2;
        public static void MAKE_AI_MOVE(){
            int centerPaddle = yPos + PADDLE_WIDTH/2;
            double mult = Math.max(-1,Math.min((Ball.y-centerPaddle)/((double) Math.max(PADDLE_SPRINT_DIST/8,PADDLE_SPRINT_DIST-((PADDLE_SPRINT_DIST * .6 * Ball.x * 1/SIZE)))),1));

            yPos += (int) (mult*AI_SPEED);
        }
    }
    public static class HumanPaddle {
        public static int xPos = SIZE - PADDLE_THICKNESS, yPos = SIZE / 2 - PADDLE_WIDTH/2;
        public static void moveHuman(){
            switch(currentKey.get()){
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    yPos-=(int) HUMAN_SPEED;
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    yPos+=(int) HUMAN_SPEED;
                    break;
            }
        }
    }

    public static class GamePanel extends javax.swing.JPanel {


        public Dimension getPreferredSize() {
            return new Dimension(SIZE, SIZE);
        }


        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(ATARI_BLUE);
            g.fillRect(0, 0, SIZE, SIZE);
            g.setColor(WHITE);
            g.fillOval(i(Ball.x - BALL_WIDTH / 2), i(Ball.y - BALL_WIDTH / 2), BALL_WIDTH, BALL_WIDTH);
            g.fillRect(AiPaddle.xPos,AiPaddle.yPos,PADDLE_THICKNESS,PADDLE_WIDTH);
            g.fillRect(HumanPaddle.xPos,HumanPaddle.yPos,PADDLE_THICKNESS,PADDLE_WIDTH);

            g.setFont(new Font("monospaced",Font.BOLD,SCORE_FONT_SIZE));
            String scores = aiScore+"|"+humanScore;
            g.drawString(scores,SIZE/2 - SCORE_FONT_SIZE/4 *scores.length() , SIZE/2);
        }
    }

    public static class GameFrame extends javax.swing.JFrame {
        public GameFrame() {
            super("Pong!");
            this.setVisible(true);
            this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            this.addKeyListener(new java.awt.event.KeyListener() {
                public void keyPressed(KeyEvent k) {
                    currentKey.set(k.getKeyCode());
                }

                public void keyTyped(KeyEvent k) {
                }

                public void keyReleased(KeyEvent k) {
                    currentKey.set(KeyEvent.VK_UNDEFINED);
                }
            });
            this.add(new GamePanel());
            this.pack();
        }
    }

}

package Pacman;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.tools.Tool;

//classe mModel extende a classe pai JPanel, para criar a janela
//implementa a classe ActionListener para reagir ao pressionar botões
public class Model extends JPanel implements ActionListener{
    //sempre coloca a visibilidade das coisas
    private Dimension d;
    //o param '"Arial"' teve de ser passado com aspas duplas NECESSARIAMENTE
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private boolean inGame = false;
    private boolean dying = false;

    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int MAX_GHOSTS = 12;
    private final int PACMAN_SPEED = 6;

    private int N_GHOSTS = 6;

    private int lives, score;
    private int [] dx, dy;
    private int [] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;

    private Image heart, ghost;
    private Image pacman_up, pacman_down, pacman_right, pacman_left;

    private int pacman_x, pacman_y, pacmand_x, pacmand_y;
    private int req_dx, req_dy;

    private final int validSpeeds[] = {1,2,3,4,6,8};
    private final int maxSpeed = 6;
    private int currentSpeed = 3;
    private short [] screenData;
    private Timer timer;

    //essa parte é responsavel por desenhar os blocos e pontos na tela.
    //cada numero representa algo a ser desenhado.
    //0 são os blocos azuis
    //1 é borda da esquerda
    //2 borda de cima
    //4 borda dadireita
    //8 borda de baixo
    //16 sao os pontos brancos
    private final short levelData[] = {
            19,18,18,18,18,18,18,18,18,18,18,18,18,18,22,
            17,16,16,16,16,24,16,16,16,16,16,16,16,16,20,
            25,24,24,24,28,0,17,16,16,16,16,16,16,16,20,
            0,0,0,0,0,0,17,16,16,16,16,16,16,16,20,
            19,18,18,18,18,18,16,16,16,16,24,24,24,24,20,
            17,16,16,16,16,16,16,16,16,20,0,0,0,0,21,
            17,16,16,16,16,16,16,16,16,20,0,0,0,0,21,
            17,16,16,16,24,16,16,16,16,20,0,0,0,0,21,
            17,16,16,20,0,17,16,16,16,16,18,18,18,18,20,
            17,24,24,28,0,25,24,24,16,16,16,16,16,16,20,
            21,0,0,0,0,0,0,0,17,16,16,16,16,16,20,
            17,18,18,22,0,19,18,18,16,16,16,16,16,16,20,
            17,16,16,20,0,17,16,16,16,16,16,16,16,16,20,
            17,16,16,20,0,17,16,16,16,16,16,16,16,16,20,
            25,24,24,24,26,24,24,24,24,24,24,24,24,24,28
    };

    //construtor
    public Model() {
        //carrega imagens
        loadImages();
        //inicia variaveis
        initVariables();
        //inicia listener das teclas
        addKeyListener(new TAdapter());
        //seta o foco na janela do jogo
        setFocusable(true);
        //inicia o jogo
        initGame();
    }

    private void loadImages(){
        pacman_down = new ImageIcon("../images/down.gif").getImage();
        pacman_up = new ImageIcon("../images/up.gif").getImage();
        pacman_left = new ImageIcon("../images/left.gif").getImage();
        pacman_right = new ImageIcon("../images/right.gif").getImage();
        ghost = new ImageIcon("../images/ghost.gif").getImage();
        heart = new ImageIcon("../images/heart.png").getImage();
    }

    private void initVariables(){
        screenData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];

        //controla de quanto em quanto tempo as coisas são redesenhadas na tela
        //o numero 40 é em miliseconds
        timer = new Timer(40, this);
        //da start no timer
        timer.restart();
    }

    private void initGame(){
        lives = 3;
        score = 0;
        initLevel();
        N_GHOSTS = 6;
        currentSpeed = 3;
    }

    private void initLevel(){
        int i;
        for(i = 0; i < N_BLOCKS*N_BLOCKS; i++) screenData[i] = levelData[i];
    }

    //public void

    //define a posição dos ghosts
    private void continueLevel(){
        int dx = 1;
        int random;

        for(int i = 0; i < N_GHOSTS; i++){
            ghost_x[i] = 4*BLOCK_SIZE;
            ghost_y[i] = 4*BLOCK_SIZE;
            ghost_dx[i] = dx;
            ghost_dy[i] = 0;
            dx = -dx;
            random = (int) Math.random() * (currentSpeed+1);

            if(random > currentSpeed) random = currentSpeed;

            ghostSpeed[i] = validSpeeds[random];
        }

        pacman_x = 7*BLOCK_SIZE;
        pacman_x = 11*BLOCK_SIZE;
        pacmand_x = 0;
        pacmand_y = 0;
        req_dx = 0;
        req_dy = 0;
        dying = false;
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.black);
        g2d.fillRect(0,0,d.width,d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if(inGame) playGame(g2d);
        else showIntroScreen(g2d);

        Toolkit.getDefaultToolkit().sync();
    }

    class TAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (inGame){
                if (key == KeyEvent.VK_LEFT){
                    req_dx = -1;
                    req_dy = 0;
                }
                if (key == KeyEvent.VK_RIGHT){
                    req_dx = 1;
                    req_dy = 0;
                }
                if (key == KeyEvent.VK_UP){
                    req_dx = 0;
                    req_dy = 1;
                }
                if (key == KeyEvent.VK_DOWN){
                    req_dx = 0;
                    req_dy = -1;
                }
                if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) inGame = false;
            } else {
                if(key == KeyEvent.VK_SPACE){
                    inGame = true;
                    initGame();
                }
            }
        }
    }

    //ao implementar ActionListener, temos que implementar aqui todas as funções que existem em ActionListener
    @Override //<- especificamos que estamos sobreescrevendo algo
    public void actionPerformed(ActionEvent e){ /*aqui, recebemos como param ActionEvent, que precisou ser estendido tb*/

    }
}

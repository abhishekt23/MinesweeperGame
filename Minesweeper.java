package com.company;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public class Minesweeper extends JFrame implements ActionListener, MouseListener {

    JToggleButton[][] board;
    JPanel boardPanel;
    boolean firstClick;
    int numMines = 10;
    ImageIcon[] numbers;
    ImageIcon mineIcon, flag, win, lose, play, start;
    GraphicsEnvironment ge;
    Font mineFont, timeFont;
    Timer timer;
    int timePassed;
    JTextField timeField;
    JMenuBar menuBar;
    JMenu menu;
    JMenuItem beg, inter, exp;
    JButton reset;
    boolean gameOn = true;
    int row = 9;
    int column = 9;




    public Minesweeper() {
        //numMines  = 20;
        try {
            ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            mineFont = Font.createFont(Font.TRUETYPE_FONT, new File("/Users/abhishekthakare/Desktop/forAbhi/Data Structures Projects/mine-sweeper.ttf"));
            timeFont = Font.createFont(Font.TRUETYPE_FONT, new File("/Users/abhishekthakare/Desktop/forAbhi/Data Structures Projects/digital-7.ttf"));
            ge.registerFont(mineFont);
        } catch (IOException | FontFormatException e) {

        }
        System.out.println(mineFont);
        System.out.println(timeFont);

        mineIcon = new ImageIcon("mine.png");
        mineIcon = new ImageIcon(mineIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

        flag = new ImageIcon("flag.png");
        flag = new ImageIcon(flag.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

        win = new ImageIcon("win0.png");
        win = new ImageIcon(win.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

        lose = new ImageIcon("lose0.png");
        lose = new ImageIcon(lose.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

        play = new ImageIcon("wait0.png");
        play = new ImageIcon(play.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));

        start = new ImageIcon("smile0.png");
        start = new ImageIcon(start.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));


        numbers = new ImageIcon[8];
        for(int x = 0; x<8; x++) {
            numbers[x] = new ImageIcon(x + 1 + ".png");
            numbers[x] = new ImageIcon(numbers[x].getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        }


        UIManager.put("ToggleButton.select", Color.LIGHT_GRAY);
        firstClick = true;
        createBoard(row,column);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

    }

    public void createBoard(int row, int col) {
        timeField = new JTextField("0");
        timeField.setFont(timeFont.deriveFont(25f));
        timeField.setHorizontalAlignment(JTextField.CENTER);
        timeField.setBackground(Color.BLACK);
        timeField.setForeground(Color.WHITE);

        beg = new JMenuItem("Beginner");
        beg.addActionListener(this);
        inter = new JMenuItem("Intermediate");
        inter.addActionListener(this);
        exp = new JMenuItem("Expert");
        exp.addActionListener(this);
        reset = new JButton("Reset");
        reset.addActionListener(this);
        reset.setIcon(start);
        menu = new JMenu("Difficulties:");
        menuBar = new JMenuBar();
        menuBar.setLayout(new GridLayout());
        menu.add(beg);
        menu.add(inter);
        menu.add(exp);
        menuBar.add(menu);
        menuBar.add(reset);
        menuBar.add(timeField);

        if(boardPanel != null)
            this.remove(boardPanel);
        boardPanel = new JPanel();
        board = new JToggleButton[row][col];
        boardPanel.setLayout(new GridLayout(row, col));
        for(int r = 0; r < row; r++) {
            for(int c = 0; c < col; c++) {
                board[r][c] = new JToggleButton();
                board[r][c].putClientProperty("row", r);
                board[r][c].putClientProperty("column", c);
                board[r][c].putClientProperty("state", 0); //what constitutes a mine? -1
                board[r][c].setFont(mineFont.deriveFont(16f));
                board[r][c].setBorder(BorderFactory.createBevelBorder(0));
                board[r][c].setFocusPainted(false);
                board[r][c].addMouseListener(this);
                boardPanel.add(board[r][c]);
            }
        }
        this.setSize(40*col,40*row);
        this.setJMenuBar(menuBar);
        this.add(boardPanel);
        this.revalidate();
    }

    public void setMinesAndCounts(int currRow, int currCol) {
        int count = numMines;
        int dimR = board.length;
        int dimC = board[0].length;
        while(count>0) {
            int randR = (int)(Math.random()*dimR);
            int randC = (int)(Math.random()*dimC);
            int state = (int) board[randR][randC].getClientProperty("state");
            if(state == 0 && (Math.abs(randR-currRow) > 1 || Math.abs(randC-currCol) > 1) ) {
                board[randR][randC].putClientProperty("state", -1);
                count--;
            }
        }

        for(int r = 0; r < dimR; r++) {
            for(int c = 0; c < dimC; c++) {
                count = 0;
                int currState = (int) board[r][c].getClientProperty("state");
                if(currState != -1) {
                    for(int rSmall = r-1; rSmall <= r+1; rSmall++) {
                        for(int cSmall = c-1; cSmall <= c+1; cSmall++) {
                            try {
                                int toggleState = (int) board[rSmall][cSmall].getClientProperty("state");
                                if(toggleState == -1 && !(rSmall == r && cSmall == c))
                                    count++;
                            }catch (ArrayIndexOutOfBoundsException e) {

                            }
                        }
                    }
                    board[r][c].putClientProperty("state", count);
                }
            }
        }

    }

    public void expand(int row, int col) {
        if(!board[row][col].isSelected()) {
            board[row][col].setSelected(true);
        }
        int state = (int) board[row][col].getClientProperty("state");
        if(state != 0) {
            write(row, col, state);
        }
        else {
            for(int r3x3 = row-1; r3x3 <= row+1; r3x3++) {
                for(int c3x3 = col-1; c3x3 <= col+1; c3x3++) {
                    try {
                        if(!board[r3x3][c3x3].isSelected())
                            expand(r3x3,c3x3);
                    }catch(ArrayIndexOutOfBoundsException e) { }
                }
            }
        }

    }

    public void write(int row, int col, int state){
        if(state > 0) {
            board[row][col].setIcon(numbers[state - 1]);
            board[row][col].setDisabledIcon(numbers[state - 1]);

        }

    }

    public void checkWin() {
        int dimR = board.length;
        int dimC = board[0].length;
        int totalSpaces = dimR*dimC;
        int count = 0;

        for(int r = 0; r<dimR;r++) {
            for(int c=0; c<dimC; c++) {
                int state = (int)board[r][c].getClientProperty("state");
                if(board[r][c].isSelected())
                    count++;
            }
        }
        if(numMines==totalSpaces-count) {
            timer.cancel();
            reset.setIcon(win);
            gameOn = false;
            JOptionPane.showMessageDialog(null, "Winner!");
        }
    }

    public void revealMines() {
        gameOn = false;
        for(int r=0; r<board.length; r++) {
            for(int c = 0; c<board[0].length; c++) {
                int state = (int)board[r][c].getClientProperty("state");
                if(state==-1) {
                    board[r][c].setIcon(mineIcon);
                    board[r][c].setDisabledIcon(mineIcon);
                    board[r][c].setSelected(true);
                }
                    board[r][c].setEnabled(false);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == beg) {
            numMines = 10;
            row = column = 9;
            createBoard(row, column);
        } else if (e.getSource() == inter) {
            numMines = 40;
            row = column = 16;
            createBoard(row, column);
        } else if (e.getSource() == exp) {
            numMines = 99;
            row = 16;
            column = 40;
            createBoard(row, column);
        } else if (e.getSource() == reset) {
            createBoard(row, column);
            timer.cancel();
            timeField.setText("0");
            timePassed = 0;
            firstClick = true;
            gameOn = true;
            reset.setIcon(start);
        }
    }
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int row = (int)((JToggleButton)e.getComponent()).getClientProperty("row");
        int col = (int)((JToggleButton)e.getComponent()).getClientProperty("column");

        if(gameOn) {
            if (e.getButton() == MouseEvent.BUTTON1 && board[row][col].isEnabled()) {
                if (firstClick) {
                    timer = new Timer();
                    timer.schedule(new UpdateTimer(), 0, 1000);
                    setMinesAndCounts(row, col);
                    firstClick = false;
                    gameOn = true;
                    reset.setIcon(play);
                }

                int state = (int) board[row][col].getClientProperty("state");
                if (state == -1) {
                    board[row][col].setIcon(mineIcon);
                    board[row][col].setContentAreaFilled(false);
                    board[row][col].setOpaque(true);
                    board[row][col].setBackground(Color.RED);
                    revealMines();
                    timer.cancel();
                    reset.setIcon(lose);
                    gameOn = false;
                    JOptionPane.showMessageDialog(null, "Loser!");
                    //show all the mines
                    //stop the user from having the ability to click on buttons until they reset the game
                } else {
                    expand(row, col);
                    checkWin();
                }

            }
            if (e.getButton() == MouseEvent.BUTTON3) {
                if (!board[row][col].isSelected()) {
                    if (board[row][col].getIcon() == null) {
                        board[row][col].setIcon(flag);
                        board[row][col].setDisabledIcon(flag);
                        board[row][col].setEnabled(false);
                    } else {
                        board[row][col].setIcon(null);
                        board[row][col].setDisabledIcon(null);
                        board[row][col].setEnabled(true);
                    }
                }
            }
        }


    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }




    public static void main(String[] args) {
        Minesweeper app = new Minesweeper();
    }


    class UpdateTimer extends TimerTask {
        public void run() {
            if(gameOn) {
                timePassed++;
                timeField.setText(timePassed + "");
                System.out.println(timePassed);
            }
        }
    }

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package ics4u.u2l3.tictactoe;

import java.util.*; 
import java.awt.Image;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

/**Dennis Ren
 * Tic Tac Toe
 * April 22 2024
 * A tic tac toe game where you can change the board size, winning length, first player, and reset the board
 * You can also play with a computer or let it play itself
 *
 * @author Dennis
 */
public class GameWindow extends javax.swing.JFrame {

	public GameWindow() {
		initComponents();
		initCustomComponents(); 
	}

	private void initCustomComponents() { 
		try {
			imageX = ImageIO.read(getClass().getResource("/x.png")); 
			imageO = ImageIO.read(getClass().getResource("/o.png")); 
			imageXWin = ImageIO.read(getClass().getResource("/xw.png")); 
			imageOWin = ImageIO.read(getClass().getResource("/ow.png")); 
			imageXTie = ImageIO.read(getClass().getResource("/xt.png")); 
			imageOTie = ImageIO.read(getClass().getResource("/ot.png")); 
			imageDefault = ImageIO.read(getClass().getResource("/def.png")); 
		} catch (IOException ex) { 
			return; 
		}

		this.timerSelfPlay = null; 
		game = new TicTac(); 
		game.setSize("3"); 
		game.setInaRow("3"); 
		btnArray = new JLabel[game.getSize()][game.getSize()]; 
		playerOneFirst = true; 

		setupNewBoard(); 
		game.setPlayerComputer(true); 
	}

	boolean playerOneFirst; 
	int width, height; 
	TicTac game; 
	JLabel[][] btnArray; 
	BoardListener boardListener = new BoardListener(this); 

	BufferedImage imageX; 
	BufferedImage imageO; 
	BufferedImage imageXWin; 
	BufferedImage imageOWin; 
	BufferedImage imageXTie; 
	BufferedImage imageOTie; 
	BufferedImage imageDefault; 
	ImageIcon imageXI; 
	ImageIcon imageOI; 
	ImageIcon imageXWinI; 
	ImageIcon imageOWinI; 
	ImageIcon imageXTieI; 
	ImageIcon imageOTieI; 
	ImageIcon imageDefaultI; 

	//the timer that calls the cpMatch function repeatedly
	javax.swing.Timer timerSelfPlay; 


	private void updateDimensions() { 
		width = (panelContainer.getSize().width) / game.getSize();
		height = width; 
		imageXI = new ImageIcon(imageX.getScaledInstance(width, height, Image.SCALE_FAST));
		imageOI = new ImageIcon(imageO.getScaledInstance(width, height, Image.SCALE_FAST));
		imageXWinI = new ImageIcon(imageXWin.getScaledInstance(width, height, Image.SCALE_FAST));
		imageOWinI = new ImageIcon(imageOWin.getScaledInstance(width, height, Image.SCALE_FAST));
		imageXTieI = new ImageIcon(imageXTie.getScaledInstance(width, height, Image.SCALE_FAST));
		imageOTieI = new ImageIcon(imageOTie.getScaledInstance(width, height, Image.SCALE_FAST));
		imageDefaultI = new ImageIcon(imageDefault.getScaledInstance(width, height, Image.SCALE_FAST));
	}

	private void clearBoard() { 
		for (var coord : game.getAllCoords()) { 
			btnArray[coord.get(0)][coord.get(1)].setIcon(imageDefaultI); 
		}
		game.clearBoard(); 
	}

	private void setupNewBoard() { 
		var layout = new java.awt.GridLayout(game.getSize(), game.getSize()); 
		layout.setHgap(0); 
		layout.setVgap(0); 
		panelContainer.removeAll(); 
		panelContainer.setLayout(layout); 
		btnArray = new JLabel[game.getSize()][game.getSize()]; 
		updateDimensions(); 
		for (int i = 0; i < game.getSize(); i++) { 
			for (int j = 0; j < game.getSize(); j++) { 
				JLabel cur = new JLabel(imageDefaultI); 
				cur.addMouseListener(boardListener); 
				cur.setName(i + " " + j); 
				btnArray[i][j] = cur; 
				panelContainer.add(cur); 
			}
		}
		panelContainer.revalidate(); 
		panelContainer.repaint(); 

		game.clearBoard(); //clears the game board data
	}

	public void reqMoveInput(int x, int y) { 
		String playerName; 
		if (playerOneFirst ^ !game.turnX) { 
			playerName = game.onePlayer ? "Human" : "Player 1"; 
		} else {
			playerName = game.onePlayer ? "Computer" : "Player 2"; 
		}
		if (playerOneFirst ^ game.turnX && game.onePlayer) { //if it's the computer's turn in a one player game
			List<Integer> cpMove = game.getComputerMove(); 
			x = cpMove.get(0); 
			y = cpMove.get(1); 
		}
		int[][] winningCoords = game.reqMove(x, y, (playerOneFirst ^ !game.turnX) ? 1 : 2); //perform the move and check for winner

		if (winningCoords == null) { 
			return; 
		} else if (winningCoords.length == 0) { //if the move resulted in a draw
			consolelog(playerName + " placed on [" + y + ", " + x + "]"); 
			var img = game.turnX ? imageOI : imageXI; 
			drawPiece(x, y, img); 
		} else if (winningCoords[0][0] < 0) { //if there was a tie
			consolelog("There was a tie!"); 
			drawTie(); 
		} else { //if there is a winner
			consolelog(playerName + " wins with " + "[" + y + ", " + x + "]!"); 
			var img = game.turnX ? imageXWinI : imageOWinI; 
			for (var coord : winningCoords) { 
				drawPiece(coord[0], coord[1], img); 
			}
		}
		if (playerOneFirst ^ game.turnX && game.onePlayer) { //if it's the computer's turn in a one player game after the current move
			reqMoveInput(-1, -1); 
		}
	}


	private void consolelog(String s) { 
		txtDialog.setText(txtDialog.getText() + "\n" + "> " + s + "\n"); 
	}

	private void drawPiece(int x, int y, ImageIcon thisImage) { 
		btnArray[x][y].setIcon(thisImage); 
	}

	private void drawTie() { 
		for (int i = 0; i < game.getSize(); i++) { 
			for (int j = 0; j < game.getSize(); j++) { 
				if(game.bMap.get(Arrays.asList(i,j)).get(0) > 0) { 
					btnArray[i][j].setIcon(imageXTieI); 
				}
				else btnArray[i][j].setIcon(imageOTieI); 
			}
		}
	}

	private String updateBtnPlayFirst() { 
		String firstplayer; 
		if (game.onePlayer) { 
			firstplayer = playerOneFirst ? "Human" : "Computer (on click)"; 
		} else {
			firstplayer = playerOneFirst ? "Player 1" : "Player 2"; 
		}
		btnPlayFirst.setText("Playing first: " + firstplayer); 
		return firstplayer; 
	}


	/**
	 * This method is called from within the constructor to initialize the
	 * form. WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                panelMain = new javax.swing.JPanel();
                labelGame = new javax.swing.JLabel();
                scrlDialog = new javax.swing.JScrollPane();
                txtDialog = new javax.swing.JTextArea();
                btnStat = new javax.swing.JButton();
                txtInput = new javax.swing.JTextField();
                panelContainer = new javax.swing.JPanel();
                jTextField2 = new javax.swing.JTextField();
                btnLen = new javax.swing.JButton();
                btnSize = new javax.swing.JButton();
                btnSelfPlay = new javax.swing.JButton();
                btnClearBoard = new javax.swing.JButton();
                btnResetStat = new javax.swing.JButton();
                btnPlayFirst = new javax.swing.JButton();
                labelInaRow = new javax.swing.JLabel();
                btnPlayerMode = new javax.swing.JButton();
                btnStat1 = new javax.swing.JButton();
                labelSize = new javax.swing.JLabel();

                setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
                setTitle("Tic Tac Toe");

                panelMain.setBackground(new java.awt.Color(0, 0, 0));
                panelMain.setForeground(new java.awt.Color(51, 51, 255));
                panelMain.setPreferredSize(new java.awt.Dimension(1451, 1130));

                labelGame.setFont(new java.awt.Font("DialogInput", 0, 48)); // NOI18N
                labelGame.setForeground(java.awt.Color.green);
                labelGame.setText("Tic Tac Toe");
                labelGame.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

                scrlDialog.setBackground(java.awt.Color.black);
                scrlDialog.setBorder(null);
                scrlDialog.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scrlDialog.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

                txtDialog.setEditable(false);
                txtDialog.setBackground(java.awt.Color.black);
                txtDialog.setColumns(20);
                txtDialog.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                txtDialog.setForeground(java.awt.Color.green);
                txtDialog.setLineWrap(true);
                txtDialog.setRows(5);
                txtDialog.setText("> Here are a couple sizes to try\nPopular games:\nGomoku: 15x15, length 5, favours first player\n\nDrawn games: \n5x5, length 4\n7x7, length 5\n8x8, length 5\n\n");
                txtDialog.setWrapStyleWord(true);
                txtDialog.setBorder(null);
                txtDialog.setCaretColor(java.awt.Color.green);
                txtDialog.setDisabledTextColor(java.awt.Color.black);
                txtDialog.setFocusable(false);
                txtDialog.setHighlighter(null);
                txtDialog.setSelectedTextColor(java.awt.Color.black);
                txtDialog.setSelectionColor(java.awt.Color.green);
                scrlDialog.setViewportView(txtDialog);

                btnStat.setBackground(java.awt.Color.black);
                btnStat.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                btnStat.setForeground(java.awt.Color.green);
                btnStat.setText("Show Stats");
                btnStat.setBorder(null);
                btnStat.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnStat.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnStatActionPerformed(evt);
                        }
                });

                txtInput.setBackground(java.awt.Color.black);
                txtInput.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                txtInput.setForeground(java.awt.Color.green);
                txtInput.setHorizontalAlignment(javax.swing.JTextField.LEFT);
                txtInput.setToolTipText("");
                txtInput.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.green));
                txtInput.setCaretColor(java.awt.Color.green);
                txtInput.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
                txtInput.setName(""); // NOI18N
                txtInput.setSelectedTextColor(java.awt.Color.black);
                txtInput.setSelectionColor(java.awt.Color.green);
                txtInput.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                txtInputActionPerformed(evt);
                        }
                });

                panelContainer.setBackground(new java.awt.Color(0, 0, 0));
                panelContainer.setMaximumSize(new java.awt.Dimension(1024, 1024));
                panelContainer.setMinimumSize(new java.awt.Dimension(1024, 1024));
                panelContainer.setPreferredSize(new java.awt.Dimension(1024, 1024));

                javax.swing.GroupLayout panelContainerLayout = new javax.swing.GroupLayout(panelContainer);
                panelContainer.setLayout(panelContainerLayout);
                panelContainerLayout.setHorizontalGroup(
                        panelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 1024, Short.MAX_VALUE)
                );
                panelContainerLayout.setVerticalGroup(
                        panelContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 1024, Short.MAX_VALUE)
                );

                jTextField2.setEditable(false);
                jTextField2.setBackground(java.awt.Color.black);
                jTextField2.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                jTextField2.setForeground(java.awt.Color.green);
                jTextField2.setHorizontalAlignment(javax.swing.JTextField.LEFT);
                jTextField2.setText(">");
                jTextField2.setBorder(null);
                jTextField2.setCaretColor(java.awt.Color.green);
                jTextField2.setSelectedTextColor(java.awt.Color.black);
                jTextField2.setSelectionColor(java.awt.Color.green);

                btnLen.setBackground(java.awt.Color.black);
                btnLen.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                btnLen.setForeground(java.awt.Color.green);
                btnLen.setText("Set Winning Length");
                btnLen.setBorder(null);
                btnLen.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnLen.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnLenActionPerformed(evt);
                        }
                });

                btnSize.setBackground(java.awt.Color.black);
                btnSize.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                btnSize.setForeground(java.awt.Color.green);
                btnSize.setText("Set Board Size");
                btnSize.setBorder(null);
                btnSize.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnSize.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnSizeActionPerformed(evt);
                        }
                });

                btnSelfPlay.setBackground(java.awt.Color.black);
                btnSelfPlay.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                btnSelfPlay.setForeground(java.awt.Color.green);
                btnSelfPlay.setText("Toggle computer selfplay (for fun)");
                btnSelfPlay.setBorder(null);
                btnSelfPlay.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnSelfPlay.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnSelfPlayActionPerformed(evt);
                        }
                });

                btnClearBoard.setBackground(java.awt.Color.black);
                btnClearBoard.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                btnClearBoard.setForeground(java.awt.Color.red);
                btnClearBoard.setText("Reset Board");
                btnClearBoard.setBorder(null);
                btnClearBoard.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnClearBoard.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnClearBoardActionPerformed(evt);
                        }
                });

                btnResetStat.setBackground(java.awt.Color.black);
                btnResetStat.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                btnResetStat.setForeground(java.awt.Color.green);
                btnResetStat.setText("Reset Statistics");
                btnResetStat.setBorder(null);
                btnResetStat.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnResetStat.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnResetStatActionPerformed(evt);
                        }
                });

                btnPlayFirst.setBackground(java.awt.Color.black);
                btnPlayFirst.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                btnPlayFirst.setForeground(java.awt.Color.green);
                btnPlayFirst.setText("Playing First: Player 1");
                btnPlayFirst.setBorder(null);
                btnPlayFirst.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnPlayFirst.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnPlayFirstActionPerformed(evt);
                        }
                });

                labelInaRow.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                labelInaRow.setForeground(java.awt.Color.green);
                labelInaRow.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
                labelInaRow.setText("3 in a row");
                labelInaRow.setVerticalAlignment(javax.swing.SwingConstants.TOP);

                btnPlayerMode.setBackground(java.awt.Color.black);
                btnPlayerMode.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                btnPlayerMode.setForeground(java.awt.Color.green);
                btnPlayerMode.setText("Switch to Two Player Mode");
                btnPlayerMode.setBorder(null);
                btnPlayerMode.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnPlayerMode.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnPlayerModeActionPerformed(evt);
                        }
                });

                btnStat1.setBackground(java.awt.Color.black);
                btnStat1.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                btnStat1.setForeground(java.awt.Color.green);
                btnStat1.setText("Clear Console");
                btnStat1.setBorder(null);
                btnStat1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                btnStat1.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnStat1ActionPerformed(evt);
                        }
                });

                labelSize.setFont(new java.awt.Font("DialogInput", 0, 18)); // NOI18N
                labelSize.setForeground(java.awt.Color.green);
                labelSize.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                labelSize.setText("3x3");
                labelSize.setVerticalAlignment(javax.swing.SwingConstants.TOP);

                javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
                panelMain.setLayout(panelMainLayout);
                panelMainLayout.setHorizontalGroup(
                        panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelMainLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(labelGame, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnLen)
                                        .addComponent(btnSize)
                                        .addComponent(btnSelfPlay)
                                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addGroup(panelMainLayout.createSequentialGroup()
                                                        .addComponent(labelSize, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(labelInaRow, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(scrlDialog, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(panelMainLayout.createSequentialGroup()
                                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(panelMainLayout.createSequentialGroup()
                                                        .addComponent(btnStat)
                                                        .addGap(103, 103, 103)
                                                        .addComponent(btnStat1)))
                                        .addComponent(btnPlayerMode)
                                        .addComponent(btnPlayFirst)
                                        .addComponent(btnResetStat)
                                        .addComponent(btnClearBoard))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(panelContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 19, Short.MAX_VALUE))
                );
                panelMainLayout.setVerticalGroup(
                        panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelMainLayout.createSequentialGroup()
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelMainLayout.createSequentialGroup()
                                                .addComponent(labelGame, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(labelInaRow)
                                                        .addComponent(labelSize))
                                                .addGap(18, 18, 18)
                                                .addComponent(btnPlayerMode)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnPlayFirst)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnResetStat)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnClearBoard)
                                                .addGap(12, 12, 12)
                                                .addComponent(scrlDialog, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(txtInput, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(btnStat)
                                                        .addComponent(btnStat1))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnSize)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(btnLen)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnSelfPlay))
                                        .addComponent(panelContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(panelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(panelMain, javax.swing.GroupLayout.PREFERRED_SIZE, 1030, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

        private void btnStatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatActionPerformed
		String p1 = !game.onePlayer ? "Player 1" : "Human";
		String p2 = !game.onePlayer ? "Player 2" : "Computer";

		boolean winStreak1 = game.getStat(TicTac.Stat.WIN_STREAK, 1) > 0;

		String msg = "Stats for " + p1 + ":\n"
		    + "Wins: " + game.getStat(TicTac.Stat.WINS, 1) + "\n"
		    + "Losses: " + game.getStat(TicTac.Stat.LOSSES, 1) + "\n"
		    + "Ties: " + game.getStat(TicTac.Stat.TIES, 1) + "\n";
		msg += winStreak1 ? "Win streak: " : "Loss streak: ";
		msg += winStreak1 ? game.getStat(TicTac.Stat.WIN_STREAK, 1) : game.getStat(TicTac.Stat.LOSS_STREAK, 1);
		msg += "\n";
		msg += "Stats for " + p2 + ":\n"
		    + "Wins: " + game.getStat(TicTac.Stat.WINS, 2) + "\n"
		    + "Losses: " + game.getStat(TicTac.Stat.LOSSES, 2) + "\n"
		    + "Ties: " + game.getStat(TicTac.Stat.TIES, 2) + "\n";
		msg += !winStreak1 ? "Win streak: " : "Loss streak: ";
		msg += !winStreak1 ? game.getStat(TicTac.Stat.WIN_STREAK, 2) : game.getStat(TicTac.Stat.LOSS_STREAK, 2);
		consolelog(msg);
        }//GEN-LAST:event_btnStatActionPerformed

        private void btnLenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLenActionPerformed
		consolelog(game.setInaRow(txtInput.getText())); 
		labelInaRow.setText(game.getInaRow() + " in a row"); 
        }//GEN-LAST:event_btnLenActionPerformed

        private void btnSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSizeActionPerformed
		consolelog(game.setSize(txtInput.getText())); 
		if (game.numPieces() == 0) { 
			setupNewBoard(); 
		}
		labelInaRow.setText(game.getInaRow() + " in a row"); 
		labelSize.setText(game.getSize() + "x" + game.getSize()); 
        }//GEN-LAST:event_btnSizeActionPerformed

        private void btnSelfPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelfPlayActionPerformed

		if(timerSelfPlay != null && timerSelfPlay.isRunning()) { 
			consolelog("Stopped self play."); 
			timerSelfPlay.stop(); 
			return; 
		}
		consolelog("Performing self play until toggled off or game finishes"); 
		timerSelfPlay = new javax.swing.Timer(3000/(game.getSize() * game.getSize()), null); 
		timerSelfPlay.addActionListener(e-> { 
			if(cpMatch()){ 
				timerSelfPlay.stop(); 
			}
		});
		timerSelfPlay.setRepeats(true); 
		timerSelfPlay.start(); 
        }//GEN-LAST:event_btnSelfPlayActionPerformed

        private void btnClearBoardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearBoardActionPerformed
		if(timerSelfPlay!= null) timerSelfPlay.stop(); 
		clearBoard(); 
		consolelog(!playerOneFirst && game.onePlayer? "Click anywhere for the computer to play" : "Click anywhere to play"); 
        }//GEN-LAST:event_btnClearBoardActionPerformed

        private void btnResetStatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetStatActionPerformed
		game.resetStats(); 
		consolelog("Stats reset!"); 
        }//GEN-LAST:event_btnResetStatActionPerformed

        private void btnPlayFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayFirstActionPerformed
		if(game.numPieces() != 0) { 
			consolelog("Value can't be set when the game has started!"); 
			return; 
		}
		playerOneFirst = !playerOneFirst; 
		
		consolelog(updateBtnPlayFirst() + " now plays first when the board is clear."); 
        }//GEN-LAST:event_btnPlayFirstActionPerformed

        private void txtInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtInputActionPerformed
		// TODO add your handling code here:
        }//GEN-LAST:event_txtInputActionPerformed
	private boolean cpMatch() { 
		int x,y; 
		List<Integer> cpMove = game.getComputerMove(); 
		x = cpMove.get(0); 
		y = cpMove.get(1); 
		int[][] winningCoords = game.reqMove(x, y, playerOneFirst ^ !game.turnX? 1 : 2); 
		if (winningCoords == null) { 
			consolelog("Couldn't find move"); 
			return true; 
		} else if (winningCoords.length == 0) { 
			var img = game.turnX ? imageOI : imageXI; 
			drawPiece(x, y, img); 
		} else if (winningCoords[0][0] < 0) { 
			String playerName1, playerName2; 
			playerName1 = game.onePlayer ? "Human" : "Player 1"; 
			playerName2 = game.onePlayer ? "Computer" : "Player 2"; 
			consolelog("Computer draws a tie on behalf of " + playerName1 + " and " + playerName2 + "!"); 
			drawTie(); 
			return true; 
		} else { 
			String playerName; 
			boolean winP1 = playerOneFirst ^ !game.turnX; 
			if (winP1) { 
				playerName = game.onePlayer ? "Human" : "Player 1"; 
			} else { 
				playerName = game.onePlayer ? "Computer" : "Player 2"; 
			}
			consolelog("Computer wins on behalf of " + playerName +"!"); 
			var img = game.turnX ? imageXWinI : imageOWinI; 
			for (var coord : winningCoords) { 
				drawPiece(coord[0], coord[1], img); 
			}
			return true; 
		}
		return false; 
	}


	
        private void btnPlayerModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayerModeActionPerformed
		consolelog(game.setPlayerComputer(!game.onePlayer)); 
		String msg = "Switch to "; 
		msg += !game.onePlayer ? "One" : "Two"; 
		msg += " Player mode"; 
		btnPlayerMode.setText(msg); 
		updateBtnPlayFirst(); 
        }//GEN-LAST:event_btnPlayerModeActionPerformed
	
        private void btnStat1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStat1ActionPerformed
		txtDialog.setText("> Here are a couple sizes to try\n"
		    +"Popular games:\n"
		    + "Gomoku: 15x15, length 5, favours first player\n\n"
		    + "Drawn games: \n"
		    + "5x5, length 4\n"
		    + "7x7, length 5\n"
		    +"8x8, length 5\n");
        }//GEN-LAST:event_btnStat1ActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(GameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(GameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(GameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(GameWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new GameWindow().setVisible(true);
			}
		});
	}

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton btnClearBoard;
        private javax.swing.JButton btnLen;
        private javax.swing.JButton btnPlayFirst;
        private javax.swing.JButton btnPlayerMode;
        private javax.swing.JButton btnResetStat;
        private javax.swing.JButton btnSelfPlay;
        private javax.swing.JButton btnSize;
        private javax.swing.JButton btnStat;
        private javax.swing.JButton btnStat1;
        private javax.swing.JTextField jTextField2;
        private javax.swing.JLabel labelGame;
        private javax.swing.JLabel labelInaRow;
        private javax.swing.JLabel labelSize;
        private javax.swing.JPanel panelContainer;
        private javax.swing.JPanel panelMain;
        private javax.swing.JScrollPane scrlDialog;
        private javax.swing.JTextArea txtDialog;
        private javax.swing.JTextField txtInput;
        // End of variables declaration//GEN-END:variables

}

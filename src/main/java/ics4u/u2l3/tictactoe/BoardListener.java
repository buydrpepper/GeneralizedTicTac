/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ics4u.u2l3.tictactoe;
import javax.swing.*;
import java.awt.event.*; 

/**
 *
 * @author Dennis
 */
//the board lisetener for the tictactoe game, only implements "mousepressed" to request input for now
public class BoardListener implements MouseListener{

	GameWindow thisWindow; 
	
	/**Initializer for the board listener
	 *preconditions: the gui has a reqMoveInput method that requests move input
	 * @param gui the main game window to request input from
	 */
	public BoardListener(GameWindow gui) {
		super();
		thisWindow = gui;
	}

	/** does nothing
	 *
	 * @param e
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	/**Event handler for mousePressed
	 *preconditions: all variables in thisWindow are set up and valid
	 * postconditions: the reqMoveInput method was called at the mouse coordinates
	 * @param e the mouseevent
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		JLabel target = (JLabel) e.getComponent(); 
		String[] name = target.getName().split(" "); 
		int x = Integer.parseInt(name[0]); 
		int y = Integer.parseInt(name[1]); 
		thisWindow.reqMoveInput(x, y); 
	}

	/**does nothing
	 *
	 * @param e
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	/**does nothing
	 *
	 * @param e
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**does nothing
	 *
	 * @param e
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}
	
}

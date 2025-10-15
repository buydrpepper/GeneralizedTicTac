/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ics4u.u2l3.tictactoe;
import java.util.*;

/**
 *
 * @author Dennis
 */
public class TicTac {
	public enum Stat {
		WINS,
		LOSSES,
		TIES,
		WIN_STREAK,
		LOSS_STREAK
	}

	private Map<Stat, Integer> statP1;
	private Map<Stat, Integer> statP2;

	private Map<Stat, Integer> statHuman;
	private Map<Stat, Integer> statComputer;
	private final int MAX_SIZE = 128;

	private int bsize;
	
	//Key: Coordinates of the x or o 
	//NOTE: Values are updated only if the piece endpoint of a line, never updated after it stops being an endpoint
	//Magnitude of the Values: The length of the lines it's part of, in the order of the Lines.arrayInd set below.
	//The sign is negative if and only if it's a an "o" piece, and positive only if it's an "x" piece that the line is part of.
	Map<List<Integer>, List<Integer>> bMap; 

	//enum for the winning line directions, always prioritizing left->right, then up->down
	public enum Lines { 
		RIGHT(0, 1,0), //(horizontal) line: value stored in index [0], lacks vertical slope
		UP(1,0,1), //(vertical) line: zero change in x, lenght stored in array[1]
		UPRIGHT(2,1,1), //diagonal upright line: positive slope = 1, length stored in array [2]
		DOWNRIGHT(3,1,-1); //diagonal downright line: negative slope, legnth stored in array[3]//diagonal downright line: negative slope, legnth stored in array[3]
		public final int arrayInd; //declare the index of the length array it's stored in
		public final int[] offset; //declares the delta {x, y} when scanning for winners 
		/**
		 * Initalizer for line
		 * @param arrayInd the array index
		 * @param dx the change in x per sweep
		 * @param dy  the change in y per sweep
		 */
		Lines(int arrayInd, int dx, int dy) { 
			this.offset = new int[]{dx, dy};
			this.arrayInd = arrayInd;
		}
	}

	public boolean turnX;
	public boolean onePlayer;
	private int inaRow; 
	private boolean canPlace; 
	private ComputerPlayer cp;
	
	/**Initializer for tictac
	 * Precondition: none
	 * Postcondition:
	 * 	board size, in a row is 3 by default
	 * 	X should always play first
	 */
	public TicTac () { 
		bMap = new HashMap<>();
		inaRow = 3;
		canPlace = true;
		bsize = 3;
		turnX = true;
		onePlayer = true;
		resetStats();
		cp = new ComputerPlayer();
		cp.boardSize = bsize;
	}

	/**Sets the winner of the game
	 * preconditions: none
	 * postconditions: Only the statistic maps are mutated
	 * @param player the player to win
	 */
	private void setWinner(int player) { 
		Map<Stat, Integer> winner, loser;
		if(onePlayer) {
			winner = statHuman;
			loser = statComputer;
		}
		else {
			winner = statP1;
			loser = statP2;
		}
		if(player == 2) {
			var tmp = winner;
			winner = loser;
			loser = tmp;
		}
		winner.put(Stat.WINS, winner.get(Stat.WINS) + 1);
		winner.put(Stat.WIN_STREAK, winner.get(Stat.WIN_STREAK) + 1);
		winner.put(Stat.LOSS_STREAK, 0);
		loser.put(Stat.LOSSES, loser.get(Stat.LOSSES) + 1);
		loser.put(Stat.LOSS_STREAK, loser.get(Stat.LOSS_STREAK) + 1);
		loser.put(Stat.WIN_STREAK, 0);
	}

	/**Sets a tie in the game
	 * preconditions: none
	 * postconditions: no side effects other than mutating stats
	 */
	private void setTie() {
		Map<Stat, Integer> P1, P2;
		P1 = onePlayer? statHuman : statP1;
		P2 = onePlayer? statComputer : statP2;
		P1.put(Stat.TIES, P1.get(Stat.TIES) + 1);
		P2.put(Stat.TIES, P2.get(Stat.TIES) + 1);
	}

	/**Does a move at the passed indices
	 * preconditions: turnX properly updated, game is not already won
	 * postconditions: turnX properly updated
	 * @param ind1 the first index
	 * @param ind2 the second index
	 * @return null if the move is invalid, an empty array if the move was valid, 
	 * an array with values {forward length, backward length}, {line.offset} if there was a winning line
	 */
	private int[][] doMove(int ind1, int ind2) { 
		if(ind1 >= bsize || ind2 >= bsize) return null;
		int fac = turnX? 1 : -1;
		if(bMap.putIfAbsent(Arrays.asList(ind1, ind2), Arrays.asList(fac, fac, fac, fac)) != null)
			return null;

		for(Lines line : Lines.values()) {
			var cur = bMap.get(Arrays.asList(ind1 + line.offset[0], ind2 + line.offset[1]));
			int forward = (cur != null)? cur.get(line.arrayInd) * fac : 0; //calculate forward score
			cur = bMap.get(Arrays.asList(ind1 - line.offset[0], ind2 - line.offset[1])); //get current value in the backward direction
			int backward = (cur != null)? cur.get(line.arrayInd) * fac : 0; //calculate backward score
			if(forward < 0) forward = 0;
			if(backward < 0) backward = 0;
			int score = forward + backward + 1; //calculate total score for the line
			if(score >= inaRow) return new int[][]{{forward, backward}, line.offset}; //if score meets winning condition, return winning coordinates
			score *= fac; //update score based on player turn
			bMap.get(Arrays.asList(ind1 + line.offset[0] * forward, ind2 + line.offset[1] * forward)).set(line.arrayInd, score); //update forward values
			bMap.get(Arrays.asList(ind1 - line.offset[0] * backward, ind2 - line.offset[1] * backward)).set(line.arrayInd, score); //update backward values
		}
		turnX = !turnX;
		return new int[][]{}; //return empty array indicating valid move
	}

	/**Get all coordinates on the game board
	 * @return a set of all coordinates on the board
	 */
	public Set<List<Integer>> getAllCoords() { 
		return bMap.keySet();
	}

	/**Get the computer's move
	 * preconditions: none
	 * postconditions: no side effects
	 * @return the coordinates the computer wants to move to, negative coordinates if the computer can't place
	 */
	public List<Integer> getComputerMove() { 
		if(!canPlace) return Arrays.asList(-1, -1);
		return cp.getMove(bMap, turnX, bsize, inaRow); 
	}


	/** Requests a move from a certain player
	 * preconditions: player is in bounds (1, 2), turnX properly updated
	 * postconditions: board map is properly updated, turnX properly updated
	 * @param ind1 the first index
	 * @param ind2 the second index
	 * @param player the player code (1 or 2) 
	 * @return 
	 * 	if move failed: null
	 * 	if move succeeded and no end condition: empty array
	 * 	if there was a tie: {{-1}} (neg coordinates)
	 * 	if there was a winner: winning "line" coordinates to draw
	 */
	public int[][] reqMove(int ind1, int ind2, int player) {
		if(!canPlace) return null;
		int [][] moveRes;
		moveRes = doMove(ind1, ind2);
		if(moveRes == null) return null;
		if(moveRes.length == 0) {
			if(bMap.size() == bsize * bsize) {
				setTie();
				canPlace = false;
				return new int[][]{{-1}};
			}
			return moveRes;
		} 

		//winning move detected:
		int[] offset = moveRes[1];
		int[][] ret = new int[inaRow][2];
		int forward = moveRes[0][0];
		int backward = moveRes[0][1];
		//Builds the winning line of coordinates
		for(int i = 0; i < inaRow; i++) { 
			ret[i][0] = ind1 + offset[0] * (i - backward); 
			ret[i][1] = ind2 + offset[1] * (i - backward);
		}
		setWinner(player); 
		canPlace = false; 
		return ret; 
	}

	/**method to set one player vs 2 player mode
	 * preconditions: none
	 * postconditions: no side effects other than flipping the onePlayer flag
	 * @param b whether to switch to vs. computer mode
	 * @return the success / error message
	 */
	public String setPlayerComputer(boolean b) { 
		if(!bMap.isEmpty()) return "Value can only be set when board is clear"; 
		onePlayer = b; 
		return onePlayer?  "You are now playing a computer!" : "You are now playing a human!"; 
	}

	/**Gets winning length
	 * @return  the winning length
	 */
	public int getInaRow() { 
		return inaRow; 
	}

	/**Sets the size of the game board
	 * preconditions: none
	 * postconditions: No side effects if setting the value fails
	 * @param inputSize the user input for size
	 * @return the error/success message
	 */
	public String setSize(String inputSize) { 
		if(!bMap.isEmpty()) return "Value can only be set when board is clear"; 
		int s; 
		try {
			s = Integer.parseInt(inputSize); 
		}
		catch(NumberFormatException ex) { 
			return "Size must be an integer!"; 
		}
		if(s <= 1) return "Size must be greater than 1"; 
		if(s > MAX_SIZE) return "Size must be less than " + MAX_SIZE; 
		bsize = s; 
		if(bsize < inaRow) setInaRow(String.valueOf(bsize)); 
		return "Size set to " + s; 
	}

	/**Requests to set the winning length
	 * preconditions: none
	 * postconditions: If setting the value fails, no side effects
	 * @param input the user input 
	 * @return  the error / success message
	 */
	public String setInaRow(String input) { 
		if(!bMap.isEmpty()) return "Value can only be set when board is clear"; 
		int s; 
		try {
			s = Integer.parseInt(input); 
		}
		catch(NumberFormatException ex) { 
			return "Size must be an integer!"; 
		}
		if(s > bsize) return "Impossible to win!"; 
		if(s <= 0) return "Value must be greater than zero!"; 

		inaRow = s; 

		return "Players now win by playing " + s + " in a row!"; 
	}

	/**Gets the number of pieces on the board
	 * @return the size of the board map
	 */
	public int numPieces() { 
		return bMap.size(); 
	}

	/** resets and reinitializes statistics maps
	 * preconditions: none
	 * postconditions: all statistics maps are properly initialized with all keys mapped to 0
	 */
	public final void resetStats() {
		Map defaultStat = new HashMap<>(); 
		for(var s : Stat.values()) { 
		    defaultStat.put(s, 0); 
		}
		statP1 = new HashMap<>(defaultStat); 
		statP2 = new HashMap<>(defaultStat); 
		statComputer = new HashMap<>(defaultStat); 
		statHuman = new HashMap<>(defaultStat); 
	}

	/**Request to clear the game board
	 * preconditions: none
	 * postconditions: Board is clear, canPlace, turnX updated properly, game is ready to be played again
	 */
	public void clearBoard() { 
		bMap.clear(); 
		canPlace = true; 
		turnX = true; 
	}

	/**Gets the size of the board
	 * preconditions: none
	 * postconditions: no side effects
	 * @return the size (width / height) of the board
	 */
	public int getSize() { 
		return bsize; 
	}

	/** gets a statistic value from a player
	 * 
	 * preconditions: none
	 * postconditions: no side effects
	 * @param s the statistic to return
	 * @param player the player (1 or 2)
	 * @return the integer value of the statistic
	 */
	public int getStat (Stat s, int player) { 
		Integer val; 
		Map<Stat, Integer> toGet; 
		switch(player) { 
			case 1 -> toGet = onePlayer? statHuman: statP1; 
			case 2 -> toGet = onePlayer? statComputer : statP2; 
			default -> { 
				return 0; 
			}
		}
		val = toGet.get(s); 
		return val; 
	}







}

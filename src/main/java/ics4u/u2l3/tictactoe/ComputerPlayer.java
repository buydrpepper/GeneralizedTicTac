/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ics4u.u2l3.tictactoe;
import ics4u.u2l3.tictactoe.TicTac.Lines; 
import java.util.*;


/** For those who were brought here from my resume:
 * Q: Is the algorithm O(log n) per move?
 * A: Ideally, yes. The implementation here is not.
 * This is because the implementation here is stateless (for the caller) for stability / debugging purposes and will rebuild the BST every time
 * This can be adapted to O(k*log n ) per move (where k a constant dependent on the winning lengths)
 * by keeping the previous BST, and editing it. However, I haven't implemented this yet.
 */

/**The computer player for tic tac toe. Can play for any board size / winning length, plays a move given a board map
 * - plays the first move in the middle of the board
 * - plays a random best move if multiple "best" moves are possible
 * - 
 * - decides by checking every "line", faster than checking every row and column
 * - not perfect but scales well for larger boards where its heuristics are far more consistent than human guesses
 *
 * @author Dennis
 */
public class ComputerPlayer {
	public ComputerPlayer() {
		myPotential = new HashMap<>(); 
		opPotential = new HashMap<>();
	}

	//Maps coordinates (x,y) -> list of "potentials" (the overlaps between the coordinate and winning lines)
	//If a winning line has n pieces on it, the potential is n. Only maps free board positions and positions with winning lines on them
	//if a piece has multiple "potentials" (multiple winning lines overlap) all will be added to the list
	Map<List<Integer>, List<Integer>> myPotential; 
	Map<List<Integer>, List<Integer>> opPotential; 

	int boardSize; 
	int myMaxPot;
	int opMaxPot;
	int inaRow;
	
	/**Gets the best move for certain priority maps based on the internal state
	 * Preconditions:
	 * 	Called after internal state was updated for the current game
	 * Post conditions:
	 * 	No side effects
	 * 
	 * @param pfor the priority map to play for
	 * @param pagainst the priority map to play against
	 * @return the coordinates (x,y) of its move
	 */
	private List<Integer> bestMove(Map<List<Integer>, List<Integer>> pfor, Map<List<Integer>, List<Integer>> pagainst) {
		if(pfor.isEmpty() && pagainst.isEmpty()) { 
			return null; 
		}
		/**Compares two lists based on heuristic rules
		 * Preconditions: inaRow is set and valid ( > than the greatest possible value in o1, o2)
		 * Rules: if a priority list might cause a forced win, rank them first
		 * 	Forced win: A priority can increase by one for every move. If a list contains n number of prioirty m,
		 * 	assume the player can turn that into n-1 number of priority m+1, etc. for every subsequent. move
		 * 	If priority can reach inaRow, then there can be a forced win
		 * 	If no forced win: choose the list with greater length			  
		 * 	because placing a piece there will add one to all of those lines' priority
		 */
		Comparator<List<Integer>> compare = new Comparator<>() {
			public int compare(List<Integer> o1, List<Integer> o2) {
				int thresh = inaRow; 
				
				int[][] winMap = new int[2][thresh]; 
				int s1 = o1.size(); 
				int s2 = o2.size(); 
				int cmpIfTie = 0; 
				for(int i = 0; i < s1; i++) { 
					winMap[0][o1.get(i)]++; 
				}
				for(int i = 0; i < s2; i++) { 
					winMap[1][o2.get(i)]++; 
				}
				for(int i = thresh - 1; i >= 1; i--) { 
					int cmp = Integer.compare(winMap[0][i], winMap[1][i]); //compare and stores the counts at each threshold
					if(Math.max(winMap[0][i], winMap[1][i]) + i >= thresh && cmp != 0) { //check if there could be a forced win for one side only
						return cmp; 
					}
					if(cmpIfTie == 0) cmpIfTie = cmp; //update if not set (favours higher potentials)
				}
				//No forced win:
				//this is the most greedy choice.
				if(s1 == s2) return cmpIfTie; 
				return Integer.compare(s1, s2); 
			}
		};
		
		TreeMap<List<Integer>, List<List<Integer>>> prioList = new TreeMap<>(compare); 
		for(var entry : pfor.entrySet()) { 
			// get the coordinates with equal priorities
			List<List<Integer>> eqCoords = new ArrayList<>(prioList.getOrDefault(entry.getValue(), Arrays.asList())); 
			eqCoords.add(entry.getKey()); 
			prioList.put(entry.getValue(), eqCoords); 
		}	
		List<List<Integer>> bestCoords; 
		if(prioList.lastEntry() == null) { 
			bestCoords = new ArrayList<>(); 
			//use all of opponent's possible moves in the next iteration
			pagainst.keySet().forEach(bestCoords::add); 
		}
		else bestCoords =  prioList.lastEntry().getValue(); 
		TreeMap<List<Integer>, List<Integer>> prioListAgainst = new TreeMap(compare); 
		for(var coord : bestCoords) { 
			var curpot = pagainst.get(coord); 
			if(curpot != null) { //if square is shared with a good opponent move
				prioListAgainst.put(curpot, coord); 
			}
		}
		if(prioListAgainst.isEmpty()){ 
			return bestCoords.get(bestCoords.size()-1); //return the last coordinate from bestCoords (a random good move)
		}
		//(pagainst's best move out of pcur's best moves):
		return prioListAgainst.lastEntry().getValue(); 
	}
	/**Check if a piece is in bounds
	 * @param x x-coordinate
	 * @param y y-coordinate
	 * @return true if the coordinates are at a square within the board, false otherwise
	 */
	private boolean inBounds(int x, int y) {
		return x < boardSize && x >= 0 && y < boardSize && y >= 0; 
	}

	/** "marks" the board and gets the max potential for a certain piece and winning line, and updates the potential map
	 * - Marks all visited squares by setting its line.arrayInd value to be zero, so that a checked line will not be rechecked
	 * - The potential of a line (with length = inaRow) is the number of pieces overlapping it, ex: winning line has potential inaRow
	 * -All lines intersecting thie piece are checked, and lines that intersect this piece and another, are checked for the other piece as well.
	 * -Does the "sweeping" until no other piece could possibly influence the sweeping line's potential
	 * -Once checking the line is done, the potential is final, since no other piece should influence it.
	 * 
	 * Pre-condition:
	 * 	thisPotential, x,y, line corresponds to the current piece
	 * 	x, y, are in bounds
	 * 	inaRow corresponds to the game
	 * 	The board is modifiable and valid
	 * Post-condition:
	 * 	the correct array indices of board is correctly modified
	 * 	thisPotential is correctly modified
	 * 
	 * @param x x-coord
	 * @param y y-coord
	 * @param thisPotential the potential map for this player (X or O)
	 * @param board the board map (coords -> length[number of lines in enum Lines])  to be altered
	 * @param line the winning line to check priority of 
	 * @return the max potential for all squares in the line it checked
	 */
	private int trimBoardAndGetMaxPot(int x, int y, Map<List<Integer>, List<Integer>> thisPotential, Map<List<Integer>, List<Integer>> board, Lines line) {
		int thisMaxPot = 0; 
		int pot = 0; 
		int tail[] = {x, y}; 
		int curfac = board.get(Arrays.asList(x, y)).get(line.arrayInd) > 0? 1 :-1; 
		//follows the line in one direction until it collides with an opponent, the board, 
		//or until the current line stops influencing priority
		for(int reach = inaRow; reach > 0; reach--) { 
			if(!inBounds(tail[0], tail[1])) break; 
			var vals = board.get(Arrays.asList(tail[0], tail[1])); 
			if(vals != null) { 
				if(vals.get(line.arrayInd) * curfac <= 0) break; 
				else reach = inaRow; 
			}
			tail[0] -= line.offset[0]; 
			tail[1] -= line.offset[1]; 
		}
		int head[] = {tail[0], tail[1]}; 
		//corrects for the last excess subtraction to tail:
		tail[0] += line.offset[0]; 
		tail[1] += line.offset[1]; 
		//move the head, starting from the tail to its correct starting point and check for collisions
		//The final head position is unchecked
		for(int i = 0; i < inaRow; i++) { 
			head[0] += line.offset[0]; 
			head[1] += line.offset[1]; 
			if(!inBounds(head[0], head[1])) { //if head is out of bounds
				pot = 0; //reset potential (line can never win from the start)
				break; 
			}
			var vals = board.get(Arrays.asList(head[0], head[1])); //get the length values of the current position
			if(vals != null){ 
				if(vals.get(line.arrayInd)* curfac <= 0) { //if piece doesn't match current player
					pot = 0; 
					break; 
				}
				vals.set(line.arrayInd, 0); //mark the position as visited
				pot++; 
			}
		}
		while(inBounds(head[0], head[1])) { //iterate over the remaining positions in this line
			if(pot == 0) break; //if potential is 0, break the loop (line contains 0 squares)
			thisMaxPot = Math.max(thisMaxPot, pot); //update the maximum potential for this piece
			var curTail = tail.clone(); 
			while(true) { 
				//Adds all valid squares in this line (tails-> head) to thisPotential with the current line potential:
				var curpos = Arrays.asList(curTail[0], curTail[1]); 
				var curvals = board.get(curpos); 
				if(curvals == null) { 
					thisPotential.putIfAbsent(curpos, new ArrayList<Integer>()); 
					thisPotential.get(curpos).add(pot); 
				}
				if(Arrays.equals(curTail, head)) break; 
				curTail[0] += line.offset[0]; 
				curTail[1] += line.offset[1]; 
			}
			head[0] += line.offset[0]; 
			head[1] += line.offset[1]; 
			var headvals = board.get(Arrays.asList(head[0], head[1])); 
			if(headvals != null) { 
				if (headvals.get(line.arrayInd)* curfac <= 0) break; //if piece doesn't match current player, break the loop (no more chances to win)
				headvals.set(line.arrayInd, 0); 
				pot++; 
			}
			var tailvals = board.get(Arrays.asList(tail[0], tail[1])); 
			if(tailvals != null) { 
				pot--; 
			}
			tail[0] += line.offset[0]; 
			tail[1] += line.offset[1]; 
		}
		return thisMaxPot; //return the maximum potential for this piece
	}
	/**completely update the state of the computer for a certain game arrangement
 Preconditions:
 	all parameters should be valid for the current game, inaRow must be positive
 Postconditions:
 	All internal state is updated for the game passed in as parameters
 	Game board was not altered in any way
 Calls trimboard and get max pot
	 * 
	 * @param gameBoard the game board map
	 * @param isX whether computer should play for x
	 * @param boardSize the size of the board
	 * @param inaRow the winning length
	 */
	private void updateState(final Map<List<Integer>, List<Integer>> gameBoard, boolean isX, int boardSize, int inaRow) {
		//Reset state:
		myPotential.clear(); 
		opPotential.clear(); 
		this.boardSize = boardSize; 
		this.inaRow = inaRow; 
		myMaxPot = 0; 
		opMaxPot = 0; 
		int fac = isX? 1 : -1; 

		//board needs to be deep copied:
		Map<List<Integer>, List<Integer>> board = new HashMap<>(); 
		for (var entry : gameBoard.entrySet()) { 
			board.put(entry.getKey(), new ArrayList<Integer>(entry.getValue())); 
		}
		for (var entry : board.entrySet()) { 
			int x = entry.getKey().get(0); 
			int y = entry.getKey().get(1); 
			boolean isMyPiece = entry.getValue().get(0) * fac > 0; //determine and store if the piece belongs to the computer
			Map<List<Integer>, List<Integer>> thisPotential = isMyPiece? myPotential : opPotential; //select the appropriate potential moves map
			for(Lines line : Lines.values()) { 
				if(entry.getValue().get(line.arrayInd) == 0){ //if the line was visited
					continue; 
				}
				int thisMaxPrio = trimBoardAndGetMaxPot(x, y, thisPotential, board, line); 
				if(isMyPiece) myMaxPot = Math.max(thisMaxPrio, myMaxPot); 
				else opMaxPot = Math.max(thisMaxPrio, opMaxPot); 
			}
		}
	}

	public List<Integer> getMove(Map<List<Integer>, List<Integer>> board, boolean isX, int boardSize, int inaRow) { 
		updateState(board, isX, boardSize, inaRow); 
		List<Integer> move; 

		//If the opponent can beat the computer in a "race" to the winning length, play the opponent's aggressive move.
		//Otherwise, play the computer's aggressive move
		move = myMaxPot >= opMaxPot? bestMove(myPotential, opPotential) : bestMove(opPotential, myPotential); 

		if(move != null) return move; 

		//no move found:
		if(!board.containsKey(Arrays.asList(boardSize / 2, boardSize / 2))) return Arrays.asList(boardSize / 2, boardSize / 2); //if center is available, play there
		if(!board.containsKey(Arrays.asList(1 + boardSize / 2, 1 + boardSize / 2))) return Arrays.asList(1 + boardSize / 2, 1 + boardSize / 2); //if near-center is available, play there

		//Computer sees no opportunities now:
		for(int i = 0; i < boardSize; i++) { 
			for(int j = 0; j < boardSize; j++) {  
				if(!board.containsKey(Arrays.asList(i,j))){ 
					return Arrays.asList(i,j); 
				}
			}
		}
		return null; //if no move is possible, return null
	}
}

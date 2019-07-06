

/*

AUTHOR:      John Lu

DESCRIPTION: This file contains your agent class, which you will
             implement.

NOTES:       - If you are having trouble understanding how the shell
               works, look at the other parts of the code, as well as
               the documentation.

             - You are only allowed to make changes to this portion of
               the code. Any changes to other portions of the code will
               be lost when the tournament runs your code.
*/

package src;
import src.Action.ACTION;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Scanner;

public class MyAI extends AI {
	// ########################## INSTRUCTIONS ##########################
	// 1) The Minesweeper Shell will pass in the board size, number of mines
	// 	  and first move coordinates to your agent. Create any instance variables
	//    necessary to store these variables.
	//
	// 2) You MUST implement the getAction() method which has a single parameter,
	// 	  number. If your most recent move is an Action.UNCOVER action, this value will
	//	  be the number of the tile just uncovered. If your most recent move is
	//    not Action.UNCOVER, then the value will be -1.
	// 
	// 3) Feel free to implement any helper functions.
	//
	// ###################### END OF INSTURCTIONS #######################
	
	// This line is to remove compiler warnings related to using Java generics
	// if you decide to do so in your implementation.
	@SuppressWarnings("unchecked")

	private int[][] board;
	private int rowSize;
	private int colSize;
	private int currX;
	private int currY;
	private LinkedList<Tuple> needUncovering;
	private LinkedList<Tuple> safeTile;


	public MyAI(int rowDimension, int colDimension, int totalMines, int startX, int startY) {
		// ################### Implement Constructor (required) ####################
		board = new int[colDimension][rowDimension];
		currX = startX;
		currY = startY;
		rowSize = rowDimension;
		colSize = colDimension;
		needUncovering = new LinkedList<Tuple>();
		safeTile = new LinkedList<Tuple>();
	}
	
	// ################## Implement getAction(), (required) #####################
	public Action getAction(int number) {
		// System.out.printf("currX: %d 	currY: %d\n", currX, currY);
		// System.out.println(number);
		board[x(currX)][y(currY)] = number == 0 ? -1 : number;


		if (number == 0)
			uncoverZero(currX,currY);
		else {
			safeTile.add(new Tuple(currX,currY));
		}
		

		if(needUncovering.size() > 0){
			Tuple coor = needUncovering.pop();
			currX = coor.x;
			currY = coor.y;
			return new Action(Action.ACTION.UNCOVER, currX, currY);
		} 
		else if (safeTile.size() > 0){
			Tuple uncover;
			while (safeTile.size() > 0){
				// Possible circular loop
				Tuple currCoor = safeTile.pop();

				if (board[x(currCoor.x)][y(currCoor.y)] == 1){
					uncover = checkIfOneUncovered(currCoor.x, currCoor.y);
					if (uncover == null)
						safeTile.add(currCoor);
					else
						return new Action(Action.ACTION.UNCOVER, uncover.x, uncover.y);
				}
			}		
		}

		return null;
		
	}

	// ################### Helper Functions Go Here (optional) ##################
	// ...

	private class Tuple {
		public int x;
		public int y;

		public Tuple(int x, int y){
			this.x = x;
			this.y = y;
		}

		public Tuple(){
			this.x = -1;
			this.y = -1;
		}

		@Override
		public boolean equals(Object o){
			if (o == this)
				return true;
			if (!(o instanceof Tuple)) {
				return false;
			}
			Tuple object = (Tuple) o;
			return object.x == object.x && object.y == y;
		}
	}

	// If value related to tile is 0, call this function
	public void uncoverZero(int x, int y){
		for(int i = -1;i <= 1;i++){
			for(int j = -1;j <= 1;j++){
				Tuple coor = new Tuple(x+i, y+j);

				if(isInList(coor))
					continue;
				if(i == 0 && j == 0)
					continue;
				if(coor.x < 1 || coor.y < 1)
					continue;
				if(coor.x > rowSize || coor.y > colSize)
					continue;
				if(board[x(coor.x)][y(coor.y)] != 0)
					continue;
				
				needUncovering.add(coor);
			}
		}
	}

	// Check if value in list
	private boolean isInList(Tuple pair){
		for (Tuple e: needUncovering){
			if (e == pair)
				return true;
		} 
		return false;
	}


	// Get the x value in local array board
	private int x(int xVal){
		return xVal-1;
	}

	// Get the y value in local array board
	private int y(int yVal){
		return yVal-1;
	}

	

	// Check if surrounding tile with value 1 is uncovered
	private Tuple checkIfOneUncovered(int x, int y){
		Tuple coor = new Tuple();
		int zeroCount = 0;
		for(int i = -1;i <= 1;i++){
			for(int j = -1;j <= 1;j++){
				if(board[x(x+i)][y(y+j)] == 0){
					coor.x = x+i;
					coor.y = y+j;
					zeroCount++;
				}
			}
		}

		if (zeroCount == 1)
			return coor;
		return null;
	}
}



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
	private LinkedList<int[]> needUncovering;
	private LinkedList<int[]> safeTile;


	public MyAI(int rowDimension, int colDimension, int totalMines, int startX, int startY) {
		// ################### Implement Constructor (required) ####################
		board = new int[colDimension][rowDimension];
		currX = startX;
		currY = startY;
		rowSize = rowDimension;
		colSize = colDimension;
		needUncovering = new LinkedList<int[]>();
		needUncovering = new LinkedList<int[]>();
	}
	
	// ################## Implement getAction(), (required) #####################
	public Action getAction(int number) {
		System.out.printf("currX: %d 	currY: %d\n", currX, currY);
		System.out.println(number);
		board[x(currX)][y(currY)] = number == 0 ? -1 : number;


		if (number == 0)
			uncoverZero(currX,currY);
		else {
			safeTile.add()
		}
		
		if(needUncovering.size() > 0){
			int[] coor = needUncovering.pop();
			currX = coor[0];
			currY = coor[1];
			return new Action(Action.ACTION.UNCOVER, currX, currY);
		} 
		else return null;

		
	}

	// ################### Helper Functions Go Here (optional) ##################
	// ...

	// If value related to tile is 0, call this function
	public void uncoverZero(int x, int y){
		for(int i = -1;i <= 1;i++){
			for(int j = -1;j <= 1;j++){
				int coor[] = {x+i, y+j};

				// System.out.printf("i: %d	j: %d\n", i, j);
				if(isInList(coor))
					continue;
				if(i == 0 && j == 0)
					continue;
				if(x+i < 1 || y+j < 1)
					continue;
				if(x+i > rowSize || y+j > colSize)
					continue;
				if(board[x(x+i)][y(y+j)] != 0)
					continue;
				
				 
				needUncovering.add(coor);
			}
		}
	}

	// Check if value in list
	private boolean isInList(int[] pair){
		for (int[] e: needUncovering){
			if (e[0] == pair[0] && e[1] == pair[1])
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
}

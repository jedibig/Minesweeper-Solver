

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
import java.util.TreeMap;
import java.util.Collections; 
import java.util.Comparator; 

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
	// @SuppressWarnings("unchecked")

	private int[][] board;
	private int rowSize; // Number of row
	private int colSize; // Number of column
	private int currX;	// Last coordinate which an action was taken
	private int currY;
	private LinkedList<Tuple> needUncovering;	// List of coordinates where it is safe to uncover
	private LinkedList<Tuple> needFlagging;		// List of coordinates where flagging is needed
	private LinkedList<Tuple> safeTile;			// List of coordinates where it is uncovered and have one or more covered tiles surrounding it
	private TreeMap<Tuple, Integer> reducedList;

	public MyAI(int rowDimension, int colDimension, int totalMines, int startX, int startY) {
		// ################### Implement Constructor (required) ####################
		board = new int[colDimension][rowDimension];
		currX = startX;
		currY = startY;
		rowSize = rowDimension;
		colSize = colDimension;
		needUncovering = new LinkedList<Tuple>();
		needFlagging = new LinkedList<Tuple>();
		safeTile = new LinkedList<Tuple>();
		reducedList = new TreeMap<>(new SortByCoor());
	}
	
	// ################## Implement getAction(), (required) #####################
	public Action getAction(int number) {
		boolean valid = false;
		String actionStr = "";

		if (number == 0)
			uncoverZero(currX,currY);
		else {
			safeTile.add(new Tuple(currX,currY));
		}
		
		int countSafeTiles = safeTile.size();

		while (!valid){
			// if the value of currX and currY is > 0, that number is assigned towards the 2D array, 
			// otherwise -1 if its 0, -2 if its flagged, 0 if its covered.

			board[x(currX)][y(currY)] = number > 0 ? number : number - 1;	

			if(needUncovering.size() > 0){
				Tuple coor = needUncovering.pop();
				currX = coor.x;
				currY = coor.y;
				actionStr = "U";
				valid = true;
			} else if (needFlagging.size() > 0){
				Tuple coor = needFlagging.pop();
				currX = coor.x;
				currY = coor.y;
				actionStr = "F";
				valid = true;
			} else if (countSafeTiles > 0){
				// Possible circular loop
				//Finish countSafeTile
				countSafeTiles--;
				
				// printList(safeTile, "safeTile");
				Tuple currCoor = safeTile.pop();
				int value = board[x(currCoor.x)][y(currCoor.y)];

				if (value > 0)
					countFlagAndCoveredTiles(new Tuple(currCoor.x, currCoor.y), value);
				
			} else if (safeTile.size() > 0){
				// 1-2 Pattern check

				reduceList.clear();
				for (Tuple coor : safeTile){
					reduceList.put(coor, board[x(coor.x)][y(coor.y)]);
					reduceNumber(coor);
				}

				findPattern();

				
				// 	printBoard();

				// printList(needUncovering, "needUncovering");
				// printList(needFlagging, "needFlagging");

				// actionStr = "L";
				// valid = true;
			} else {
				
				// printBoard();

				actionStr = "L";
				valid = true;
			}
		}

		if (actionStr.equals("U")) {
			return new Action(Action.ACTION.UNCOVER, currX, currY);
		}
		else if (actionStr.equals("F")) {
			return new Action(Action.ACTION.FLAG, currX, currY);
		} 
		else {
			return new Action(Action.ACTION.LEAVE,1,1);
		}
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
				if(outBoundaries(coor.x, coor.y))
					continue;
				try{
					if(board[x(coor.x)][y(coor.y)] != 0)
						continue;
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.printf("Array out of bound. Was trying to access (%d,%d) with rowsize: %d and colsize: %d.\n",coor.x,coor.y,rowSize,colSize);
				}
				
				needUncovering.add(coor);
			}
		}
	}

	private boolean outBoundaries(int x, int y){
		return x < 1 || y < 1 || x > colSize || y > rowSize;
	}

	// Check if value in list
	private boolean isInList(Tuple pair){
		for (Tuple e: needUncovering){
			if (e.x == pair.x && e.y == pair.y)
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
	private void countFlagAndCoveredTiles(Tuple pair, int value){
		LinkedList<Tuple> coveredTiles = new LinkedList<>();
		LinkedList<Tuple> flaggedTiles = new LinkedList<>();
	
		for(int i = -1;i <= 1;i++){
			for(int j = -1;j <= 1;j++){
				if(outBoundaries(pair.x+i, pair.y+j))
					continue;
				else if(i == 0 && j == 0)
					continue;
				
	
				if(board[x(pair.x+i)][y(pair.y+j)] == 0){
					coveredTiles.add(new Tuple(pair.x+i, pair.y+j));
				} else if (board[x(pair.x+i)][y(pair.y+j)] == -2)
					flaggedTiles.add(new Tuple(pair.x+i, pair.y+j));
			}
		}

		if (flaggedTiles.size() == value){
			for (Tuple e: coveredTiles)
				needUncovering.add(e);
		} else if (coveredTiles.size() == value - flaggedTiles.size()){
			for (Tuple e: coveredTiles)
				needFlagging.add(e);
		} else if (coveredTiles.size() > 0)
			safeTile.add(pair);

	}

	private void reduceNumber(Tuple pair){
		for(int i = -1;i <= 1;i++){
			for(int j = -1;j <= 1;j++){

				if(outBoundaries(pair.x+i, pair.y+j))
					continue;
				else if(i == 0 && j == 0)
					continue;

				if(board[x(pair.x+i)][y(pair.y+j)] == -2){
					board[x(pair.x+i)][y(pair.y+j)] -= 1;
					reduceSurroundingNumber(new Tuple(pair.x+i,pair.y+j));
				}
			}
		}
	}

	private void reduceSurroundingNumber(Tuple pair){
		for(int i = -1;i <= 1;i++){
			for(int j = -1;j <= 1;j++){
				if(outBoundaries(pair.x+i, pair.y+j))
					continue;
				else if(i == 0 && j == 0)
					continue;

				if(board[x(pair.x+i)][y(pair.y+j)] > 0)
					reduceList.put(new Tuple(pair.x+i, pair.y+j), board[x(pair.x+i)][y(pair.y+j)]-1);

			}
		}
	}


	//Comparator x & y
	class SortByCoor implements Comparator<Tuple>{
		public int compare(Tuple t1, Tuple t2){
			return (t1.x + t1.y) - (t2.x + t2.y);
		}
	}

	//Find pattern then manipulate the 
	private void findPattern(){
		boolean vertical = false;
		boolean horizontal = false;
		if(reducedList.firstEntry().getValue() == 1){
			Tuple pair1 = reducedList.pollFirstEntry().getKey();
			if(reducedList.firstEntry().getValue() == 2){
				Tuple pair2 = reducedList.pollFirstEntry().getKey();
				if(pair1.x == pair2.x)
					vertical = true;
				else if(pair1.y == pair2.y)
					horizontal = true;
				if(reducedList.firstEntry().getValue() == 1){
					Tuple pair3 = reducedList.pollFirstEntry().getKey();
					if(horizontal && pair3.y == pair2.y){
						if(board[x(pair1.x)][y(pair1.y)+1] == 0 && board[x(pair2.x)][y(pair2.y)+1] == 0 && board[x(pair3.x)][y(pair3.y)+1] == 0){
							//Flag on top
							needFlagging.add(new Tuple(pair1.x, (pair1.y)+1));
							needFlagging.add(new Tuple(pair3.x, (pair3.y)+1));
							needUncovering.add(new Tuple(pair2.x, (pair2.y)+1));
						} else {
							//Flag below
							needFlagging.add(new Tuple(pair1.x, (pair1.y)-1));
							needFlagging.add(new Tuple(pair3.x, (pair3.y)-1));
							needUncovering.add(new Tuple(pair2.x, (pair2.y)-1));
						}	
					} else if(vertical && pair3.x == pair2.x){
						if(board[x(pair1.x)+1][y(pair1.y)] == 0 && board[x(pair2.x)+1][y(pair2.y)] == 0 && board[x(pair3.x)+1][y(pair3.y)] == 0){
							//Flag on right
							needFlagging.add(new Tuple(pair1.x+1, pair1.y));
							needFlagging.add(new Tuple(pair3.x+1, pair3.y));
							needUncovering.add(new Tuple(pair2.x+1, pair2.y));
						} else{
							//Flag left
							needFlagging.add(new Tuple((pair1.x)-1, pair1.y));
							needFlagging.add(new Tuple((pair3.x)-1, pair3.y));
							needUncovering.add(new Tuple((pair2.x)-1, pair2.y));
						}
					}
				} else if(reducedList.firstEntry().getValue() == 2){
					Tuple pair3 = reducedList.pollFirstEntry().getKey();
					Tuple pair4 = reducedList.pollFirstEntry().getKey();
					if (horizontal && pair3.y == pair2.y && pair4.y == pair3.y){
						if(board[x(pair4.x)][y(pair4.y)] == 1){
							//Found pattern 1221
							if(board[x(pair1.x)][y(pair1.y)+1] == 0 && board[x(pair2.x)][y(pair2.y)+1] == 0 && board[x(pair3.x)][y(pair3.y)+1] == 0 && board[x(pair4.x)][y(pair4.y)+1] == 0){
								//Flag on top
								needFlagging.add(new Tuple(pair2.x, (pair2.y)+1));
								needFlagging.add(new Tuple(pair3.x, (pair3.y)+1));
								needUncovering.add(new Tuple(pair1.x, (pair1.y)+1));
								needUncovering.add(new Tuple(pair4.x, (pair4.y)+1));
							} else {
								//Flag below
								needFlagging.add(new Tuple(pair2.x, (pair2.y)-1));
								needFlagging.add(new Tuple(pair3.x, (pair3.y)-1));
								needUncovering.add(new Tuple(pair1.x, (pair1.y)-1));
								needUncovering.add(new Tuple(pair4.x, (pair4.y)-1));
							}
						}
					} else if (vertical && pair3.x == pair2.x && pair4.x == pair3.x){
						// System.out.println("hello from for found4");
						if(board[x(pair4.x)][y(pair4.y)] == 1){
							if(board[x(pair1.x)+1][y(pair1.y)] == 0 && board[x(pair2.x)+1][y(pair2.y)] == 0 && board[x(pair3.x)+1][y(pair3.y)] == 0 && board[x(pair4.x)+1][y(pair4.y)] == 0){
								//Flag on right
								needFlagging.add(new Tuple(pair2.x+1, pair2.y));
								needFlagging.add(new Tuple(pair3.x+1, pair3.y));
								needUncovering.add(new Tuple(pair1.x+1, pair1.y));
								needUncovering.add(new Tuple(pair4.x+1, pair4.y));
							} else{
								//Flag left
								needFlagging.add(new Tuple((pair2.x)-1, pair2.y));
								needFlagging.add(new Tuple((pair3.x)-1, pair3.y));
								needUncovering.add(new Tuple((pair1.x)-1, pair1.y));
								needUncovering.add(new Tuple((pair4.x)-1, pair4.y));
							}
						}
					}
				}
			}
		}
		
		
		
		// safeTile.sort(new SortByCoor());
		// for(int i = 0;i < reducedList.size();i++){	
		// 	// System.out.println("hello from for loop");
		// 	boolean vertical = false;
		// 	boolean horizontal = false;
		// 	try {
		// 		Tuple pair1 = safeTile.get(i);
		// 		Tuple pair2 = safeTile.get(i+1);
		// 		Tuple pair3 = safeTile.get(i+2);
		// 		Tuple pair4 = safeTile.get(i+3);

		// 		if(board[x(pair1.x)][y(pair1.y)] == 1){
		// 			// System.out.println("hello from for found1");
		// 			if(board[x(pair2.x)][y(pair2.y)] == 2){
		// 				// System.out.println("hello from for found2");

		// 				if(pair1.x == pair2.x)
		// 					vertical = true;
		// 				else if(pair1.y == pair2.y)
		// 					horizontal = true;
		// 				else continue;
		// 				if(board[x(pair3.x)][y(pair3.y)] == 1){
		// 					// System.out.printf("hello from for found3, coord: (%d,%d)\n", pair1.x, pair1.y);

		// 					//Found pattern 121
		// 					//Check if it's on top/bottom
		// 					// System.out.printf("horizontal if "+ horizontal + " %d %d\n" , pair3.y, pair2.y);
		// 					if(horizontal && pair3.y == pair2.y){
		// 						if(board[x(pair1.x)][y(pair1.y)+1] == 0 && board[x(pair2.x)][y(pair2.y)+1] == 0 && board[x(pair3.x)][y(pair3.y)+1] == 0){
		// 							//Flag on top
		// 							needFlagging.add(new Tuple(pair1.x, (pair1.y)+1));
		// 							needFlagging.add(new Tuple(pair3.x, (pair3.y)+1));
		// 							needUncovering.add(new Tuple(pair2.x, (pair2.y)+1));
		// 						} else {
		// 							//Flag below
		// 							needFlagging.add(new Tuple(pair1.x, (pair1.y)-1));
		// 							needFlagging.add(new Tuple(pair3.x, (pair3.y)-1));
		// 							needUncovering.add(new Tuple(pair2.x, (pair2.y)-1));
		// 						}			
		// 					} else if(vertical && pair3.x == pair2.x){
		// 						if(board[x(pair1.x)+1][y(pair1.y)] == 0 && board[x(pair2.x)+1][y(pair2.y)] == 0 && board[x(pair3.x)+1][y(pair3.y)] == 0){
		// 							//Flag on right
		// 							needFlagging.add(new Tuple(pair1.x+1, pair1.y));
		// 							needFlagging.add(new Tuple(pair3.x+1, pair3.y));
		// 							needUncovering.add(new Tuple(pair2.x+1, pair2.y));
		// 						} else{
		// 							//Flag left
		// 							needFlagging.add(new Tuple((pair1.x)-1, pair1.y));
		// 							needFlagging.add(new Tuple((pair3.x)-1, pair3.y));
		// 							needUncovering.add(new Tuple((pair2.x)-1, pair2.y));
		// 						}
		// 					}
		// 				} else if (board[x(pair3.x)][y(pair3.y)] == 2){
		// 					// System.out.println("hello from for found3");

		// 					if (horizontal && pair3.y == pair2.y && pair4.y == pair3.y){
		// 						// System.out.println("hello from for found4");

		// 						if(board[x(pair4.x)][y(pair4.y)] == 1){
		// 							//Found pattern 1221
		// 							if(board[x(pair1.x)][y(pair1.y)+1] == 0 && board[x(pair2.x)][y(pair2.y)+1] == 0 && board[x(pair3.x)][y(pair3.y)+1] == 0 && board[x(pair4.x)][y(pair4.y)+1] == 0){
		// 								//Flag on top
		// 								needFlagging.add(new Tuple(pair2.x, (pair2.y)+1));
		// 								needFlagging.add(new Tuple(pair3.x, (pair3.y)+1));
		// 								needUncovering.add(new Tuple(pair1.x, (pair1.y)+1));
		// 								needUncovering.add(new Tuple(pair4.x, (pair4.y)+1));
		// 							} else {
		// 								//Flag below
		// 								needFlagging.add(new Tuple(pair2.x, (pair2.y)-1));
		// 								needFlagging.add(new Tuple(pair3.x, (pair3.y)-1));
		// 								needUncovering.add(new Tuple(pair1.x, (pair1.y)-1));
		// 								needUncovering.add(new Tuple(pair4.x, (pair4.y)-1));
		// 							}
		// 						}
		// 					} else if (vertical && pair3.x == pair2.x && pair4.x == pair3.x){
		// 						// System.out.println("hello from for found4");
		// 						if(board[x(pair4.x)][y(pair4.y)] == 1){
		// 							if(board[x(pair1.x)+1][y(pair1.y)] == 0 && board[x(pair2.x)+1][y(pair2.y)] == 0 && board[x(pair3.x)+1][y(pair3.y)] == 0 && board[x(pair4.x)+1][y(pair4.y)] == 0){
		// 								//Flag on right
		// 								needFlagging.add(new Tuple(pair2.x+1, pair2.y));
		// 								needFlagging.add(new Tuple(pair3.x+1, pair3.y));
		// 								needUncovering.add(new Tuple(pair1.x+1, pair1.y));
		// 								needUncovering.add(new Tuple(pair4.x+1, pair4.y));
		// 							} else{
		// 								//Flag left
		// 								needFlagging.add(new Tuple((pair2.x)-1, pair2.y));
		// 								needFlagging.add(new Tuple((pair3.x)-1, pair3.y));
		// 								needUncovering.add(new Tuple((pair1.x)-1, pair1.y));
		// 								needUncovering.add(new Tuple((pair4.x)-1, pair4.y));
		// 							}
		// 						}
		// 					}
		// 				}
		// 			}
		// 		} 
		// 	} catch (IndexOutOfBoundsException e){
		// 		continue;
		// 	}
		// }
	}

	// For testing purpose only
	private void printList(LinkedList<Tuple> list, String name){
		System.err.printf("printing list %s\t", name);
		for (Tuple e : list)
			System.err.printf("(%d,%d)", e.x, e.y);
		System.err.println();
	}

	private void printBoard(){
		for(int i = 1;i <= colSize;i++){
			System.out.printf("%2d\t\t", i);
			for(int j = 1;j <= rowSize;j++){
				System.out.printf("%2d\t", board[x(i)][y(j)]);
			}
			System.out.println();
		} 
		System.out.println();
	}
}
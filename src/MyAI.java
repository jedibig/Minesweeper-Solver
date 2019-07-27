

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
	private TreeMap<Tuple, Integer> reducedListHorizontal;
	private TreeMap<Tuple, Integer> reducedListVertical;

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
		reducedListHorizontal = new TreeMap<>(
			new Comparator<Tuple>(){
				@Override
				public int compare(Tuple t1, Tuple t2){
					return t1.y == t2.y ? t1.x - t2.x : t1.y - t2.y;
				}
			});
		reducedListVertical = new TreeMap<>(
					new Comparator<Tuple>(){
						@Override
						public int compare(Tuple t1, Tuple t2){
							return t1.x == t2.x ? t2.y - t1.y : t2.x - t1.x;
						}
			});
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
				int value = value(currCoor.x, currCoor.y);

				if (value > 0)
					countFlagAndCoveredTiles(new Tuple(currCoor.x, currCoor.y), value);

			} else if (safeTile.size() > 0){
				// 1-2 Pattern check

				reducedListHorizontal.clear();
				reducedListVertical.clear();
				for (Tuple coor : safeTile)
					reduceNumber(coor);

				findPatternHorizontal();
				findPatternVertical();

				printBoard();

				printList(needFlagging, "needFlagging");
				// actionStr = "L";
				// valid = true;
			} else {
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
			// printBoard();
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

		@Override
		public String toString() { 
			return String.format("(%d,%d)", x, y); 
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
					if(value(coor.x,coor.y) != 0)
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

	// Get the board value of (x,y)
	private int value(int x, int y){
		return board[x-1][y-1];
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


				if(value(pair.x+i, pair.y+j) == 0){
					coveredTiles.add(new Tuple(pair.x+i, pair.y+j));
				} else if (value(pair.x+i, pair.y+j) == -2)
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
		int value = value(pair.x, pair.y);
		for(int i = -1;i <= 1;i++){
			for(int j = -1;j <= 1;j++){

				if(outBoundaries(pair.x+i, pair.y+j))
					continue;
				else if(i == 0 && j == 0)
					continue;

				if(value(pair.x+i, pair.y+j) == -2)
					value--;
			}
		}
		reducedListHorizontal.put(pair, value);
		reducedListVertical.put(pair, value);
	}

	private void findPatternHorizontal(){
		Tuple pair1 = null, pair2 = null;
		while(reducedListHorizontal.size() >= 2){
			//Check if first element is 1
			if(reducedListHorizontal.firstEntry().getValue() == 1){
				pair1 = reducedListHorizontal.pollFirstEntry().getKey();
				if(reducedListHorizontal.firstEntry().getValue() == 2 && (reducedListHorizontal.firstKey().y == pair1.y)){
					//Check if second element is 2 (pattern 1-2)
					pair2 = reducedListHorizontal.firstKey();
					if(checkSurrounding12H(pair1, pair2)){
						needFlagging.add(new Tuple(pair2.x+1, pair2.y+1));
					}
				} else if(reducedListHorizontal.firstEntry().getValue() == 1 && (reducedListHorizontal.firstKey().y == pair1.y)){
					//Check if second element is 1 (pattern 1-1)
					pair2 = reducedListHorizontal.firstKey();
					Tuple flagPair = checkSurrounding11H(pair1, pair2);
					if(flagPair != null){
						needUncovering.add(flagPair);
					}
				}
			} 
			//Check if first element is 2
			else if(reducedListHorizontal.firstEntry().getValue() == 2){
				pair1 = reducedListHorizontal.pollFirstEntry().getKey();
				if(reducedListHorizontal.firstEntry().getValue() == 1 && (reducedListHorizontal.firstKey().y == pair1.y)){
					//Check if second element is 1 (pattern 2-1)
					pair2 = reducedListHorizontal.firstKey();
					if(checkSurrounding12H(pair1, pair2)){
						needFlagging.add(new Tuple(pair1.x-1, pair1.y-1));
					}
				}
			}
			else reducedListHorizontal.pollFirstEntry();
		}
	}

	private Tuple checkSurrounding11H(Tuple t1, Tuple t2){
		Tuple t3 = null;
		Tuple t4 = null;
		if(!outBoundaries(t1.x-1, t1.y)){
			t3 = new Tuple(t1.x-1, t1.y);
		} if(!outBoundaries(t1.x+1, t1.y)){
			t4 = new Tuple(t1.x+1, t1.y);
		}

		if(outBoundaries(t1.x, t1.y-1) || (isBottomOpen(t1) && isBottomOpen(t2) && (t3 == null || isBottomOpen(t3)) && (t4 == null || isBottomOpen(t4)))){
			if(!outBoundaries(t1.x, t1.y+1) && isTopOpen(t1) && isTopOpen(t2)){
				if((t3 == null || isTopOpen(t3)) && t4 != null){
					return new Tuple(t4.x, t4.y+1);
				} else if((t4 == null || isTopOpen(t4)) && t3 != null){
					return new Tuple(t3.x, t3.y+1);
				}
			}
		}
		else if(outBoundaries(t1.x, t1.y+1) || (isTopOpen(t1) && isTopOpen(t2) && (t3 == null || isTopOpen(t3)) && (t4 == null || isTopOpen(t4)))){
			if(!outBoundaries(t1.x, t1.y-1) && isBottomOpen(t1) && isBottomOpen(t2)){
				if((t3 == null || isBottomOpen(t3)) && t4 != null){
					return new Tuple(t4.x, t4.y-1);
				} else if((t4 == null || isBottomOpen(t4)) && t3 != null){
					return new Tuple(t3.x, t3.y-1);
				}
			}
		}
		return null;
	}

	private boolean checkSurrounding12H(Tuple t1, Tuple t2){
		Tuple t3 = t2.x > t1.x ? new Tuple(t2.x+1, t2.y) : new Tuple(t2.x-1, t2.y);
		if(outBoundaries(t3.x, t3.y)){
			return false;
		}
		
		if(outBoundaries(t1.x, t1.y-1) || (isBottomOpen(t1) && isBottomOpen(t2) && isBottomOpen(t3))){
			if(!outBoundaries(t1.x, t1.y+1) && isTopOpen(t1) && isTopOpen(t2) && isTopOpen(t3)){
				return true;
			}
		}
		else if(outBoundaries(t1.x, t1.y+1) || (isTopOpen(t1) && isTopOpen(t2) && isTopOpen(t3))){
			if(!outBoundaries(t1.x, t1.y-1) && (isBottomOpen(t1) && isBottomOpen(t2) && isBottomOpen(t3))){
				return true;
			}
		}
		
		return false;
	}

	private boolean isBottomOpen(Tuple t){
		return value(t.x, t.y-1) == 0;
	}

	private boolean isTopOpen(Tuple t){
		return value(t.x, t.y+1) == 0;
	}

	private void findPatternVertical(){
		Tuple pair1 = null, pair2 = null;
		while(reducedListVertical.size() >= 2){
			//Check if first element is 1
			if(reducedListVertical.firstEntry().getValue() == 1){
				pair1 = reducedListVertical.pollFirstEntry().getKey();
				if(reducedListVertical.firstEntry().getValue() == 2 && (reducedListVertical.firstKey().x == pair1.x)){
					//Check if second element is 2 (pattern 1-2)
					pair2 = reducedListVertical.firstKey();
					checkSurrounding12V(pair1, pair2);
				} else if(reducedListVertical.firstEntry().getValue() == 1 && (reducedListVertical.firstKey().x == pair1.x)){
					//Check if second element is 1 (pattern 1-1)
					pair2 = reducedListVertical.firstKey();
					checkSurrounding11V(pair1, pair2);
				}
			} 
			//Check if first element is 2
			else if(reducedListVertical.firstEntry().getValue() == 2){
				pair1 = reducedListVertical.pollFirstEntry().getKey();
				if(reducedListVertical.firstEntry().getValue() == 1 && (reducedListVertical.firstKey().x == pair1.x)){
					//Check if second element is 1 (pattern 2-1)
					pair2 = reducedListVertical.firstKey();
					checkSurrounding12V(pair2, pair1);
				}
			} else reducedListVertical.pollFirstEntry();
		}
	}

	private void checkSurrounding11V(Tuple t1, Tuple t2){
		Tuple t3 = null;
		Tuple t4 = null;
		if(!outBoundaries(t1.x, t1.y+1)){
			t3 = new Tuple(t1.x, t1.y+1);
			if(value(t3.x,t3.y) == 0)
				return;
		} if(!outBoundaries(t2.x, t2.y-1)){
			t4 = new Tuple(t2.x, t2.y-1);
			if(value(t4.x,t4.y) == 0)
				return;
		}

		if(outBoundaries(t1.x-1, t1.y) || (isLeftUncovered(t1) && isLeftUncovered(t2) && (t3 == null || isLeftUncovered(t3)) && (t4 == null || isLeftUncovered(t4)))){
			if(!outBoundaries(t1.x+1, t1.y) && !isRightUncovered(t1) && !isRightUncovered(t2)){
				if((t3 == null || isRightUncovered(t3)) && t4 != null && !isRightUncovered(t4))
					needUncovering.add(new Tuple(t4.x+1,t4.y));
				else if((t4 == null || isRightUncovered(t4)) && t3 != null && !isRightUncovered(t3))
					needUncovering.add(new Tuple(t3.x+1, t3.y));
			}
		}
		else if(outBoundaries(t1.x+1, t1.y) || (isRightUncovered(t1) && isRightUncovered(t2) && (t3 == null || isRightUncovered(t3)) && (t4 == null || isRightUncovered(t4)))){
			if(!outBoundaries(t1.x-1, t1.y) && !isLeftUncovered(t1) && !isLeftUncovered(t2)){
				if((t3 == null || isLeftUncovered(t3)) && t4 != null && !isLeftUncovered(t4))
					needUncovering.add(new  Tuple(t4.x-1, t4.y));
				else if((t4 == null || isLeftUncovered(t4)) && t3 != null && !isLeftUncovered(t3))
					needUncovering.add(new Tuple(t3.x-1, t3.y));
			}
		}
	}

	private void checkSurrounding12V(Tuple t1, Tuple t2){
		Tuple t3 = t2.y > t1.y ? new Tuple(t2.x, t2.y+1) : new Tuple(t2.x, t2.y-1);
		if(outBoundaries(t3.x, t3.y) || value(t3.x,t3.y) == 0){
			return;
		}
		
		if(outBoundaries(t1.x-1, t1.y) || (isLeftUncovered(t1) && isLeftUncovered(t2) && isLeftUncovered(t3))){
			if (!(outBoundaries(t1.x+1, t1.y) || isRightUncovered(t1) || isRightUncovered(t2) || isRightUncovered(t3)))
				needFlagging.add(new Tuple(t3.x+1, t3.y));
		}	// left side is open or out of bpunds

		else if( outBoundaries(t1.x+1, t1.y) || isRightUncovered(t1) && isRightUncovered(t2) && isRightUncovered(t3) ){
			if (!outBoundaries(t1.x-1, t1.y) && !isLeftUncovered(t1) && !isLeftUncovered(t2) && !isLeftUncovered(t3)){
			// if (!( outBoundaries(t1.x-1, t1.y) || isLeftUncovered(t1) || isLeftUncovered(t2) || isLeftUncovered(t3))){
				needFlagging.add(new Tuple(t3.x-1, t3.y));
			}
		}
		
		// if (!outBoundaries(t1.x-1, t1.y)){
		// 	System.out.printf("l: t1: (%d,%d) %b\t t2: (%d,%d) %b\t t3: (%d,%d) %b\n", t1.x, t1.y, isLeftUncovered(t1), t2.x, t2.y, isLeftUncovered(t2), t3.x, t3.y, isLeftUncovered(t3) );
		// 	System.out.printf("r: t1: (%d,%d) %b\t t2: (%d,%d) %b\t t3: (%d,%d) %b\n", t1.x, t1.y, isRightUncovered(t1), t2.x, t2.y, isRightUncovered(t2), t3.x, t3.y, isRightUncovered(t3) );
		// 	System.out.printf("outound1: %b\t outbound2: %b\n", outBoundaries(t1.x+1, t1.y), outBoundaries(t1.x-1, t1.y));
		// }
		
		
	}

	private boolean isLeftUncovered(Tuple t){
		return value(t.x-1, t.y) != 0;
	}

	private boolean isRightUncovered(Tuple t){
		return value(t.x+1, t.y) != 0;
	}

	// For testing purpose only
	private void printList(LinkedList<Tuple> list, String name){
		System.err.printf("printing list %s\t", name);
		for (Tuple e : list)
			System.err.printf("(%d,%d)", e.x, e.y);
		System.err.println();
	}

	private void printBoard(){
		for(int i = rowSize; i >= 1; i--){
			System.out.printf("%2d\t", i);
			for(int j = 1;j <= colSize;j++){
				System.out.printf("%2d ", value(j,i));
			}
			System.out.println();
		}
		System.out.printf("  \t");
		for (int k = 1; k <= colSize; k++)
			System.out.printf("%2d ", k);
		System.out.println();

	}
}
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


/**
 * The Player class represents a player in the Nim game, producing Moves as a response to a Board state. Each player 
 * is initialized with a type, either human or one of several computer strategies, which defines the move he 
 * produces when given a board in some state. The heuristic strategy of the player is already implemented. You are 
 * required to implement the rest of the player types according to the exercise description.
 * @author OOP course staff
 */
public class Player {

	//Constants that represent the different players.
	/** The constant integer representing the Random player type. */
	public static final int RANDOM = 1;
	/** The constant integer representing the Heuristic player type. */
	public static final int HEURISTIC = 2;
	/** The constant integer representing the Smart player type. */
	public static final int SMART = 3;
	/** The constant integer representing the Human player type. */
	public static final int HUMAN = 4;
	
	private static final int BINARY_LENGTH = 4;	//Used by produceHeuristicMove() for binary representation of board rows.
	// constant RANDOM_DELTA is used to fix the randomising for the random move to the right range
	private static final int RANDOM_DELTA = 1;
	private final int playerType;
	private final int playerId;
	private Scanner scanner;

	/** class constants **/
	private static final String INPUT_REQUEST_MSG = "Press 1 to display the board. Press 2 to make a move:";
	private static final String UNSUPPORTED_COMMAND ="Unsupported command";
	private static final String ROW_INPUT_REQUEST = "Enter the row number:";
	private static final String LEFT_INPUT_REQUEST = "Enter the index of the leftmost stick:";
	private static final String RIGHT_INPUT_REQUEST = "Enter the index of the rightmost stick:";
	
	/**
	 * Initializes a new player of the given type and the given id, and an initialized scanner.
	 * @param type The type of the player to create.
	 * @param id The id of the player (either 1 or 2).
	 * @param inputScanner The Scanner object through which to get user input
	 * for the Human player type. 
	 */
	public Player(int type, int id, Scanner inputScanner){		
		// Check for legal player type (we will see better ways to do this in the future).
		if (type != RANDOM && type != HEURISTIC 
				&& type != SMART && type != HUMAN){
			System.out.println("Received an unknown player type as a parameter"
					+ " in Player constructor. Terminating.");
			System.exit(-1);
		}		
		playerType = type;	
		playerId = id;
		scanner = inputScanner;

	}

	/**
	 * @return an integer matching the player type.
	 */	
	public int getPlayerType(){
		return playerType;
	}
	
	/**
	 * @return the players id number.
	 */	
	public int getPlayerId(){
		return playerId;
	}
	
	/**
	 * @return a String matching the player type.
	 */
	public String getTypeName(){
		switch(playerType){
			
			case RANDOM:
				return "Random";			    
	
			case SMART: 
				return "Smart";	
				
			case HEURISTIC:
				return "Heuristic";
				
			case HUMAN:			
				return "Human";
		}
		//Because we checked for legal player types in the
		//constructor, this line shouldn't be reachable.
		return "UnknownPlayerType";
	}
	
	/**
	 * This method encapsulates all the reasoning of the player about the game. The player is given the 
	 * board object, and is required to return his next move on the board. The choice of the move depends
	 * on the type of the player: a human player chooses his move manually; the random player should 
	 * return some random move; the Smart player can represent any reasonable strategy; the Heuristic 
	 * player uses a strong heuristic to choose a move. 
	 * @param board - a Board object representing the current state of the game.
	 * @return a Move object representing the move that the current player will play according to his strategy.
	 */
	public Move produceMove(Board board){
		
		switch(playerType){
		
			case RANDOM:
				return produceRandomMove(board);				
				    
			case SMART: 
				return produceSmartMove(board);
				
			case HEURISTIC:
				return produceHeuristicMove(board);
				
			case HUMAN:
				return produceHumanMove(board);

			//Because we checked for legal player types in the
			//constructor, this line shouldn't be reachable.
			default: 
				return null;			
		}
	}

	/**
	 * Produces a random move.
	 */
	private Move produceRandomMove(Board board) {
		//building an array to which relevant row's index will be inserted
		ArrayList<Integer> relevantRows = new ArrayList<>();
		//iterating over rows in order to check which are relevant
		for (int i = 1; i < board.getNumberOfRows() + 1; i++) {
			//iterating over each row's stick to check if the row's relevant
			for (int j = 1; j < board.getRowLength(i) + 1; j++) {
				if (board.isStickUnmarked(i, j)) {
					relevantRows.add(i);
					break;
				}
			}
		}
		if (relevantRows.size() == 0){
			return new Move(0,0,0);
		}
		//randomly choosing a relevant row-
		Random randomGenerator = new Random();
		int rowIndex = randomGenerator.nextInt(relevantRows.size());
		int selectedRow = relevantRows.get(rowIndex);
		int rowLength = board.getRowLength(selectedRow);
		//randomly choosing a stick from the relevant row
		int randomLeftBound = randomGenerator.nextInt(rowLength)+RANDOM_DELTA;
		//checking if the stick is unmarked, as wanted, and assuring the chosen stick is unmarked
		while (!board.isStickUnmarked(selectedRow, randomLeftBound)){
			randomLeftBound = randomGenerator.nextInt(rowLength)+RANDOM_DELTA;
		} int unmarkedNeighbour = 0, randomRightBound = 0;
		//checking if there are unmarked sticks to the right of the chosen unmarked stick
		for (int i=1; i<rowLength+1;i++){
			// if there are any right-neighbours unmarked, the variable 'unmarkedNeighbours' will count how many
			if (board.isStickUnmarked(selectedRow, randomLeftBound+i)){
				unmarkedNeighbour++;
			} else {
				break;
			}
		// after counting how many unmarked-right-neighbours are there, we will randomly choose one-
		} if (unmarkedNeighbour==0){
			randomRightBound = randomLeftBound;
		} else {
			randomRightBound = randomGenerator.nextInt(unmarkedNeighbour)+RANDOM_DELTA+randomLeftBound;
		}
		 return new Move(selectedRow, randomLeftBound, randomRightBound);
	}

    /**
     * aid function for the produceSmartMove method, checks if there is a continuous sequence in the asked length
     * in the asked row.
     * @param board- the game board
     * @param sequenceLength- the wanted length of sequence
     * @param row- the row to be checked
     * @return the left bound of the possible move, 0 if there is no sequence in wanted length
     */
	private Move checkForSequence(Board board, int sequenceLength, int row){
        //initializing checking variables-
	    int sequenceCount = 0, lastStickFlag=0, leftBound=0;
	    Move selectedMove= new Move(0,0,0);
        //if input is not valid- return 0
        if (sequenceLength>board.getRowLength(row)){
            selectedMove = new Move(0,0,0);
            return selectedMove;
        }
        //iterating over the row's sticks in order to see if a wanted sequence exists
        for (int stick=1; stick< board.getRowLength(row)+1; stick++){
            //checks if the first stick is marked
            if (stick == 1){
                //if it is marked, the sequence count is added by 1 and the stick is marked as the left bound
                if (board.isStickUnmarked(row, stick)){
                    lastStickFlag = 1;
                    sequenceCount++;
                    leftBound = stick;
                //if its not, the last stick flag is marked accordingly
                } else {
                    lastStickFlag = 0;
            	} //in the case in which the stick is not the first one-
            } else {
                //if it is unmarked, the counting is increased by 1 and the flag changes accordingly
                if (board.isStickUnmarked(row, stick)){
                    sequenceCount++;
					if ((sequenceCount>=sequenceLength) && (lastStickFlag==1)){
						selectedMove = new Move(row, leftBound, leftBound+sequenceLength-1);
						return selectedMove;
					} //if the previous stick was marked, change the last stick flag and the left bound
                    if (lastStickFlag!=1) {
						lastStickFlag = 1;
						leftBound = stick;
						if (sequenceCount>=sequenceLength){
							selectedMove = new Move(row, leftBound, leftBound+sequenceLength-1);
							return selectedMove;
						}
					} //if the stick is marked, check if the sequence length to this point is as requested
                } else {
                    //if so, break the loop and return the left bound
                    if (sequenceCount>=sequenceLength){
						selectedMove = new Move(row, leftBound, leftBound+sequenceLength-1);
						return selectedMove;
                        // if it isn't suitable, start the counting from 0 and initialize the left bound and flag
                    } else {
                        sequenceCount = lastStickFlag = leftBound = 0;
                    }
                }
            }
            //in all cases- return the left bound, found or not (returns zero if so)
        } if (sequenceCount<sequenceLength){
        	return new Move(0,0,0);
		} return selectedMove;
	}

	/**
	 * an aid method for the produce smart and random move methods. it counts how many unmarked sticks are there in a
	 * specific row in the board.
	 * @param board the current board
	 * @param row the row asked to be searched
	 * @return the number of unmarked sticks in the row
	 */
	private int getUnmarkedSticksNumberInRow(Board board, int row){
		//initializing stick counter-
		int sticksCount = 0;
		//initializing for loop, iterating over the asked row-
		for (int stick=1; stick< board.getRowLength(row)+1; stick++){
			//if stick is unmarked, the count gets +1-
			if (board.isStickUnmarked(row, stick)){
				sticksCount++;
			}
			//return the number of unmarked sticks-
		} return sticksCount;
	}

    /**
     * this function grades every board. if the grade is zero, the move returned is random. if the grade is different
     * then zero, the best possible move is a move that makes the board's grade zero, so that is the move returned.
     * @param board- the game board
     * @return returns a valid smart move
     */
	private Move produceSmartMove(Board board){
	    //initializing variables for grading the board, including data structures helping to do so.
        int nimSum =0, numberOfRows = board.getNumberOfRows(), leftBound=0, rightBound=0, selectedRow=0;
        int[] unmarkedSticksArray = new int[numberOfRows];
        ArrayList<Integer> unmarkedRows = new ArrayList<>();
        //initializing iteration in order to check how many unmarked sticks are in every row-
        //iterating over the rows of the board-
        for (int row=1; row < numberOfRows+1; row++){
			unmarkedSticksArray[row-1]+= getUnmarkedSticksNumberInRow(board, row);
			//if not all of the row is marked, it is inserted in the unmarkedRows array-
			if (unmarkedSticksArray[row-1] != 0){
                unmarkedRows.add(row);
            }
        } //initializing a default, always-valid move for default return value-
        Move possibleMove = checkForSequence(board, 1,unmarkedRows.get(0));
		//calculating the board's nim sum, which is all of the row's number of unmarked sticks with the
		// XOR operator between them-
        for (int localNimSumIndex = 0; localNimSumIndex < unmarkedSticksArray.length; localNimSumIndex++){
        	nimSum ^= unmarkedSticksArray[localNimSumIndex];
        // if nimSum!=0 we will want to make it zero-
        } if ((nimSum!=0) && (unmarkedRows.size()>1)){
            //iterating over the rows in order to check which is not a suitable row to make the move on-
            for (int row=1; row < numberOfRows+1; row++){
            	//calculating the joint nim sum. a row that the joint nim sum (nimsum^number of unmarked sticks in the
				// row) is smaller then the amount of unmarked stick in it, is not suitable-
                int jointNimSum = (nimSum^unmarkedSticksArray[row-1]);
                if (jointNimSum<unmarkedSticksArray[row-1]){
                	//so no we can set the unwanted sequence length-
                    int unwantedSequenceLength = unmarkedSticksArray[row-1]-jointNimSum;
                    //we will no iterate over the possible sequence lengths, excluding the unwanted one-
                    for (int i=1; i<board.getRowLength(selectedRow)+1; i++){
						if (i != unwantedSequenceLength){
							possibleMove = checkForSequence(board, i, row);
                    		//if such there is no sequence, we will update the move's pre-initialized variables to the
							//default variables and then return
                    		if ((possibleMove.getRow()==0)||(possibleMove.getRightBound()==0)||
									(possibleMove.getLeftBound()==0)){
                    			possibleMove= checkForSequence(board, 1 ,unmarkedRows.get(0));
								return possibleMove;
                    		} else {
                    			//otherwise- return the result-
                    			return possibleMove;
							}
						}
                    }
                }
            }
			//if there is only one unmarked row, we will check how many unmarked sticks it has-
        } else if (unmarkedRows.size()==1) {
        	if ((board.getNumberOfUnmarkedSticks()==1)&&(getUnmarkedSticksNumberInRow(board, 5)==1)){
        		possibleMove = new Move(5,1,1);
        		return possibleMove;
			}
			selectedRow = unmarkedRows.get(0);
			//if there is more than one stick unmarked, let say there are x unmarked sticks, and we will mark x-1
			// sticks, if it is possible. if there is only one left, we will mark it and loose.
			int numberOfUnmarkedSticks = unmarkedSticksArray[selectedRow - 1];
			//we will check what is the maximum possible sequence and set the bounds accordingly-
			if (numberOfUnmarkedSticks == 1) {
				//if there is only one stick left, we lost and there is only one move possible-
				possibleMove = checkForSequence(board, 1, selectedRow);
			} else {
				//otherwise, we will check if a sequence of length of the amount of left unmarked sticks minus 1-
				possibleMove = checkForSequence(board, numberOfUnmarkedSticks - 1, selectedRow);
				//if it is not suitable- we will return the default value-
			} if ((possibleMove.getRow()==0)||(possibleMove.getRightBound()==0)||(possibleMove.getLeftBound()==0)) {
				possibleMove = checkForSequence(board, 1, unmarkedRows.get(0));
				return possibleMove;
			} else{
				return possibleMove;
			}
			//if nim sum is 0, we will have to generate a random move-
		} else {
            Random randomGenerator = new Random();
            // we will randomize a row from the existing unmarkedRowsArray-
            int selectedRowIndex = randomGenerator.nextInt(unmarkedRows.size());
            selectedRow = unmarkedRows.get(selectedRowIndex);
            //we will iterate over the randomized row in order to see what possible sequences are available. we will
            //set the move bounds accordingly
			int numberOfUnmarkedSticksInRow = unmarkedSticksArray[selectedRow-1];
            for (int i=numberOfUnmarkedSticksInRow; i>0; i--){
                possibleMove = checkForSequence(board, i, selectedRow);
                //if the received move is unsuitable, we will return the default-
            	if ((possibleMove.getRow()==0)||(possibleMove.getRightBound()==0)||(possibleMove.getLeftBound()==0)) {
					possibleMove = checkForSequence(board, 1, unmarkedRows.get(0));
            		return possibleMove;
            	}
        	}  //either way we will return a valid move-
    	} return possibleMove;
	}

	/**
	 * Interact with the user to produce his move.
	 */
	private Move produceHumanMove(Board board){
		//printing the first message-
		System.out.println(INPUT_REQUEST_MSG);
		int userInput = scanner.nextInt();
		//initializing a while loop, making sure the human player gives the right input
		while (userInput != 2){
			//printing the board if the human player asked for it
			if (userInput == 1){
				System.out.println(board.toString());
				System.out.println(INPUT_REQUEST_MSG);
			// if input is not 1 or 2, it is wrong input, and an error message will be printed-
			} else {
				System.out.println(UNSUPPORTED_COMMAND);
				System.out.println(INPUT_REQUEST_MSG);
			}
			userInput = scanner.nextInt();
		}
		//initializing inputs from the human user-
		//printing the relevant messages with the right order, waiting for input-
		System.out.println(ROW_INPUT_REQUEST);
		int rowInput = scanner.nextInt();
		System.out.println(LEFT_INPUT_REQUEST);
		int leftInput = scanner.nextInt();
		System.out.println(RIGHT_INPUT_REQUEST);
		int rightInput = scanner.nextInt();
		//returning the move given by the human user
		return new Move(rowInput, leftInput, rightInput);
		}
	
	/**
	 * Uses a winning heuristic for the Nim game to produce a move.
	 */
	private Move produceHeuristicMove(Board board){

		int numRows = board.getNumberOfRows();
		int[][] bins = new int[numRows][BINARY_LENGTH];
		int[] binarySum = new int[BINARY_LENGTH];
		int bitIndex,higherThenOne=0,totalOnes=0,lastRow=0,lastLeft=0,lastSize=0,lastOneRow=0,lastOneLeft=0;
		
		for(bitIndex = 0;bitIndex<BINARY_LENGTH;bitIndex++){
			binarySum[bitIndex] = 0;
		}
		
		for(int k=0;k<numRows;k++){
			
			int curRowLength = board.getRowLength(k+1);
			int i = 0;
			int numOnes = 0;
			
			for(bitIndex = 0;bitIndex<BINARY_LENGTH;bitIndex++){
				bins[k][bitIndex] = 0;
			}
			
			do {
				if(i<curRowLength && board.isStickUnmarked(k+1,i+1) ){
					numOnes++;
				} else {
					
					if(numOnes>0){
						
						String curNum = Integer.toBinaryString(numOnes);
						while(curNum.length()<BINARY_LENGTH){
							curNum = "0" + curNum;
						}
						for(bitIndex = 0;bitIndex<BINARY_LENGTH;bitIndex++){
							bins[k][bitIndex] += curNum.charAt(bitIndex)-'0'; //Convert from char to int
						}
						
						if(numOnes>1){
							higherThenOne++;
							lastRow = k +1;
							lastLeft = i - numOnes + 1;
							lastSize = numOnes;
						} else {
							totalOnes++;
						}
						lastOneRow = k+1;
						lastOneLeft = i;
						
						numOnes = 0;
					}
				}
				i++;
			}while(i<=curRowLength);
			
			for(bitIndex = 0;bitIndex<BINARY_LENGTH;bitIndex++){
				binarySum[bitIndex] = (binarySum[bitIndex]+bins[k][bitIndex])%2;
			}
		}
		
		
		//We only have single sticks
		if(higherThenOne==0){
			return new Move(lastOneRow,lastOneLeft,lastOneLeft);
		}
		
		//We are at a finishing state				
		if(higherThenOne<=1){
			
			if(totalOnes == 0){
				return new Move(lastRow,lastLeft,lastLeft+(lastSize-1) - 1);
			} else {
				return new Move(lastRow,lastLeft,lastLeft+(lastSize-1)-(1-totalOnes%2));
			}
			
		}
		
		for(bitIndex = 0;bitIndex<BINARY_LENGTH-1;bitIndex++){
			
			if(binarySum[bitIndex]>0){
				
				int finalSum = 0,eraseRow = 0,eraseSize = 0,numRemove = 0;
				for(int k=0;k<numRows;k++){
					
					if(bins[k][bitIndex]>0){
						eraseRow = k+1;
						eraseSize = (int)Math.pow(2,BINARY_LENGTH-bitIndex-1);
						
						for(int b2 = bitIndex+1;b2<BINARY_LENGTH;b2++){
							
							if(binarySum[b2]>0){
								
								if(bins[k][b2]==0){
									finalSum = finalSum + (int)Math.pow(2,BINARY_LENGTH-b2-1);
								} else {
									finalSum = finalSum - (int)Math.pow(2,BINARY_LENGTH-b2-1);
								}
								
							}
							
						}
						break;
					}
				}
				
				numRemove = eraseSize - finalSum;
				
				//Now we find that part and remove from it the required piece
				int numOnes=0,i=0;
				while(numOnes<eraseSize){

					if(board.isStickUnmarked(eraseRow,i+1)){
						numOnes++;
					} else {
						numOnes=0;
					}
					i++;
					
				}
				return new Move(eraseRow,i-numOnes+1,i-numOnes+numRemove);
			}
		}
		
		//If we reached here, and the board is not symmetric, then we only need to erase a single stick
		if(binarySum[BINARY_LENGTH-1]>0){
			return new Move(lastOneRow,lastOneLeft,lastOneLeft);
		}
		
		//If we reached here, it means that the board is already symmetric, and then we simply mark one stick from the last sequence we saw:
		return new Move(lastRow,lastLeft,lastLeft);		
	}


}

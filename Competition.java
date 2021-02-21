import java.util.Scanner;

/**
 * The Competition class represents a Nim competition between two players, consisting of a given number of rounds. 
 * It also keeps track of the number of victories of each player.
 */
public class Competition {

	/** instance's fields **/
	private Player player1;
	private Player player2;
	private boolean verboseMode;
	private int wins1;
	private int wins2;

	/** class constants **/
	private static final String INVALID_MOVE_MSG = "Invalid move. Enter another:";
	private static final String WELCOME_MSG = "Welcome to the sticks game!";
	private static final int ID_1 = 1;
	private static final int ID_2 = 2;
	private static final String PLAYER2_VICTORY = "Player " +ID_2+ " won!";
	private static final String PLAYER1_VICTORY = "Player " +ID_1+ " won!";

    /**
     * Receives two Player objects, representing the two competing opponents, and a flag determining whether messages
     * should be displayed.
     * @param player1 The Player objects representing the first player.
     * @param player2 The Player objects representing the second player.
     * @param displayMessage a flag indicating whether game play messages should be printed to the console.
     */
	public Competition (Player player1,
                        Player player2,
                        boolean displayMessage){
        this.player1 = player1;
        this.player2 = player2;
        verboseMode = displayMessage;
        wins1 = 0;
		wins2 = 0;
    }

    /**
     *If playerPosition = 1, the results of the first player is returned. If playerPosition = 2, the result of
     * the second player is returned. If playerPosition equals neiter, -1 is returned.
     * @param playerPosition playerPosition should be 1 or 2, corresponding to the first or the second player in
     * the competition.
     * @return the number of victories of a player.
     */
    public int getPlayerScore(int playerPosition){
        //return requested player's score
    	if (playerPosition==1){
			return wins1;
		} else if (playerPosition==2){
        	return wins2;
		//return -1 if player is invalid-
    	} return -1;
    }

	/**
	 * an aid method for the PlayMultipleRounds- it manages a single turn, receiving a player and a board, and
	 * returning nothing.
	 * @param player the player whose turn it is
	 * @param board current board
	 */
    private void singleTurnManager(Player player, Board board){
    	// displays the turn's first message
		messageDisplayManager("Player "+player.getPlayerId()+", it is now your turn!");
		//initializing a move, using 'produceMove' method
		Move player_move = player.produceMove(board);
		//checks if the move is valid-
		int result = board.markStickSequence(player_move);
		// a while loop- if move is not valid, it shows an error message and continues to ask for new moves, until one
		// is received.
		while (result != 0) {
			messageDisplayManager(INVALID_MOVE_MSG);
			player_move = player.produceMove(board);
			result = board.markStickSequence(player_move);
		}
		//will print end-of-turn message, informing the move preformed
		messageDisplayManager("Player "+player.getPlayerId()+" made the move: "+player_move.toString());
	}

    /**
     * Run the game for the given number of rounds.
     * @param numRounds number of rounds to play.
     */
    public void playMultipleRounds(int numRounds) {
		//initializing the round counting variable
    	int roundsCount = 0;
    	//printing the first message of the competition- a one notifying the number of rounds and the players
		System.out.println("Starting a Nim competition of "+numRounds+" rounds between a "+player1.getTypeName()+
				" player and a "+player2.getTypeName()+" player.");
		//the while loop that manages the amount of matches is initialized-
		while (roundsCount < numRounds) {
			//create a Board
			Board board = new Board();
			//welcome message is printed out
			messageDisplayManager(WELCOME_MSG);
			//the while loop that manages one match-
			while(board.getNumberOfUnmarkedSticks() != 0){
				//calling the turn managing method for player 1
				singleTurnManager(player1, board);
				//if game is done- the score is updated and the loop stops-
				if (board.getNumberOfUnmarkedSticks() == 0) {
					wins2++;
					messageDisplayManager(PLAYER2_VICTORY);
					break;
				} //calling the turn managing method for player 2
				singleTurnManager(player2, board);
				//if game is done- the score is updated and the loop stops-
				if (board.getNumberOfUnmarkedSticks() == 0){
					wins1++;
					messageDisplayManager(PLAYER1_VICTORY);
					break;
				}
			} // after each turn, the rounds count is being updated-
			roundsCount++;
		} //goodbye message is printed out, informing the score-
		System.out.println("The results are "+wins1+":"+wins2);
	}

	/**
	 * Returns the integer representing the type of player 1; returns -1 on bad
	 * input.
	 */
	private static int parsePlayer1Type(String[] args){
		try{
			return Integer.parseInt(args[0]);
		} catch (Exception E){
			return -1;
		}
	}

	/**
	 * Returns the integer representing the type of player 2; returns -1 on bad
	 * input.
	 */
	private static int parsePlayer2Type(String[] args){
		try{
			return Integer.parseInt(args[1]);
		} catch (Exception E){
			return -1;
		}
	}

	/**
	 * Returns the integer representing the type of player 2; returns -1 on bad
	 * input.
	 */
	private static int parseNumberOfGames(String[] args){
		try{
			return Integer.parseInt(args[2]);
		} catch (Exception E){
			return -1;
		}
	}

	/**
	 * a method that manages all the messages that needs to be printed only if there is a displayMessage = true
	 * @param message a string to be or not to be printed
	 */
	private void messageDisplayManager(String message){
		if (verboseMode){
			System.out.println(message);
		}
	}

	/**
	 * The method runs a Nim competition between two players according to the three user-specified arguments.
	 * (1) The type of the first player, which is a positive integer between 1 and 4: 1 for a Random computer
	 *     player, 2 for a Heuristic computer player, 3 for a Smart computer player and 4 for a human player.
	 * (2) The type of the second player, which is a positive integer between 1 and 4.
	 * (3) The number of rounds to be played in the competition.
	 * @param args an array of string representations of the three input arguments, as detailed above.
	 */
	public static void main(String[] args) {

		int p1Type = parsePlayer1Type(args);
		int p2Type = parsePlayer2Type(args);
		int numGames = parseNumberOfGames(args);
		//initialize a scanner object-
		Scanner scanner = new Scanner(System.in);
        // create player1 object of p1Type type with id=1
        Player player1 = new Player(p1Type,1,scanner);
		// create player2 object of p2Type type with id=2
        Player player2 = new Player(p2Type,2,scanner);
        boolean verboseMode = false;
        if ((player1.getPlayerType() == 4) || ((player2.getPlayerType() == 4))){
        	verboseMode = true; }

		// initializing a competition object according to the displayMessage input received as an argument-
        Competition newCompetition = new Competition(player1, player2, verboseMode);
        newCompetition.playMultipleRounds(numGames);
        scanner.close();
	}	
	
}

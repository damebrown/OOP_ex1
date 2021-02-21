/*
  Tester for ex1.
  Created by: Evyatar
  version 2.4
  <p>
  You are more then welcome to improve the tester - add tests, fix bugs, improve the current ones or make
  the tester more accessible. If you do so, update the changelog below.
  <p>
  Changelog:
  29/03/2018 - version 1.0 - Evyatar - Created. the tester doesn't test the human player, doesn't tests
  the prints and doesn't test the main(String[] args) function.
  <p>
  30/03/2018 - version 2.0 - Evyatar - Now test human vs human, and check if the prints were correct.
  30/03/2018 - version 2.1 - Evyatar - Fix the issue with the id's (I hope),  and now test the print for
  										invalid input in the main functions.
  03/04/2018 - version 2.2 - Mattan - Switched the "got" and "expected" values in the human vs human tests
  									  (16 and 17).
  									- Replaced \r with \r\n in test 18.
  03/04/2018 - version 2.3 - Mattan - Renamed main to match filename.
  03/04/2018 - version 2.4 - Mattan - Removed test 18 - check input error and added "Tests complete" message.


 */

import java.io.*;
import java.util.Scanner;

public class TesterEx1OOP {

	// thing that has to do with stopPrints methods.
	private static PrintStream originalStream = System.out;
	private static PrintStream dummyStream = new PrintStream(
			new OutputStream() {
				public void write(int b) {/*NO-OP*/}
			});
	private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private static PrintStream printsRecorder = new PrintStream(baos);

	public static void main(String[] args) {

		Scanner scanner = new Scanner(System.in);

		//Move tests
		title("Move tests");
		Move move1 = new Move(2, 2, 3);
		test(move1.getRow() == 2, "1");
		test(move1.getLeftBound() == 2, "2");
		test(move1.getRightBound() == 3, "3");
		test(move1.toString().equals("2:2-3"), "4");

		//random tests
		title("Random tests");
		Player randPlayer1 = new Player(1, 1, scanner);
		Player randPlayer2 = new Player(1, 2, scanner);

		// generate 4 random moves for the same board
		Move[] randMoves1 = new Move[4];
		for (int i = 0; i < randMoves1.length; i++) {
			Board board = new Board();
			board.markStickSequence(move1);
			randMoves1[i] = randPlayer1.produceMove(board);
			// test if the random move is valid.
			test(board.markStickSequence(randMoves1[i]) == 0, "5." + i);
		}
		// check if the random moves are different, if it fail's, try again. very odd if 4 random moves are
		// the same.
		test(!randMoves1[1].toString().equals(randMoves1[2].toString())
				|| !randMoves1[1].toString().equals(randMoves1[3].toString())
				|| !randMoves1[1].toString().equals(randMoves1[4].toString()), "6");

		// we do it again for another board
		Move[] randMoves2 = new Move[4];
		for (int i = 0; i < randMoves2.length; i++) {
			Board board = new Board();
			board.markStickSequence(move1);
			board.markStickSequence(randMoves1[0]);
			randMoves2[i] = randPlayer1.produceMove(board);
			// test if the random move is valid.
			test(board.markStickSequence(randMoves2[i]) == 0, "7." + i);
		}
		test(!randMoves2[0].toString().equals(randMoves2[1].toString())
				|| !randMoves2[0].toString().equals(randMoves2[2].toString())
				|| !randMoves2[0].toString().equals(randMoves2[3].toString()), "8");

		// and again, now we try to get a random move, when there is a few options.
		for (int i = 0; i < randMoves2.length; i++) {
			Board board = new Board();
			board.markStickSequence(new Move(1, 1, 9));
			board.markStickSequence(new Move(2, 1, 7));
			board.markStickSequence(new Move(3, 1, 4));
			board.markStickSequence(new Move(4, 2, 3));
			//System.out.println(board);
			//test if the random move is valid.
			test(board.markStickSequence(randPlayer1.produceMove(board)) == 0, "9." + i);
		}


		//Smart AI tests
		title("Smart AI tests");
		Player smartPlayer1 = new Player(3, 1, scanner);
		Player smartPlayer2 = new Player(3, 2, scanner);
		Board board = new Board();
		Move move;
		int test5Index = 1;
		while (board.getNumberOfUnmarkedSticks() > 0) {
			move = smartPlayer1.produceMove(board);
			if (board.markStickSequence(move) == 0) {
				test(true, "10." + test5Index);
			} else {
				test(false, "10"); // the move was invalid.
				break;
			}
			test5Index++;
		}
		//Competition tests
		title("Competition tests");

		Competition competition = new Competition(randPlayer1, randPlayer2, true);

		test(competition.getPlayerScore(1) == 0, "11");
		test(competition.getPlayerScore(2) == 0, "12");


		// random vs random
		//stopPrints();
		competition.playMultiple(500);
		resumePrints();
		test(competition.getPlayerScore(1) + competition.getPlayerScore(2) == 500, "13");
		System.out.println(competition.getPlayerScore(1) + ", " + competition.getPlayerScore(2) +
				" <- i'ts a good sigh if the numbers are relatively close.");

		// random vs smart
		stopPrints();
		competition = new Competition(randPlayer1, smartPlayer2, false);
		competition.playMultiple(1000);
		resumePrints();
		test(competition.getPlayerScore(2) - 100 > competition.getPlayerScore(1), "14");

		// smart vs random
		stopPrints();
		competition = new Competition(smartPlayer1, randPlayer2, false);
		competition.playMultiple(1000);
		resumePrints();
		test(competition.getPlayerScore(1) - 100 > competition.getPlayerScore(2), "15");

		// human vs human
		title("Human tests");
		test(testHuman(true), "16"); // test prints verbose mode. (with red lines)
		test(testHuman(false), "17"); // test prints no verbose mode.
		System.out.println("Tests Complete!");
//		 main function tests
//		title("main function tests");
//		test(testInputError(), "18");
	}


	/*
	* simulate a human vs human game, with input, and test if all the print lines are correct, if there is
	* a problem with a line, print an informative message.
	 */
	private static boolean testHuman(boolean isVerbose) {
		boolean is16Passed = true;
		String input = "1 2 1 1 9 2 1 1 1 2 2 1 7 2 3 1 5 5 2 4 1 3 1 2 1 1 1 2 5 1 1"; // list of moves
		Scanner fakeScanner = new Scanner(input);
		Player human1 = new Player(4, 1, fakeScanner);
		Player human2 = new Player(4, 2, fakeScanner);
		Competition competition = new Competition(human1, human2, isVerbose);
		recordPrints();
		competition.playMultiple(1);
		resumePrints();

		String[] yourGame = getPrintRecordsAndReset().replace("\r", "").split("\n");// your game prints
		String[] correctGame;
		if (isVerbose)
			correctGame = getString(0).split("\n"); // the game
		else
			correctGame = getString(1).split("\n"); // the game
		// prints to compare
		resumePrints();
		if (yourGame.length != correctGame.length) {
			System.out.println("the number of rows is different, may throw exception");
			is16Passed = false;
		}
		for (int i = 0; i < correctGame.length; i++) {
			if (!yourGame[i].equals(correctGame[i])) {

				System.out.printf("in line %s, \nexpected:\t%s\ngot:\t\t%s\n", i, correctGame[i], yourGame[i]);
				is16Passed = false;
			}
		}
		return is16Passed;
	}
	private static boolean testInputError() {
		recordPrints();
		String[] testArgs1 = {"0", "2", "1"}; // all of these inputs to the main function should prints the
		// "Invalid input message"
		Competition.main(testArgs1);
		String inputError1 = getPrintRecordsAndReset();
		String[] testArgs2 = {"1", "5", "1"};
		Competition.main(testArgs2);
		String inputError2 = getPrintRecordsAndReset();
		String[] testArgs3 = {"6", "2", "0"};
		Competition.main(testArgs3);
		String inputError3 = getPrintRecordsAndReset();
		String[] testArgs4 = {"1", "1"};
		Competition.main(testArgs4);
		String inputError4 = getPrintRecordsAndReset();

		resumePrints();

		return inputError1.replace("\r\n", "").equals(getString(2))
				&& inputError2.replace("\r\n", "").equals(getString(2))
				&& inputError3.replace("\r\n", "").equals(getString(2))
				&& inputError4.replace("\r\n", "").equals(getString(2));

	}


	/*
	return a string according to the index.
	 */
	private static String getString(int index){
		String[] strings = new String[3];

		// correct records for human vs human verbose.
		strings[0] = "Starting a Nim competition of 1 rounds between a Human player and a Human player.\n" +
				"Welcome to the sticks game!\n" +
				"Player 1, it is now your turn!\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"    1    \n" +
				"   111   \n" +
				"  11111  \n" +
				" 1111111 \n" +
				"111111111\n" +
				"\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Player 1 made the move: 1:1-9\n" +
				"Player 2, it is now your turn!\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Invalid move. Enter another:\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Player 2 made the move: 2:1-7\n" +
				"Player 1, it is now your turn!\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Player 1 made the move: 3:1-5\n" +
				"Player 2, it is now your turn!\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Unsupported command\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Player 2 made the move: 4:1-3\n" +
				"Player 1, it is now your turn!\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"    1    \n" +
				"   000   \n" +
				"  00000  \n" +
				" 0000000 \n" +
				"000000000\n" +
				"\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Invalid move. Enter another:\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Player 1 made the move: 5:1-1\n" +
				"Player 2 won!\n" +
				"The results are 0:1\n";

		// correct records for human vs human No verbose
		strings[1] = "Starting a Nim competition of 1 rounds between a Human player and a Human player.\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"    1    \n" +
				"   111   \n" +
				"  11111  \n" +
				" 1111111 \n" +
				"111111111\n" +
				"\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Unsupported command\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"    1    \n" +
				"   000   \n" +
				"  00000  \n" +
				" 0000000 \n" +
				"000000000\n" +
				"\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"Press 1 to display the board. Press 2 to make a move:\n" +
				"Enter the row number:\n" +
				"Enter the index of the leftmost stick:\n" +
				"Enter the index of the rightmost stick:\n" +
				"The results are 0:1";

		strings[2] = "Invalid Input. There should be 3 arguments:\n" + 	// print for invalid input to 'main'
				"The first two are integers between 1 and 4.\n" +		// according to school solution.
				"The third is an integer larger than 0";

		return strings[index];
	}


	/*
	print "pass" if "test" is true, print "failed" otherwise.
	 */
	private static void test(boolean test, String name) {
		if (test) {
			System.out.printf("Test %s passed", name);
		} else {
			System.out.printf("Test %s failed -------------------------", name);
		}
		System.out.println();
	}

	/*
	print a title.
	 */
	private static void title(String msg) {
		System.out.println();
		System.out.println(msg);
		System.out.println();
	}

	/*
	stop all prints to console, System.out.print will do nothing.
	 */
	private static void stopPrints() {
		System.setOut(dummyStream);
	}

	private static void resumePrints() {
		System.setOut(originalStream);
	}

	/*
	record all prints.
	 */
	private static void recordPrints() {
		System.setOut(printsRecorder);
	}

	private static String getPrintRecordsAndReset() {
		String string = baos.toString();
		baos.reset();
		return string;
	}

}

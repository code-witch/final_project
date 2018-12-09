package game;

import java.io.IOException;
import java.util.ArrayList;

import card.Card;
import card.RailRoad;
import card.TitleDeed;
import dependancy.ConsoleUI;
import dependancy.menu;
import enumeration.TitleColor;
import enumeration.Token;
import card.Property;

/**
 * 
 * @author Searjasub Lopez
 * @author Spencer Schmollinger
 * @author Brooke Showers
 *
 */

public class Game {

	// Class level variables
	public int turn, countPlayers, roundCount, countOfDoublesRolled = 0;
	public Player[] players;
	Die die = new Die();
	public Board board = new Board();
	
	//TODO Make more Java Docs

	/**
	 * Initialize the game by assigning names, tokens and initial balance.
	 * @param totalPlayers insert the number of player
	 */
	private void init(int totalPlayers) throws IOException {

		for (Token t : Token.values()) {
			menu.tokenArray.add(t);
		}

		players = new Player[totalPlayers];
		for (int i = 0; i < players.length; i++) {
			String playerName = ConsoleUI.promptForInput("\nEnter player " + (i + 1) + "'s name", false);

			System.out.println("\nOk, " + playerName + " it is time to choose your token.");
			Token selection = menu.chooseYourToken();

			System.out.println("\nTime to roll the dice to see who starts.");
			int total = rollForOrder();

			// Player(String name, Token token, int balance, int turn, int location)
			Player newPlayer = new Player(playerName, selection, 1500, total, 0);
			players[i] = newPlayer;

			// Finish interaction with players[i]
			if (players.length > totalPlayers) {
				System.out.println("Thank you " + players[i].getName() + ".");
			} else {
				System.out.println("Thank you " + players[i].getName() + ". Now let me ask your friend.\n");
			}

		}
		checkForTie();
		sort();
	}

	/**
	 * Method that lets the user roll
	 * 
	 * @return the total number the player rolled
	 */
	private int rollForOrder() throws IOException {
		String[] options = new String[1];
		options[0] = "Let's roll those dice!";
		int rollOptions = ConsoleUI.promptForMenuSelection(options);
		if (rollOptions == 0) {
			die.roll();
			whatYouRolled();
		}
		return die.getTotal();
	}

	/**
	 * Tiny method that will print each die you rolled and the total as well
	 */
	private void whatYouRolled() {
		System.out.println("\nYou have rolled " + die.getDieOne() + " and " + die.getDieTwo());
		System.out.println("Your total is: " + die.getTotal());
	}

	/**
	 * Checks for tie when players are rolling to see who's starts (CURRENTLY NEEDS
	 * FIX)
	 */
	private void checkForTie() throws IOException {
		for (int i = 2; i <= 12; i++) {
			int count = 0;
			for (int j = 0; j < players.length; j++) {
				if (i == players[j].getTurn()) {
					count++;
				}
			}
			if (count > 1) {
				for (int j2 = 0; j2 < players.length; j2++) {
					if (i == players[j2].getTurn()) {
						System.out.println("\nThere is a tie!");
						System.out.println("\n" + players[j2].getName() + ", you can roll again");
						players[j2].setTurn(rollForOrder());
						count = 0;
					}
				}
			}
		}
	}

	/**
	 * This method will sort the player in descending order
	 */
	public void sort() {
		for (int i = 0; i < players.length - 1; i++) {
			for (int j = 0; j < players.length - i - 1; j++) {
				if (players[j].getTurn() < players[j + 1].getTurn()) {
					Player temp = players[j];
					players[j] = players[j + 1];
					players[j + 1] = temp;
				}
			}
		}
	}

	/**
	 * Without a while loop to only run the game once. Later on we can comment them
	 * back to let the game keep running. Might need a little fix after one game is
	 * played
	 * 
	 * @throws InterruptedException
	 */
	public void run() throws IOException, InterruptedException {
		// boolean keepRunning = true;
		// while (keepRunning) {

		board.printWelcome();
		int action = menu.printMainMenu();
		takeAction(action);

		// keepRunning = takeAction(action);
		// }
	}

	/**
	 * Handles the selection from the user
	 * 
	 * @param action the selection from user of the main menu
	 * @return if the game needs to keepRunning or not
	 */
	private boolean takeAction(int action) throws IOException {
		switch (action) {
		case 0:
			classicMonopolyRules();
			break;
		case 1:
			return false;
		default:
			throw new IllegalArgumentException("Invalid action " + action);
		}
		return true;
	}

	/**
	 * After selection this method through the main menu, it will play this version
	 * of monopoly
	 */
	private void classicMonopolyRules() throws IOException {
		boolean gameOver = false;
		System.out.println("Welcome to Monopoly\nClassic Rules");
		int howManyPlayers = ConsoleUI.promptForInt("Let's get started by having a count of the players.\n"
				+ "Remember that the minimum is 2 and maximum is 8", 2, 8);
		init(howManyPlayers);
		while (!gameOver) {
			// handle turns
			// TODO Finish what happens when player is bankrupt
			for (int i = 0; i < players.length; i++) {
				if (!players[i].bankrupt) {
					turn(players[i]);
				} else {
					continue;
				}
			}
			roundCount++;
		}
	}

	/**
	 * This method will handle the first time a player takes the turn
	 * 
	 * @param p the player taking the turn
	 */
	public void turn(Player currentPlayer) throws IOException {

		if (currentPlayer.isInJail == true) {
			System.out.println("\n\n\nOk " + currentPlayer.getName() + ", let's get you out of jail.");
			handleJail(currentPlayer);
		} else {
			boolean isYourTurn = true;
			while (isYourTurn) {
				board.printBoard(currentPlayer);
				System.out.println("\nAlright player, " + currentPlayer.getToken() + " you're up.");
				int action = menu.printTurnMenu();
				switch (action) {
				case 0:
					isYourTurn = regularTurn(currentPlayer);
					break;
				case 1:
					showBalance(currentPlayer);
					break;
				case 2:
					showProperties(currentPlayer);
					break;
				case 3:
					sell(currentPlayer);
					break;
				case 4:
					buy(currentPlayer);

					break;
				default:
					throw new IllegalArgumentException("Invalid action " + action);
				}
			}
		}
	}

	/**
	 * This method will handle each option of the menu to help the currentPlayer to
	 * get out of jail.
	 * 
	 * @param currentPlayer who's turn is it.
	 */
	private void handleJail(Player currentPlayer) throws IOException {
		int action = menu.printJailMenu();
		switch (action) {
		case 0:

			System.out.println("You have 3 chances to get doubles and get out of jail this turn");

			int selection = ConsoleUI.promptForInt("[0]\tRoll dice", 0, 0);
			if (selection == 0) {
				die.roll();
				whatYouRolled();
				if (die.getDieOne() == die.getDieTwo()) {
					breakOutOfJail(currentPlayer);
					movePlayer(die.getTotal(), currentPlayer);
					board.printBoard(currentPlayer);
					currentPlayer.setTurnInJail(0);
					// handle what happens when you lay on a property
				} else {
					if (currentPlayer.getTurnInJail() > 1) {
						System.out.println(
								"\nSince you have rolled 3 times and you didn't rolled doubles, the bank took $50 off of your balance.");
						breakOutOfJail(currentPlayer);
						currentPlayer.setBalance(-50);
						break;
					}
					currentPlayer.setTurnInJail(currentPlayer.getTurnInJail() + 1);
					turnAfterRoll(currentPlayer);
					break;
				}
			}
			breakOutOfJail(currentPlayer);
			break;
		case 1:
			if (currentPlayer.jailCardOwned[1] != null) {
				useCardToGetOut(currentPlayer);
			} else if (currentPlayer.jailCardOwned[0] != null) {
				useCardToGetOut(currentPlayer);
			}
			break;
		case 2:
			System.out.println("*********************************************************************" + "\n\nOk "
					+ currentPlayer.getName() + ", you are free now.");
			currentPlayer.setBalance(-50);
			breakOutOfJail(currentPlayer);
			turn(currentPlayer);
			break;
		default:
			throw new IllegalArgumentException("Invalid action " + action);
		}

	}

	private void useCardToGetOut(Player currentPlayer) throws IOException {
		breakOutOfJail(currentPlayer);
		System.out.println("\n*********************************************************************"
				+ "\nYou haved used your \"Get Out Of Jail\" card! Hopefully we won't see you again here.");
		currentPlayer.jailCardOwned[1] = null;
		turn(currentPlayer);

	}

	/**
	 * Reset the flag isInJail of the currentPlayer to false
	 * 
	 * @param currentPlayer player currently taking the turn
	 * @return false to break out of the loop
	 */
	private boolean breakOutOfJail(Player currentPlayer) {
		currentPlayer.isInJail(false);
		return false;
	}

	/**
	 * Method that will let you either take a regular turn if you rolled doubles or
	 * your end of the turn options if you did not rolled doubles
	 * 
	 * @param currentPlayer who's turn it.
	 * @return if the player ended his turn or not.
	 */
	private boolean regularTurn(Player currentPlayer) throws IOException {
		die.roll();
		whatYouRolled();
		movePlayer(die.getTotal(), currentPlayer);

		// TODO We can print with thread sleep like if the player is moving the token
		// Simple println that will show each location name one by one until reached the
		// total thrown

		System.out.println("\n*************************************\n" + "You landed on: "
				+ board.squares[currentPlayer.getLocation()].getName() + "\n"
				+ "*************************************\n");
		landOnProperty(currentPlayer, die.getTotal());

		if (die.getDieOne() == die.getDieTwo()) {
			countOfDoublesRolled++;
			if (countOfDoublesRolled == 3) {
				System.out.println(""
						+ "\n*************************************************************************************\n"
						+ currentPlayer.name
						+ " you have rolled 3 doubles. You will not be visiting jail this time; you will be going to jail.\n"
						+ "You also lose your turn. Better luck next time!\n Have fun pumping iron.\n"
						+ "*************************************************************************************");
				sendToJail(currentPlayer, 10);
				currentPlayer.isInJail(true);
				countOfDoublesRolled = 0;
				return false;
			}
			turn(currentPlayer);
			return false;
		}
		turnAfterRoll(currentPlayer);
		return false;
	}

	/**
	 * Method that will handle the options available when the player lands on a
	 * property
	 * 
	 * @param currentPlayer taking the turn
	 */
	private void landOnProperty(Player currentPlayer, int location) throws IOException {

		// MEDITERRANEAN AVENUE
		if (currentPlayer.getLocation() == 1) {
			if (board.ownsDeed(0, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 0, -60);
			} else {
				payRent(currentPlayer, 2, 0);
			}
		}
		// COMMUNITY CHEST
		else if (currentPlayer.getLocation() == 2) {
			handleCommunityChestCard(currentPlayer);
		}
		// BALTIC AVENUE
		else if (currentPlayer.getLocation() == 3) {
			if (board.ownsDeed(1, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 1, -60);
			} else {
				payRent(currentPlayer, 4, 1);
			}
		}
		// INCOME TAX
		else if (currentPlayer.getLocation() == 4) {
			handleIncomeTax(currentPlayer);
		}
		// READING RAILROAD
		else if (currentPlayer.getLocation() == 5) {
			if (board.ownsDeed(2, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 2, -200);
			} else {
				railRoadRent(currentPlayer, 2);
			}
		}
		// ORIENTAL AVENUE
		else if (currentPlayer.getLocation() == 6) {
			if (board.ownsDeed(3, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 3, -100);
			} else {
				payRent(currentPlayer, 6, 3);
			}
		}
		// CHANCE
		else if (currentPlayer.getLocation() == 7) {
			handleChanceCard(currentPlayer);
		}
		// VERMONT AVENUE
		else if (currentPlayer.getLocation() == 8) {
			if (board.ownsDeed(4, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 4, -board.deeds[location].getCost());
			} else {
				payRent(currentPlayer, 6, 4);
			}
		}
		// CONNECTICUT AVENUE
		else if (currentPlayer.getLocation() == 9) {
			if (board.ownsDeed(5, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 5, -120);
			} else {
				payRent(currentPlayer, 8, 5);
			}
		}
		// ST. CHARLES PLACE
		else if (currentPlayer.getLocation() == 11) {
			if (board.ownsDeed(6, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 6, -140);
			} else {
				payRent(currentPlayer, 10, 6);
			}
		}
		// ELECTRIC COMPANY
		else if (currentPlayer.getLocation() == 12) {
			if (board.ownsDeed(7, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 7, -150);
			} else {
				utilityRent(currentPlayer, 7);
			}
		}
		// STATES AVENUE
		else if (currentPlayer.getLocation() == 13) {
			if (board.ownsDeed(8, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 8, -140);
			} else {
				payRent(currentPlayer, 10, 8);
			}
		}
		// VIRGINIA AVENUE
		else if (currentPlayer.getLocation() == 14) {
			if (board.ownsDeed(9, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 9, -160);
			} else {
				payRent(currentPlayer, 12, 9);
			}
		}
		// PENNSYLVANIA RAILROAD
		else if (currentPlayer.getLocation() == 15) {
			if (board.ownsDeed(10, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 10, -200);
			} else {
				railRoadRent(currentPlayer, 10);
			}
		}
		// ST. JAMES PLACE
		else if (currentPlayer.getLocation() == 16) {
			if (board.ownsDeed(11, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 11, -180);
			} else {
				payRent(currentPlayer, 14, 11);
			}
		}
		// COMMUNITY CHEST
		else if (currentPlayer.getLocation() == 17) {
			handleCommunityChestCard(currentPlayer);
		}
		// TENNESSE AVENUE
		else if (currentPlayer.getLocation() == 18) {
			if (board.ownsDeed(12, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 12, -180);
			} else {
				payRent(currentPlayer, 14, 12);
			}
		}
		// NEW YORK AVENUE
		else if (currentPlayer.getLocation() == 19) {
			if (board.ownsDeed(13, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 13, -200);
			} else {
				payRent(currentPlayer, 16, 13);
			}
		}
		// KENTUCKY AVENUE
		else if (currentPlayer.getLocation() == 21) {
			if (board.ownsDeed(14, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 14, -220);
			} else {
				payRent(currentPlayer, 18, 14);
			}
		}
		// CHANCE
		else if (currentPlayer.getLocation() == 22) {
			handleChanceCard(currentPlayer);
		}

		// INDIANA AVENUE
		else if (currentPlayer.getLocation() == 23) {
			if (board.ownsDeed(15, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 15, -220);
			} else {
				payRent(currentPlayer, 18, 15);
			}
		}
		// ILLINOIS AVENUE
		else if (currentPlayer.getLocation() == 24) {
			if (board.ownsDeed(16, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 16, -240);
			} else {
				payRent(currentPlayer, 20, 16);
			}
		}
		// B. & O. RAILROAD
		else if (currentPlayer.getLocation() == 25) {
			if (board.ownsDeed(17, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 17, -200);
			} else {
				railRoadRent(currentPlayer, 17);
			}
		}
		// ATLANTIC AVENUE
		else if (currentPlayer.getLocation() == 26) {
			if (board.ownsDeed(18, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 18, -260);
			} else {
				payRent(currentPlayer, 22, 18);
			}
		}
		// VENTNOR AVENUE
		else if (currentPlayer.getLocation() == 27) {
			if (board.ownsDeed(19, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 19, -260);
			} else {
				payRent(currentPlayer, 22, 19);
			}
		}
		// WATER WORKS
		else if (currentPlayer.getLocation() == 28) {
			if (board.ownsDeed(20, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 20, -150);
			} else {
				utilityRent(currentPlayer, 20);
			}
		}
		// MARVIN GARDENS
		else if (currentPlayer.getLocation() == 29) {
			if (board.ownsDeed(21, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 21, -280);
			} else {
				payRent(currentPlayer, 24, 21);
			}
		}
		// GO TO JAIL
		else if (currentPlayer.getLocation() == 30) {
			currentPlayer.setLocation(10);
			currentPlayer.isInJail = true;
		}

		// PACIFIC AVENUE
		else if (currentPlayer.getLocation() == 31) {
			if (board.ownsDeed(22, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 22, -300);
			} else {
				payRent(currentPlayer, 26, 22);
			}
		}
		// NORTH CAROLINA AVENUE
		else if (currentPlayer.getLocation() == 32) {
			if (board.ownsDeed(23, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 23, -300);
			} else {
				payRent(currentPlayer, 26, 23);
			}
		}
		// COMMUNITY CHEST
		else if (currentPlayer.getLocation() == 33) {
			handleChanceCard(currentPlayer);
		}
		// PENNSYLVANIA AVENUE
		else if (currentPlayer.getLocation() == 34) {
			if (board.ownsDeed(24, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 24, -320);
			} else {
				payRent(currentPlayer, 28, 24);
			}
		}
		// SHORT LINE
		else if (currentPlayer.getLocation() == 35) {
			if (board.ownsDeed(25, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 25, -200);
			} else {
				railRoadRent(currentPlayer, 25);
			}
		}
		// CHANCE
		else if (currentPlayer.getLocation() == 36) {
			handleChanceCard(currentPlayer);
		}
		// PARK PLACE
		else if (currentPlayer.getLocation() == 37) {
			if (board.ownsDeed(26, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 26, -350);
			} else {
				payRent(currentPlayer, 35, 26);
			}
		}
		// LUXURY TAX
		else if (currentPlayer.getLocation() == 38) {
			System.out.println("The bank took $100 off of your balance");
			currentPlayer.setBalance(-100);
		}

		// BOARDWALK
		else if (currentPlayer.getLocation() == 39) {
			if (board.ownsDeed(27, currentPlayer)) {
				propertyMenuSelection(currentPlayer, 27, -400);
			} else {
				payRent(currentPlayer, 50, 27);
			}
		}
	}

	private void printCardInfo(Card topCard) {
		System.out.print(topCard.getName() + topCard.getDesc());
	}

	private void handleIncomeTax(Player currentPlayer) throws IOException {
		int taxSelection = menu.payLuxuryTaxMenu();
		switch (taxSelection) {
		case 0:
			currentPlayer.setBalance(-200);
			break;
		case 1:
			int totalToPay = 0;
			for (card.Property cards : currentPlayer.propertiesOwned) {
				totalToPay += cards.getCost() * 0.1;
			}
			totalToPay += currentPlayer.getBalance() * 0.1;
			// TODO Calculate houses as well

			System.out.println("\n10% of your income is: " + totalToPay);
			currentPlayer.setBalance(-totalToPay);
			break;
		default:
			throw new IllegalArgumentException("Invalid selection" + taxSelection);
		}
	}

	private void landOnUtilityByChance(Player currentPlayer) throws IOException {
		int totalOwed = 0;
		int selection = menu.printPayRentMenu();
		if (selection == 0) {
			System.out.println("\nYou will now roll the dice to see how much you will have to pay rent.");
			int selection2 = menu.rollDiceMenu();
			if (selection2 == 0) {
				die.roll();
				whatYouRolled();
				for (Player playerOwner : players) {
					if (playerOwner.propertiesOwned.contains(board.deeds[7])
							|| playerOwner.propertiesOwned.contains(board.deeds[20])) {
						totalOwed = 10 * die.getTotal();
						System.out.println("Since you rolled " + die.getTotal() + ". You are paying $" + totalOwed);
						playerOwner.setBalance(totalOwed);
						currentPlayer.setBalance(-totalOwed);
					}
				}
			}
		}
	}

	private void handleChanceCard(Player currentPlayer) throws IOException {
		int firstCardPosistion = 0;
		Card topCard = board.chance.get(firstCardPosistion);
		printCardInfo(topCard);
		board.chance.remove(firstCardPosistion);
		switch (topCard.cardName) {
		case JAIL_FREE:

			if (currentPlayer.jailCardOwned[0] == null) {
				currentPlayer.jailCardOwned[0] = topCard;
				board.chance.remove(firstCardPosistion);
			} else {
				currentPlayer.jailCardOwned[1] = topCard;
				board.chance.remove(firstCardPosistion);
			}
			break;

		case MOVEMENT:
			board.chance.add(topCard);
			if (topCard.getId() == 4) {
				// Go back 3 spaces
				printCardInfo(topCard);
				currentPlayer.setLocation(currentPlayer.getLocation() - 3);
				landOnProperty(currentPlayer, currentPlayer.getLocation() - 3);
			} else if (topCard.getId() == 5 || topCard.getId() == 7) {
				// Go to nearest railroad
				if (currentPlayer.getLocation() == 7) {
					movePlayer(8, currentPlayer);
					landOnProperty(currentPlayer, 15);
				} else if (currentPlayer.getLocation() == 22) {
					movePlayer(3, currentPlayer);
					landOnProperty(currentPlayer, 25);
				} else if (currentPlayer.getLocation() == 36) {
					movePlayer(9, currentPlayer);
					landOnProperty(currentPlayer, 5);

				}
			} else if (topCard.getId() == 6) {
				// Go to nearest utility
				if (currentPlayer.getLocation() == 7) {
					movePlayer(5, currentPlayer);
					if (board.deeds[12].getOwner() != null) {
						landOnUtilityByChance(currentPlayer);
					} else {
						landOnProperty(currentPlayer, 12);
					}
				} else if (currentPlayer.getLocation() == 22) {
					movePlayer(6, currentPlayer);
					if (board.deeds[128].getOwner() != null) {
						landOnUtilityByChance(currentPlayer);
					} else {
						landOnProperty(currentPlayer, 28);
					}
				} else if (currentPlayer.getLocation() == 36) {
					movePlayer(16, currentPlayer);
					if (board.deeds[12].getOwner() != null) {
						landOnUtilityByChance(currentPlayer);
					} else {
						landOnProperty(currentPlayer, 12);
					}
				}
			}
			if (topCard.getId() == 8) {
				// Advance to GO
				if (currentPlayer.getLocation() == 7) {
					movePlayer(33, currentPlayer);
					landOnProperty(currentPlayer, 0);
				} else if (currentPlayer.getLocation() == 22) {
					movePlayer(18, currentPlayer);
					landOnProperty(currentPlayer, 0);
				} else if (currentPlayer.getLocation() == 36) {
					movePlayer(4, currentPlayer);
					landOnProperty(currentPlayer, 0);
				}
			}
			if (topCard.getId() == 9) {
				// Advance to Illinois avenue
				if (currentPlayer.getLocation() == 7) {
					movePlayer(17, currentPlayer);
					landOnProperty(currentPlayer, 24);
				} else if (currentPlayer.getLocation() == 22) {
					movePlayer(2, currentPlayer);
					landOnProperty(currentPlayer, 24);
				} else if (currentPlayer.getLocation() == 36) {
					movePlayer(28, currentPlayer);
					landOnProperty(currentPlayer, 24);
				}
			}
			if (topCard.getId() == 10) {
				// Take a trip to reading railroad
				if (currentPlayer.getLocation() == 7) {
					movePlayer(38, currentPlayer);
					landOnProperty(currentPlayer, 5);
				} else if (currentPlayer.getLocation() == 22) {
					movePlayer(23, currentPlayer);
					landOnProperty(currentPlayer, 5);
				} else if (currentPlayer.getLocation() == 36) {
					movePlayer(9, currentPlayer);
					landOnProperty(currentPlayer, 5);
				}
			}
			if (topCard.getId() == 11) {
				// Advance to St. Charles
				if (currentPlayer.getLocation() == 7) {
					movePlayer(4, currentPlayer);
					landOnProperty(currentPlayer, 11);
				} else if (currentPlayer.getLocation() == 22) {
					movePlayer(29, currentPlayer);
					landOnProperty(currentPlayer, 11);
				} else if (currentPlayer.getLocation() == 36) {
					movePlayer(15, currentPlayer);
					landOnProperty(currentPlayer, 11);
				}
			}
			if (topCard.getId() == 12) {
				// Go to jail
				currentPlayer.setLocation(10);
				landOnProperty(currentPlayer, 10);
			}
			if (topCard.getId() == 14) {
				// Advance to Boardwalk
				if (currentPlayer.getLocation() == 7) {
					movePlayer(32, currentPlayer);
					landOnProperty(currentPlayer, 39);
				} else if (currentPlayer.getLocation() == 22) {
					movePlayer(17, currentPlayer);
					landOnProperty(currentPlayer, 39);
				} else if (currentPlayer.getLocation() == 36) {
					landOnProperty(currentPlayer, 39);
				}
			}
			break;
		case PAY_BUILDING_TAX:
			board.chance.add(topCard);
			break;
		case PAY_MONEY:
			board.chance.add(topCard);
			if (topCard.getId() == 15) {
				currentPlayer.setBalance(-15);
			} else if (topCard.getId() == 16) {

				currentPlayer.setBalance(-50);
			} else if (topCard.getId() == 17) {
				currentPlayer.setBalance(-50);
			} else if (topCard.getId() == 18) {
				currentPlayer.setBalance(-100);
			}
			break;
		case PAY_OR_RECEIVE_PLAYERS:
			board.chance.add(topCard);
			if (topCard.getId() == 22) {
				int totalAmountGiven = 0;
				currentPlayer.setBalance(-50 * players.length);
				for (Player player : players) {
					player.setBalance(totalAmountGiven / players.length);
				}
			}
			break;
		case RECEIVE_MONEY:
			board.chance.add(topCard);
			if (topCard.getId() == 23) {
				currentPlayer.setBalance(150);
			}
			if (topCard.getId() == 24) {
				currentPlayer.setBalance(50);
			}
			break;
		default:
			break;
		}
	}

	public void handleCommunityChestCard(Player currentPlayer) throws IOException {
		Card topCard = board.communityChest.get(0);
		board.communityChest.remove(0);
		switch (topCard.cardName) {
		case JAIL_FREE:
			board.communityChest.add(topCard);
			if (currentPlayer.jailCardOwned[0] == null) {
				currentPlayer.jailCardOwned[0] = topCard;
				board.communityChest.remove(0);
			} else {
				currentPlayer.jailCardOwned[1] = topCard;
				board.communityChest.remove(0);
			}
			break;
		case MOVEMENT:
			board.communityChest.add(topCard);
			if (topCard.getId() == 3) {
				// Advance to GO
				printCardInfo(topCard);
				if (currentPlayer.getLocation() == 2) {
					movePlayer(38, currentPlayer);
					landOnProperty(currentPlayer, 0);
				} else if (currentPlayer.getLocation() == 17) {
					movePlayer(23, currentPlayer);
					landOnProperty(currentPlayer, 0);
				} else if (currentPlayer.getLocation() == 33) {
					movePlayer(7, currentPlayer);
					landOnProperty(currentPlayer, 0);
				}
			}
			if (topCard.getId() == 13) {
				// Go to jail
				printCardInfo(topCard);
				currentPlayer.setLocation(10);
				landOnProperty(currentPlayer, 10);
			}
			break;
		case PAY_BUILDING_TAX:
			board.communityChest.add(topCard);
			break;
		case PAY_MONEY:
			board.communityChest.add(topCard);
			if (topCard.getId() == 16) {
				currentPlayer.setBalance(-50);
			}
			if (topCard.getId() == 17) {
				currentPlayer.setBalance(-50);
			}
			if (topCard.getId() == 18) {
				currentPlayer.setBalance(-100);
			}
			break;
		case PAY_OR_RECEIVE_PLAYERS:
			board.communityChest.add(topCard);
			if (topCard.getId() == 21) {
				int totalAmountCollected = 0;
				for (Player player : players) {
					player.setBalance(-10);
					totalAmountCollected += 10;
				}

				currentPlayer.setBalance(totalAmountCollected);
			}
			break;
		case RECEIVE_MONEY:
			board.communityChest.add(topCard);
			if (topCard.getId() == 25) {
				currentPlayer.setBalance(200);
			}
			if (topCard.getId() == 26) {
				currentPlayer.setBalance(100);
			}
			if (topCard.getId() == 27) {
				currentPlayer.setBalance(20);
			}
			if (topCard.getId() == 28) {
				currentPlayer.setBalance(25);
			}
			if (topCard.getId() == 29) {
				currentPlayer.setBalance(10);
			}
			if (topCard.getId() == 30) {
				currentPlayer.setBalance(50);
			}
			if (topCard.getId() == 31) {
				currentPlayer.setBalance(100);
			}
			if (topCard.getId() == 32) {
				currentPlayer.setBalance(100);
			}
			break;
		default:
			break;
		}

	}

	private void utilityRent(Player currentPlayer, int deedLocation) throws IOException {
		int totalOwed = 0;
		int selection = menu.printPayRentMenu();
		if (selection == 0) {
			System.out.println("\nYou will now roll the dice to see how much you will have to pay for rent.");
			int selection2 = menu.rollDiceMenu();
			if (selection2 == 0) {
				die.roll();
				whatYouRolled();
				for (Player playerOwner : players) {
					if (playerOwner.propertiesOwned.contains(board.deeds[deedLocation])) {
						if (playerOwner.propertiesOwned.contains(board.deeds[7])
								&& playerOwner.propertiesOwned.contains(board.deeds[20])) {
							totalOwed = 10 * die.getTotal();
							System.out.println("Since you rolled " + die.getTotal() + ", and " + playerOwner.getName()
									+ " owns 2 property\n" + "You are paying $" + totalOwed);
						} else if (playerOwner.propertiesOwned.contains(board.deeds[7])
								|| playerOwner.propertiesOwned.contains(board.deeds[20])) {
							totalOwed = 4 * die.getTotal();
							System.out.println("Since you rolled " + die.getTotal() + ", and " + playerOwner.getName()
									+ " owns 1 property\n" + "You are paying $" + totalOwed);
						}
						playerOwner.setBalance(totalOwed);
						currentPlayer.setBalance(-totalOwed);
					}
				}
			}

		}
	}

	private void railRoadRent(Player currentPlayer, int deedLocation) throws IOException {
		int totalOwed = 0;
		int selection = menu.printPayRentMenu();
		if (selection == 0) {
			for (Player playerOwner : players) {
				if (playerOwner.propertiesOwned.contains(board.deeds[deedLocation])) {
					int counter = 0;
					for (card.Property titledeed : playerOwner.propertiesOwned) {
						if (titledeed instanceof RailRoad) {
							counter++;
						}
					}
					switch (counter) {
					case 1:
						totalOwed = 25;
						break;
					case 2:
						totalOwed = 50;
						break;
					case 3:
						totalOwed = 100;
						break;
					case 4:
						totalOwed = 200;
						break;
					default:
						break;
					}
					playerOwner.setBalance(totalOwed);
					currentPlayer.setBalance(-totalOwed);
				}
			}
		}

	}

	private void propertyMenuSelection(Player currentPlayer, int location, int cost) throws IOException {
		int selection = menu.printBuyPropertiesMenu();
		switch (selection) {
		case 0:
			System.out.println("\nYou now own this deed!");
			currentPlayer.propertiesOwned.add(board.deeds[location]);
			currentPlayer.setBalance(cost);
			break;
		case 1:
			System.out.println("\n\nSince you decided not to buy it, the bank will auction this property.");
			// HANDLE AUCTIONING
			int costOfAuction = 10;
			Player[] inAuction = players;
			int totalPlayersInAuction = inAuction.length;
			boolean playerBoughtProperty = false;
			while (!playerBoughtProperty) {
				for (Player AuctionPlayer : inAuction) {
					System.out.println("\nCurrent Auction Price: " + costOfAuction);
					if (AuctionPlayer == null) {
						// skipping auctioned player
						continue;
					}
					if (totalPlayersInAuction == 1) {
						// you win pay the price.
						AuctionPlayer.setBalance(-1 * costOfAuction);
						playerBoughtProperty = true;
						AuctionPlayer.propertiesOwned.add(board.deeds[location]);
						System.out.println("\n*******************************************************************\n"
								+ "Congratulations " + AuctionPlayer.getName() + "! You know own this property\n"
								+ "*******************************************************************\n");
						break;
					}
					System.out.println("It is your turn, " + AuctionPlayer.getName() + "!");
					int chooseToLeave = ConsoleUI
							.promptForMenuSelection(new String[] { "Leave The Auction", "Increment Value" });
					if (chooseToLeave == 0) {
						// left the auction
						AuctionPlayer = null;
						totalPlayersInAuction--;
					} else {
						// you must auction to the death
						int amountToIncreaseBy = ConsoleUI.promptForInt(
								"Enter amount you want to increase the bid price by: ", 1, Integer.MAX_VALUE);
						costOfAuction += amountToIncreaseBy;
					}
				}
			}

			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @param currentPlayer player who land on the property already owned
	 * @param regularRent   how much will rent be
	 * @param deedLocation  what is the location in the array
	 */
	private void payRent(Player currentPlayer, int regularRent, int deedLocation) throws IOException {
		int selection = menu.printPayRentMenu();
		if (selection == 0) {
			// TODO CHECK IF OWNER HAS ALL GROUP PROPERTIES
			// DOUBLE RENT
			// CHECK IF THERE IS HOUSES/HOTELS
			// ELSE
			currentPlayer.setBalance(-regularRent);

			for (Player player : players) {
				if (player.propertiesOwned.contains(board.deeds[deedLocation])) {
					player.setBalance(regularRent);
					System.out.println("\n" + player.getName() + " says thanks for the money!");
				}
			}
		}
	}

	/**
	 * Method that will let finish your turn (does not have a roll dice option)
	 * 
	 * @param currentPlayer who's turn is it
	 */
	private void turnAfterRoll(Player currentPlayer) throws IOException {
		boolean isYourTurnAfterRoll = true;
		while (isYourTurnAfterRoll) {
			board.printBoard(currentPlayer);
			int action = menu.printMenuAfterRoll();
			switch (action) {
			case 0:
				showBalance(currentPlayer);
				break;
			case 1:
				showProperties(currentPlayer);
				break;
			case 2:
				sell(currentPlayer);
				break;
			case 3:
				buy(currentPlayer);
				break;
			case 4:
				isYourTurnAfterRoll = false;
				break;
			default:
				throw new IllegalArgumentException("Invalid action " + action);
			}
		}
	}

	private void sell(Player currentPlayer) throws IOException {
		int action = menu.printSellBuyMainMenu();
		switch (action) {
		case 0:
			if (currentPlayer.getPropertiesOwned().size() == 0) {
				System.out.println(
						"\nSorry you don't have any properties to sell, check back later after you had purchase something");
			} else {
				showPropertyNameFormated(currentPlayer);
				int selection = ConsoleUI.promptForInt("\nSelect the index of the card you want so sell", 0,
						currentPlayer.getPropertiesOwned().size());
				Property temp = currentPlayer.propertiesOwned.get(selection);
				int price = ConsoleUI.promptForInt("Enter the price you want to sell it for", 0, Integer.MAX_VALUE);
				String playerName = ConsoleUI
						.promptForInput("Enter the name of the player you want to sell the property", false);
				for (Player playerBuyer : players) {
					if (playerBuyer.getName().toLowerCase().equals(playerName.toLowerCase())) {
						System.out.println("Hey " + playerBuyer.getName() + ", " + currentPlayer.getName()
								+ " wants to sell " + temp.getPropertyName() + " for $" + price);
						int playerBuyerSelection = menu.printAcceptMenu();
						switch (playerBuyerSelection) {
						case 0:
							playerBuyer.propertiesOwned.add(temp);
							playerBuyer.setBalance(-price);
							currentPlayer.propertiesOwned.remove(selection);
							currentPlayer.setBalance(price);
							System.out.println(currentPlayer.getName() + " says thank you!");
							break;
						case 1:
							System.out.println("Sorry " + currentPlayer.getName() + ", " + playerBuyer.getName()
									+ " said that will not buy it for that much.");
							break;
						default:
							break;
						}
					}
				}
			}

			break;
		case 1:
			if (currentPlayer.jailCardOwned[1] == null) {
				System.out.println("\nYou don't have any \"Get Out Of Jail\" cards");
			} else if (currentPlayer.jailCardOwned[0] == null && currentPlayer.jailCardOwned[1] == null) {
				System.out.println("\nYou don't have any \"Get Out Of Jail\" cards");
			} else {

				int cardLocation = 0;
				Card temp2 = null;
				if (currentPlayer.jailCardOwned[1] != null) {
					cardLocation = 1;
					temp2 = currentPlayer.jailCardOwned[1];
				} else if (currentPlayer.jailCardOwned[0] != null) {
					temp2 = currentPlayer.jailCardOwned[0];
				}

				int price2 = ConsoleUI.promptForInt("Enter the price you want to sell it for", 0, Integer.MAX_VALUE);
				String playerName2 = ConsoleUI
						.promptForInput("Enter the name of the player you want to sell the property", false);
				for (Player playerBuyer : players) {
					if (playerBuyer.getName().toLowerCase().equals(playerName2.toLowerCase())) {
						System.out.println("Hey " + playerBuyer.getName() + ", " + currentPlayer.getName()
								+ " wants to sell a \"Get Out Of Jail Card\"" + " for $" + price2);
						int playerBuyerSelection = menu.printAcceptMenu();
						switch (playerBuyerSelection) {
						case 0:
							if (playerBuyer.jailCardOwned[0] == null) {
								playerBuyer.jailCardOwned[0] = temp2;
								System.out.println("We added the card to the location 0");
							} else if (playerBuyer.jailCardOwned[1] == null) {
								playerBuyer.jailCardOwned[1] = temp2;
								System.out.println("We added the card to the location 1");
							}
							playerBuyer.setBalance(-price2);

							if (cardLocation == 1) {
								currentPlayer.jailCardOwned[cardLocation] = null;
							} else if (cardLocation == 0) {
								currentPlayer.jailCardOwned[cardLocation] = null;
							}
							currentPlayer.setBalance(price2);
							System.out.println(currentPlayer.getName() + " says thank you!");

							break;
						case 1:
							System.out.println("Sorry " + currentPlayer.getName() + ", " + playerBuyer.getName()
									+ " said that will not buy it for that much.");
							break;
						default:
							break;
						}
					}
				}
			}
			break;
		case 2:
			break;
		default:
			throw new IllegalArgumentException("Invalid action " + action);
		}
	}

	private void showPropertyNameFormated(Player currentPlayer) {
		System.out.print("\nThe properties you own are:\n");
		for (int i = 0; i < currentPlayer.getPropertiesOwned().size(); i++) {
			if (i == currentPlayer.getPropertiesOwned().size() - 1) {
				System.out.print("[" + i + "]" + currentPlayer.propertiesOwned.get(i).getPropertyName() + "\n");
			}
		}
	}

	private void buy(Player currentPlayer) throws IOException {

		int action = menu.printSellBuyMainMenu();
		switch (action) {
		case 0:
			String playerName = ConsoleUI.promptForInput("Enter the name of the player you want to see the properties",
					false);
			for (Player playerSeller : players) {
				if (playerSeller.getName().toLowerCase().equals(playerName.toLowerCase())) {

					if (playerSeller.propertiesOwned.isEmpty()) {
						System.out.println("\n" + playerSeller.getName()
								+ " does not have any properties. Check back with him later.");
					} else {

						showPropertyNamesOtherPlayer(playerSeller);
						int selection = ConsoleUI.promptForInt("\nSelect the index of the card you want buy", 0,
								playerSeller.getPropertiesOwned().size());
						Property temp = playerSeller.propertiesOwned.get(selection);
						int price = ConsoleUI.promptForInt("Enter the price you want to sell it for", 0,
								Integer.MAX_VALUE);
						System.out.println("Hey " + playerSeller.getName() + ", " + currentPlayer.getName()
								+ " wants to buy " + temp.getPropertyName() + " for $" + price);
						int playerSellerSelection = menu.printAcceptMenu();
						switch (playerSellerSelection) {
						case 0:
							playerSeller.propertiesOwned.remove(selection);
							playerSeller.setBalance(price);
							currentPlayer.propertiesOwned.add(temp);
							currentPlayer.setBalance(-price);
							System.out.println("Transaction completed");
							break;
						case 1:
							System.out.println("Sorry " + currentPlayer.getName() + ", " + playerSeller.getName()
									+ " said that will not buy it for that much.");
							break;
						default:
							break;
						}
					}
				}
			}

			break;
		case 1:
			String playerName2 = ConsoleUI.promptForInput(
					"Enter the name of the player you want to see if he has \"Get Out Of Jail\" cards ", false);
			for (Player playerSeller : players) {
				if (playerSeller.getName().toLowerCase().equals(playerName2.toLowerCase())) {

					if (playerSeller.jailCardOwned[1] == null) {
						System.out.println(
								"\n" + playerSeller.getName() + " does not have any \"Get Out Of Jail\" cards");
					} else if (playerSeller.jailCardOwned[0] == null && playerSeller.jailCardOwned[1] == null) {
						System.out.println(
								"\n" + playerSeller.getName() + " does not have any \"Get Out Of Jail\" cards");
					} else {

						int howMany = checkForJailCard(playerSeller);

						int selection = ConsoleUI.promptForInt("\nSelect the index of the card you want buy", 0,
								howMany);

						Card temp2 = playerSeller.jailCardOwned[selection];

						int price2 = ConsoleUI.promptForInt("Enter the price you want to buy it for", 0,
								Integer.MAX_VALUE);

						// Switch perspective
						System.out.println("Hey " + playerSeller.getName() + ", " + currentPlayer.getName()
								+ " wants to buy " + howMany + " card for $" + price2);
						int playerSellerSelection = menu.printAcceptMenu();
						switch (playerSellerSelection) {
						case 0:

							int cardLocation = 0;
							if (playerSeller.jailCardOwned[1] != null) {
								cardLocation = 1;
								temp2 = currentPlayer.jailCardOwned[1];
							} else if (currentPlayer.jailCardOwned[0] != null) {
								temp2 = currentPlayer.jailCardOwned[0];
							}

							if (currentPlayer.jailCardOwned[0] == null) {
								currentPlayer.jailCardOwned[0] = temp2;
								System.out.println("We added the card to the location 0");
							} else if (currentPlayer.jailCardOwned[1] == null) {
								currentPlayer.jailCardOwned[1] = temp2;
								System.out.println("We added the card to the location 1");
							}

							playerSeller.setBalance(price2);

							// Take card out of player seller
							if (cardLocation == 1) {
								playerSeller.jailCardOwned[cardLocation] = null;
								System.out.println("removed card from location 1");
							} else if (cardLocation == 0) {
								playerSeller.jailCardOwned[cardLocation] = null;

								System.out.println("removed card from location 0");
							}
							currentPlayer.setBalance(-price2);
							System.out.println(currentPlayer.getName() + " says thank you!");
							break;
						case 1:
							System.out.println("Sorry " + currentPlayer.getName() + ", " + playerSeller.getName()
									+ " said that will not buy it for that much.");
							break;
						default:
							break;

						}
					}
				}
			}
			break;
		case 2:
			break;
		default:
			throw new IllegalArgumentException("Invalid action " + action);
		}
	}

	private void showPropertyNamesOtherPlayer(Player otherPlayer) {
		System.out.print("\nThe properties that " + otherPlayer.getName() + " owns are:\n");
		for (int i = 0; i < otherPlayer.getPropertiesOwned().size(); i++) {
			if (i == otherPlayer.getPropertiesOwned().size()) {
				System.out.println("[" + i + "]\t" + otherPlayer.propertiesOwned.get(i).getPropertyName() + "\n");
			}
		}
	}

	private int checkForJailCard(Player playerSeller) {
		int howMany = 0;
		System.out.print(playerSeller.getName() + " has ");
		for (int i = 0; i < playerSeller.jailCardOwned.length; i++) {
			if (i == playerSeller.jailCardOwned.length - 1) {
				howMany++;
				System.out.print(howMany + " \"Get Out Of Jail\" card");
			}
		}
		return howMany;
	}

	/**
	 * 
	 * Method to print what the balance is.
	 * 
	 * @param currentPlayer who's turn is it.
	 */
	private void showBalance(Player currentPlayer) {
		System.out.println("\nYour balance is: " + currentPlayer.getBalance());
	}

	/**
	 * Method to print/show the properties the current player owns.
	 * @param currentPlayer who's turn is it
	 */
	private void showProperties(Player currentPlayer) {
		if (currentPlayer.getPropertiesOwned().isEmpty()) {
			System.out.println("\n\nSorry, you don't own any properties.\nKeep playing to see if you get better luck!");
		} else {
			System.out.print("\nThe properties you own are:\n");

			for (int i = 0; i < currentPlayer.getPropertiesOwned().size(); i++) {

				if (currentPlayer.getPropertiesOwned().contains(board.deeds[2])
						|| currentPlayer.getPropertiesOwned().contains(board.deeds[10])
						|| currentPlayer.getPropertiesOwned().contains(board.deeds[17])
						|| currentPlayer.getPropertiesOwned().contains(board.deeds[25])) {

					System.out.print("\n[" + i + "] " + currentPlayer.propertiesOwned.get(i).getPropertyName()
							+ " | Rent: 1 - $25 || 2 - $50 || 3 - $100 || 4 - $200 ");
				} else if(currentPlayer.getPropertiesOwned().contains(board.deeds[7]) 
						|| currentPlayer.getPropertiesOwned().contains(board.deeds[20])) {
					System.out.print("\n[" + i + "] " + currentPlayer.propertiesOwned.get(i).getPropertyName()
							+ " | Rent: 1 - 4 time what the dice rolled || 2 - 10 times what the dice rolled");
				} 
				
				else {
					System.out.print("[" + i + "] " + currentPlayer.propertiesOwned.get(i).getPropertyName()
							+ " | Rent: " + currentPlayer.getPropertiesOwned().get(i).getRent() + " | Buy House: $"
							+ currentPlayer.propertiesOwned.get(i).getBuildingCost() + "\n");
				}

			}
		}
	}

	// TODO Currently Working on this feature
	private void buyHouse(Player currentPlayer) throws IOException {
		ArrayList<String> propertyMonopolies = new ArrayList<String>();
		int redColorTotal = 0;
		int blueColorTotal = 0;
		int brownColorTotal = 0;
		int lightBlueColorTotal = 0;
		int pinkColorTotal = 0;
		int orangeColorTotal = 0;
		int yellowColorTotal = 0;
		int greenColorTotal = 0;
		for (Property card : currentPlayer.propertiesOwned) {
			if (card instanceof TitleDeed) {
				TitleDeed newCard = (TitleDeed) card;
				if (newCard.color == TitleColor.RED) {
					redColorTotal++;
				} else if (newCard.color == TitleColor.BLUE) {
					blueColorTotal++;
				} else if (newCard.color == TitleColor.BROWN) {
					brownColorTotal++;
				} else if (newCard.color == TitleColor.LIGHTBLUE) {
					lightBlueColorTotal++;
				} else if (newCard.color == TitleColor.PINK) {
					pinkColorTotal++;
				} else if (newCard.color == TitleColor.ORANGE) {
					orangeColorTotal++;
				} else if (newCard.color == TitleColor.YELLOW) {
					yellowColorTotal++;
				} else if (newCard.color == TitleColor.GREEN) {
					greenColorTotal++;
				}
			}
		}
		if (redColorTotal == 3) {
			propertyMonopolies.add("red");
		}
		if (blueColorTotal == 2) {
			propertyMonopolies.add("blue");
		}
		if (brownColorTotal == 2) {
			propertyMonopolies.add("brown");
		}
		if (lightBlueColorTotal == 3) {
			propertyMonopolies.add("light blue");
		}
		if (pinkColorTotal == 3) {
			propertyMonopolies.add("pink");
		}
		if (orangeColorTotal == 3) {
			propertyMonopolies.add("orange");
		}
		if (yellowColorTotal == 3) {
			propertyMonopolies.add("yellow");
		}
		if (greenColorTotal == 3) {
			propertyMonopolies.add("green");
		}
		if (propertyMonopolies.size() == 0) {
			System.out.println("You ain't got shit,,,, dawg");
			return;
		}
		String[] listOfMonopolies = (String[]) propertyMonopolies.toArray();
		int menuChoiceForColor = ConsoleUI.promptForMenuSelection(listOfMonopolies);
		String colorValue = listOfMonopolies[menuChoiceForColor];
		int amountOfHouses = ConsoleUI.promptForInt("How many houses do you want to buy(5 for a hotel)", 1, 5);
		switch (colorValue) {
		case "red":
			currentPlayer.setBalance(amountOfHouses * -150);

//			for(int i = 0; i < amountOfHouses; i++) {
//				
//			for(Property deed: currentPlayer.propertiesOwned) {
//				if(deed instanceof TitleDeed && TitleColor.RED == ((TitleDeed) deed).color) {
//					if(((TitleDeed) deed).totalBuildings == 1) {
//						
//					}
//					((TitleDeed) deed).totalBuildings += 1;
//					
//				}
//				else {
//					continue;
//				}
//			}
//			
//			}

			break;
		case "blue":
			currentPlayer.setBalance(amountOfHouses * -200);
			break;
		case "brown":
			currentPlayer.setBalance(amountOfHouses * -50);
			break;
		case "light blue":
			currentPlayer.setBalance(amountOfHouses * -50);
			break;
		case "pink":
			currentPlayer.setBalance(amountOfHouses * -100);
			break;
		case "orange":
			currentPlayer.setBalance(amountOfHouses * -100);
			break;
		case "yellow":
			currentPlayer.setBalance(amountOfHouses * -150);
			break;
		case "green":
			currentPlayer.setBalance(amountOfHouses * -200);
			break;
		default:
			break;
		}

	}

	/**
	 * Method that will move the player base on the total number they rolled
	 * 
	 * @param num the total number that the dice got
	 * @param p   the player who's turn is it
	 */
	private void movePlayer(int totalDie, Player currentPlayer) {
		while (totalDie > 0) {
			totalDie--;
			currentPlayer.addLocation(1);
			if (currentPlayer.getLocation() == 40) {
				currentPlayer.setLocation(0);
			}
			if (currentPlayer.getLocation() == 0) {
				currentPlayer.setBalance(200);
			}

		}
	}

	/**
	 * Method that will turn on the isInJail flag of a player and update location of
	 * the player if is sent to jail.
	 * 
	 * @param currentPlayer
	 * @param jailLocation
	 */
	private void sendToJail(Player currentPlayer, int jailLocation) {
		currentPlayer.setLocation(jailLocation);
		currentPlayer.isInJail(true);
	}

	/**
	 * Set bankruptcy status as On
	 * 
	 * @param currentPlayer
	 */
	private void bankruptcy(Player currentPlayer) {
		currentPlayer.bankrupt = true;
	}

	// TODO UNDER CONSTRUCTION - PLEASE ADD SOME CODE HERE
	private void speedDieRules() {
		System.out.println("Please read rules inside box.");
	}
}
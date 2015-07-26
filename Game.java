import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

class Game implements Serializable {

	private final transient static long serialVersionUID = 9116192370304142140L;
	public  final transient static String LINE_BREAK = "------------------------------------------------";
	private final transient static int LOSE = -1;
	private final transient static int PUSH = 0;
	private final transient static int WIN = 1;
	private final transient static int BLACKJACK = 2;
	private final transient static Scanner scanner = new Scanner(System.in, "UTF-8");

	private final Deck deck;
	private final Player player;
	private final Dealer dealer;
	private transient Hand playerHand = null;
	private transient Hand dealerHand = null;

	private transient long startTime;
	private transient boolean prompted = false;

	private Game() {
		deck = new Deck();
		player = new Player();
		dealer = new Dealer();
	}

	public static void start() {
		Game game;
		System.out.println(LINE_BREAK);
		System.out.println("Do you want to load a previously saved game?");
		if (promptYesNo()) {
			System.out.print("Enter the name: ");
			String fileName = scanner.next();
			game = loadGame(fileName);
			System.out.println(LINE_BREAK);
		}
		else {
			System.out.println("Starting new game.");
			System.out.println(LINE_BREAK);
			game = new Game();
		}

		game.startTime = System.currentTimeMillis();
		boolean gameOver = game.startGame();

		System.out.println(LINE_BREAK);
		System.out.println("Do you want to save your game?");
		if (promptYesNo()) {
			System.out.print("Enter a name: ");
			saveGame(game, scanner.next());
		}
		if (!gameOver) {
			start();
		}
	}

	private static Game loadGame(String fileName) {
		Game game = null;
		fileName += ".game";
		try {
			FileInputStream fileInputStream = new FileInputStream(fileName);
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			game = (Game) objectInputStream.readObject();
		} catch (FileNotFoundException e) {
			System.out.println("Game not found. Starting new game.");
		}catch (IOException e) {
			System.out.println("Error loading game. Starting new game.");
		} catch (ClassNotFoundException e) {
			System.out.println("Error loading game. Starting new game.");
		}

		if (game == null) {
			game = new Game();
		}
		else {
			System.out.println("Game successfully loaded.");
		}
		return game;
	}

	private static void saveGame(Game game, String fileName) {
		fileName += ".game";
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(game);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean startGame() {
		if (player.canPlay()) {
			deal();
			greeting();
			if (!checkBlackJacks()) {
				play();
			}
			player.settle();
			if (promptNewGame()) {
				long playTime = System.currentTimeMillis() - startTime;
				if (playTime >= TimeUnit.MINUTES.toMillis(30) && !prompted) {
					prompted = true;
					System.out.println("You've been playing for " + 30 + " minutes.");
					System.out.println("Do you want to quit?");
					return promptYesNo();
				}
				else {
					startGame();
				}
			}
			else {
				System.out.println(LINE_BREAK);
				System.out.println("You walk away with " + (int) player.getWallet() + " credit(s).");
				System.out.println("Your high wallet value was " + player.getHighMark() + " credit(s).");
			}
		}
		else {
			System.out.println("You gambled all your money away.");
			System.out.println("Your high wallet value was " + player.getHighMark() + " credit(s).");
		}
		return true;
	}

	private void deal() {
		playerHand = new Hand(deck);
		player.newGame(deck);
		player.addHand(playerHand);
		dealerHand = new Hand(deck);
		dealer.setHand(dealerHand);
	}

	private void greeting() {
		System.out.println("You have " + player.getWallet() + " credit(s).");

		placeBet((playerHand));

		System.out.println("You now have " + player.getWallet() + " credit(s) remaining.");
		System.out.println(LINE_BREAK);
		System.out.println(dealer.toString());
		System.out.println(player.toString());
	}

	private void placeBet(Hand hand) {
		System.out.println("How much do you want to bet?");
		boolean validBet = false;

		while (!validBet) {
			try {
				double input = scanner.nextDouble();
				hand.setBet(input);
				validBet = player.decreaseWallet(input);
				if (input < 0) {
					validBet = false;
					System.out.println("You can't bet negative credits.");
				}
				else if (input == 0) {
					validBet = false;
					System.out.println("You have to bet something.");
				}
				else if (!validBet) {
					System.out.println("You don't have that many credits.");
				}
			} catch (InputMismatchException e) {
				System.out.println("Enter a valid number.");
				scanner.next();
			}
		}
	}

	private boolean checkBlackJacks(){
		boolean playerBlackJack = playerHand.checkBlackJack();
		boolean dealerBlackJack = dealerHand.checkBlackJack();

		if (playerBlackJack && dealerBlackJack) {
			playerHand.setOutcome(PUSH);
			System.out.println("Blackjacks, push");
			return true;
		}
		else if (playerBlackJack) {
			playerHand.setOutcome(BLACKJACK);
			System.out.println("Blackjack, you win");
			return true;
		}
		else if (dealerBlackJack) {
			playerHand.setOutcome(LOSE);
			System.out.println("Dealer blackjack, you lose");
			return true;
		}
		return false;
	}

	private void play() {
		Hand otherHand = playHand(playerHand);
		dealer.playHand(deck);

		if (otherHand != null) {
			player.addHand(otherHand);
			player.decreaseWallet(otherHand.getBet());
			printMessage(playerHand, otherHand, dealerHand);
		}
		else {
			printMessage(playerHand, dealerHand);
		}
	}

	private Hand playHand(Hand hand) {
		Hand otherHand = null;

		if (hand.pairOfAces()) {
			otherHand = promptSplitAces(hand);
			System.out.println(LINE_BREAK);
		}
		else {
			if (hand.canSplit()) {
				otherHand = promptSplit(hand);
			}
			System.out.println(LINE_BREAK);
			if (otherHand != null) {
				System.out.println("Playing left hand:");
			}
			promptHit(hand);
			if (otherHand != null) {
				System.out.println("Playing right hand");
				promptHit(otherHand);
			}
		}
		return otherHand;
	}

	private Hand promptSplitAces(Hand hand) {
		Hand splitHand = null;
		System.out.println("Your hand: " + hand.toString(false));
		System.out.println("You have a pair of Aces. Do you want to split? (y/n)");
		if (promptYesNo()) {
			splitHand = new Hand(deck, hand.removeCard());
			hand.addNextCard(deck);

			System.out.println("Your left hand: " + hand.toString(false));
			System.out.println("Your right hand: " + splitHand.toString(false));
		}
		return splitHand;
	}

	private Hand promptSplit(Hand hand) {
		Hand splitHand = null;
		System.out.println("You have a pair. Do you want to split? (y/n)");

		if (promptYesNo()) {
			splitHand = new Hand(deck, hand.removeCard());
			splitHand.setBet(hand.getBet());
			hand.addNextCard(deck);

			System.out.println("Your left hand: " + hand.toString(false));
			System.out.println("Your right hand: " + splitHand.toString(false));
		}
		return splitHand;
	}

	private static boolean promptYesNo() {
		boolean validInput = false;
		boolean booleanInput = false;
		String input;

		while (!validInput) {
			input = scanner.next();
			if (input.equals("y")) {
				validInput = true;
				booleanInput = true;
			}
			else if (input.equals("n")) {
				validInput = true;
				booleanInput = false;
			}
			else {
				System.out.println("Enter either 'y' or 'n'.");
			}
		}
		return booleanInput;
	}

	private void promptHit(Hand hand) {
		boolean stand = false;
		boolean firstMove = true;

		while (!stand && !hand.getBust()) {
			System.out.println("Your hand: " + hand.toString(false));
			if (firstMove) {
				System.out.println("Do you want to hit, stand, or double down? (h/s/d)");
				char playerMove = promptFirstMove();
				if (playerMove == 'h') {
					hand.addNextCard(deck);
				}
				else if (playerMove == 's') {
					stand = true;
				}
				else if (playerMove == 'd') {
					stand = true;
					hand.addNextCard(deck);
					player.decreaseWallet(hand.getBet());
					hand.doubleDown();
					System.out.println("You now have " + player.getWallet() + " credit(s) remaining.");
					System.out.println("Your hand: " + hand.toString(false));
				}
				firstMove = false;
			}
			else {
				System.out.println("Do you want to hit or stand? (h/s)");
				char playerMove = promptMove();
				if (playerMove == 'h') {
					hand.addNextCard(deck);
				}
				else if (playerMove == 's') {
					stand = true;
				}
			}

			System.out.println(LINE_BREAK);
		}
		if (hand.getBust()) {
			System.out.println("Your hand: " + hand.toString(false));
			System.out.println("Hand busts.");
			System.out.println(LINE_BREAK);
		}
	}

	private char promptFirstMove() {
		boolean validInput = false;
		String input = "";
		char charInput;

		while (!validInput) {
			input = scanner.next();
			if (input.equals("h") || input.equals("s")) {
				validInput = true;
			}
			else if (input.equals("d")) {
				if (player.canDoubleDown(playerHand.getBet())) {
					validInput = true;
				}
				else {
					System.out.println("You don't have enough credit to double down.");
				}
			}
			else {
				System.out.println("Enter either 'h' or 's' or 'd'.");
			}
		}
		charInput = input.charAt(0);
		return charInput;
	}

	private char promptMove() {
		boolean validInput = false;
		String input = "";
		char charInput;

		while (!validInput) {
			input = scanner.next();
			if (input.equals("h") || input.equals("s")) {
				validInput = true;
			}
			else {
				System.out.println("Enter either 'h' or 's'.");
			}
		}
		charInput = input.charAt(0);
		return charInput;
	}

	private boolean promptNewGame() {
		System.out.println("You have " + player.getWallet() + " credit(s).");
		System.out.println(LINE_BREAK);
		System.out.println("Do you want to play another hand? (y/n)");
		if (promptYesNo()) {
			System.out.println(LINE_BREAK);
			return true;
		}
		return false;
	}

	private void printMessage(Hand playerHand, Hand dealerHand) {
		System.out.println("RESULTS");
		System.out.println("Your hand: " + playerHand.toString(false));
		System.out.println("Dealer's hand: " + dealerHand.toString(false));

		String message = "";
		if (playerHand.getBust()) {
			message = "You bust. You lose " + playerHand.getBet() + " credit(s).";
			playerHand.setOutcome(LOSE);
		}
		else if (dealerHand.getBust()) {
			message = "Dealer busts. You win " + playerHand.getBet() + " credit(s).";
			playerHand.setOutcome(WIN);
		}
		else {
			int compare = playerHand.compare(dealerHand);
			if (compare == LOSE) {
				message = "You lose. You lose " + playerHand.getBet() + " credit(s).";
			}
			else if (compare == WIN) {
				message = "You win. You win " + playerHand.getBet() + " credit(s).";
			}
			else if (compare == PUSH) {
				message = "It's a push. " + playerHand.getBet() + " credit(s) returned.";
			}
			playerHand.setOutcome(compare);
		}
		System.out.println(message);
	}

	private void printMessage(Hand playerHand, Hand splitHand, Hand dealerHand) {
		System.out.println("RESULTS");
		System.out.println("Your left hand: " + playerHand.toString(false));
		System.out.println("Your right hand: " + splitHand.toString(false));
		System.out.println("Dealer's hand: " + dealerHand.toString(false));

		String message = "";
		if (playerHand.getBust()) {
			message = "Your left hand busts.";
			playerHand.setOutcome(LOSE);
		}
		else if (dealerHand.getBust()) {
			playerHand.setOutcome(WIN);
			message = "Dealer busts. Your left hand wins.";
		}
		else {
			int compare = playerHand.compare(dealerHand);
			if (compare == LOSE) {
				message = "Your left hand loses.";
			}
			else if (compare == WIN) {
				message = "Your left hand wins.";
			}
			else if (compare == PUSH) {
				message = "Your left hand pushes.";
			}
			playerHand.setOutcome(compare);
		}
		System.out.println(message);

		if (splitHand.getBust()) {
			splitHand.setOutcome(LOSE);
			message = "Your right hand busts.";
		}
		else if (dealerHand.getBust()) {
			splitHand.setOutcome(WIN);
			message = "Dealer busts. Your right hand wins.";
		}
		else {
			int compare = splitHand.compare(dealerHand);
			if (compare == LOSE) {
				message = "Your right hand loses.";
			}
			else if (compare == WIN) {
				message = "Your right hand wins.";
			}
			else if (compare == PUSH) {
				message = "Your right hand pushes.";
			}
			splitHand.setOutcome(compare);
		}
		System.out.println(message);

		double netWinnings = (playerHand.getBet() * playerHand.getOutcome()) + (splitHand.getBet() * splitHand.getOutcome());
		if (netWinnings < 0) {
			message = "You lose " + -netWinnings + " credit(s).";
		}
		else if (netWinnings == 0) {
			message = "You came out even.";
		}
		else if (netWinnings > 0) {
			message = "You win " + netWinnings + " credit(s).";
		}
		System.out.println(message);
	}
}

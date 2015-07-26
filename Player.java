import java.io.Serializable;
import java.util.ArrayList;

class Player implements Serializable {

	private final ArrayList<Hand> hands = new ArrayList<Hand>();
	private double wallet;
	private int highMark;

	public Player() {
		this.wallet = (double) 100;
		this.highMark = (int) this.wallet;
	}

	@Override
	public String toString() {
		String message = "Your hand: ";
		for (Hand hand : hands) {
			message += hand.toString(false);
		}
		return message;
	}

	public void newGame(Deck deck) {
		for (Hand hand : hands) {
			hand.returnHand(deck);
		}
		hands.clear();
	}

	public void addHand(Hand hand) {
		hands.add(hand);
	}

	public double getWallet() {
		return wallet;
	}

	public int getHighMark() {
		return highMark;
	}

	private void setHighMark(double highMark) {
		if (highMark > this.highMark) {
			this.highMark = (int) highMark;
		}
	}

	public boolean decreaseWallet(double amount) {
		if (amount > 0 && amount <= wallet) {
			wallet -= amount;
			return true;
		}
		return false;
	}

	private void increaseWallet(double amount) {
		wallet += amount;
	}


	public void settle() {
		for (Hand hand : hands) {
			increaseWallet(hand.getWalletChange());
		}
		setHighMark(wallet);
	}

	public boolean canPlay() {
		return wallet > 0;
	}

	public boolean canDoubleDown(double bet) {
		return wallet >= bet;
	}
}

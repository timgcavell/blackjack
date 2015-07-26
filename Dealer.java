import java.io.Serializable;

class Dealer implements Serializable {

	private final static int DEALER_HIT = 17;

	private Hand hand;

	@Override
	public String toString() {
		return "Dealer's hand: " + hand.toString(true);
	}

	public void setHand(Hand hand) {
		this.hand = hand;
	}

	public void playHand(Deck deck) {
		boolean stand = false;
		while (!stand && !hand.getBust()) {
			System.out.println("Dealer's hand: " + hand.toString(false));

			sleep();
			if (hand.getHandValue() < DEALER_HIT) {
				hand.addNextCard(deck);
				System.out.println("Dealer hits");
			}
			else {
				stand = true;
				System.out.println("Dealer stands");
			}
			System.out.println(Game.LINE_BREAK);
		}

		sleep();
		if (hand.getBust()) {
			System.out.println("Dealer's hand: " + hand.toString(false));
			System.out.println("Dealer busts.");
			System.out.println(Game.LINE_BREAK);
		}
		sleep();
	}

	private void sleep() {
		try {
			Thread.sleep(500);
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}

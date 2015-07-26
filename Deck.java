import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class Deck implements Serializable {

	private final Random rand;
	private final ArrayList<Card> cards;

	Deck() {
		rand = new Random(System.currentTimeMillis());
		cards = new ArrayList<Card>(52);
		addAllCards();
		shuffle();
	}
	
	@Override
	public String toString() {
		return cards.toString();
	}

	private void addAllCards() {
		int nameIndex = 0;
		int suiteIndex = 0;

		for (int i = 0; i < 52; i ++) {
			nameIndex = (nameIndex == 13) ? 0 : nameIndex;
			suiteIndex = (suiteIndex == 4) ? 0 : suiteIndex;

			cards.add(new Card(Card.CardName.values()[nameIndex], Card.CardSuite.values()[suiteIndex]));

			nameIndex++;
			suiteIndex = (i % 13 == 12) ? suiteIndex + 1 : suiteIndex;
		}
	}

	private void shuffle() {
		Collections.shuffle(cards);
		Collections.shuffle(cards, rand);
	}

	public void addCard(Card card) {
		cards.add(card);
	}

	public boolean removeCard(Card card) {
		return cards.remove(card);
	}

	public Card removeNextCard() {
		if (cards.isEmpty()) {
			System.out.println("New deck coming in.");
			addAllCards();
		}
		return cards.remove(0);
	}
}

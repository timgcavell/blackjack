import java.io.Serializable;
import java.util.ArrayList;

class Hand implements Serializable {

    private final static int BLACKJACK = 21;
    private final static int ALT_ACE_VALUE = 10;

    private final ArrayList<Card> cards = new ArrayList<Card>(2);

	private double bet = 0;
	private int handValue = 0;
	private boolean bust = false;
	private int outcome = 0;

	Hand(Deck deck) {
	    cards.add(deck.removeNextCard());
	    cards.add(deck.removeNextCard());
	    updateHandValue();
    }

	Hand(Deck deck, Card card) {
		cards.add(card);
		if (deck.removeCard(card)) {
			cards.add(deck.removeNextCard());
		}
		cards.add(deck.removeNextCard());
		updateHandValue();
	}

	@SuppressWarnings("unused")
	Hand(Deck deck, Card card1, Card card2) {
		cards.add(card1);
		cards.add(card2);
		if (deck.removeCard(card1)) {
			cards.add(deck.removeNextCard());
		}
		if (deck.removeCard(card2)) {
			cards.add(deck.removeNextCard());
		}
		updateHandValue();
	}

	public String toString(boolean hide) {
		String handString = "";
		if (hide) {
			for (int i = 1; i < cards.size(); i++) {
				handString += cards.get(i) + " ";
			}
			handString = handString.trim() +  ", 1 card hidden";
		}
		else {
			for (Card card : cards)
				handString += card + " ";
		}
		return handString;
	}

    public void addNextCard(Deck deck) {
        Card tempCard = deck.removeNextCard();
        if (tempCard != null) {
            cards.add(tempCard);
        }
        updateHandValue();
    }

	public Card removeCard() {
		return cards.remove(1);
	}

	public void returnHand(Deck deck) {
		for (Card card : cards) {
			deck.addCard(card);
		}
		cards.clear();
	}

	public boolean canSplit() {
		if (cards.size() == 2) {
			if (cards.get(0).getName().equals(cards.get(1).getName())) {
				return true;
			}
		}
		return false;
	}

    public boolean pairOfAces() {
	    if (cards.size() == 2) {
		    if (cards.get(0).getName().equals(Card.CardName.ACE.toString()) && cards.get(1).getName().equals(Card.CardName.ACE.toString())) {
			    return true;
		    }
	    }
	    return false;
    }

    public int getHandValue() {
        updateHandValue();
        return handValue;
    }

	public boolean getBust() {
		updateHandValue();
		return bust;
	}

	public double getBet() {
		return bet;
	}

	public void setBet(double bet) {
		this.bet = bet;
	}

	public double getOutcome() {
		return outcome;
	}

	public void setOutcome(int outcome) {
		this.outcome = outcome;
	}

	public void doubleDown() {
		bet = bet * 2;
	}

    private void updateHandValue() {
        int tempHandValue;
        int cardValue;
        int numAces = 0;

        handValue = 0;
        for (Card card : this.cards) {
            cardValue = card.getValue();
            handValue += cardValue;

            if (cardValue == 1) {
                numAces++;
            }
        }

        tempHandValue = this.handValue;
        for (int i = 0; i < numAces; i++) {
            tempHandValue += ALT_ACE_VALUE;

	        if (tempHandValue <= BLACKJACK) {
                handValue = tempHandValue;
            }
        }

	    if (handValue > BLACKJACK) {
		    bust = true;
	    }
    }

    public int compare(Hand otherHand) {
        if (getHandValue() > otherHand.getHandValue()) {
            return 1;
        }
        else if (getHandValue() < otherHand.getHandValue()) {
            return -1;
        }
        else {
            return 0;
        }
    }

    public boolean checkBlackJack() {
        updateHandValue();
        return cards.size() == 2 && handValue == BLACKJACK;
    }

	public double getWalletChange() {
		double walletChange = 0;
		if (outcome == -1) {
			walletChange = 0;
		}
		else if (outcome == 0) {
			walletChange = bet;
		}
		else if (outcome == 1) {
			walletChange = bet * 2;
		}
		else if (outcome == 2) {
			walletChange = bet * 2.5;
		}
		return walletChange;
	}
}

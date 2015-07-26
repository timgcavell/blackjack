import java.io.Serializable;

class Card implements Serializable {

	public enum CardName {ACE,TWO,THREE,FOUR,FIVE,SIX,SEVEN,EIGHT,NINE,TEN,JACK,QUEEN,KING}
	public enum CardSuite {HEART,DIAMOND,SPADE,CLUB}
	public enum CardColor {RED,BLACK}

	private final CardName name;
	private final CardSuite suite;
	private final CardColor color;
	private final int rank;
	private final int value;

	Card(CardName name, CardSuite suite) {
		this.name = name;
		this.suite = suite;
		this.color = (suite.equals(CardSuite.HEART) || suite.equals(CardSuite.DIAMOND)) ? CardColor.RED : CardColor.BLACK;
		this.rank = name.ordinal() + 1;
		if (name.ordinal() < 10) {
			value = name.ordinal() + 1;
		}
		else {
			value = 10;
		}
	}
	
	public String getName() {
		return name.toString();
	}

	@SuppressWarnings("unused")
	public String getSuite() {
		return suite.toString();
	}

	@SuppressWarnings("unused")
	public String getColor() {
		return color.toString();
	}
	
	@SuppressWarnings("unused")
	public int getRank() {
		return rank;
	}

	public int getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}

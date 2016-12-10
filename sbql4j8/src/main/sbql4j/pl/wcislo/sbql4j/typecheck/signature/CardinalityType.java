package pl.wcislo.sbql4j.typecheck.signature;

public enum CardinalityType {
	ANY(0, Integer.MAX_VALUE, false),
	SINGLE_RESULT(0, 1, false),
	BAG(0, Integer.MAX_VALUE, false),
	SEQUENCE(0, Integer.MAX_VALUE, true),
	ANY_COLLECTION(0, Integer.MAX_VALUE, false)
	;
	
	public int minCard, maxCard;
	public boolean ordered;
	private CardinalityType(int minCard, int maxCard, boolean ordered) {
		this.minCard = minCard;
		this.maxCard = maxCard;
		this.ordered = ordered;
	}
}

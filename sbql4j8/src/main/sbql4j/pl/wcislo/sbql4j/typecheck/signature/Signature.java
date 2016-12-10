package pl.wcislo.sbql4j.typecheck.signature;

public abstract class Signature {
	private Cardinality card;
	
	public Signature(Cardinality card) {
		super();
		this.card = card;
	}

	public Cardinality getCard() {
		return card;
	}

}

package pl.wcislo.sbql4j.typecheck.signature;

public class Cardinality {
	private int lowerBound;
	private int upperBound;
	private boolean ordered;
	
	public Cardinality(int lowerBound, int upperBound, boolean ordered) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.ordered = ordered;
	}

	public static Cardinality singleMandatory() {
		return new Cardinality(1, 1, false);
	}
	public static Cardinality singleOptional() {
		return new Cardinality(0, 1, false);
	}
	public static Cardinality bagOptional() {
		return new Cardinality(0, Integer.MAX_VALUE, false);
	}
	public static Cardinality bagMandatory() {
		return new Cardinality(1, Integer.MAX_VALUE, false);
	}
	public static Cardinality sequenceOptional() {
		return new Cardinality(0, Integer.MAX_VALUE, true);
	}
	public static Cardinality sequenceMandatory() {
		return new Cardinality(1, Integer.MAX_VALUE, true);
	}
	public static Cardinality ofType(CardinalityType t) {
		switch(t) {
			case BAG: return bagOptional();
			case SEQUENCE: return sequenceOptional();
			case SINGLE_RESULT: return singleMandatory();
		}
		return null;
	}
	
	public int getLowerBound() {
		return lowerBound;
	}
	public int getUpperBound() {
		return upperBound;
	}
	public boolean isOrdered() {
		return ordered;
	}

	public boolean match(CardinalityType cType) {
		boolean lowerBoundMatch = this.lowerBound >= cType.minCard;
		boolean upperBoundMatch = this.upperBound <= cType.maxCard;
		boolean orderMatch = cType.ordered ? this.ordered : true;
		
		return lowerBoundMatch && upperBoundMatch && orderMatch;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lowerBound;
		result = prime * result + (ordered ? 1231 : 1237);
		result = prime * result + upperBound;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cardinality other = (Cardinality) obj;
		if (lowerBound != other.lowerBound)
			return false;
		if (ordered != other.ordered)
			return false;
		if (upperBound != other.upperBound)
			return false;
		return true;
	}

	
	
}

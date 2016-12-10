package pl.wcislo.sbql4j.typecheck.signature;

public class ValueSignature<T> extends Signature {
	private Class<T> valueClass;
	
	public ValueSignature(Class<T> valueClass, Cardinality card) {
		super(card);
		this.valueClass = valueClass;
	}
	
	public Class<T> getValueClass() {
		return valueClass;
	}

}

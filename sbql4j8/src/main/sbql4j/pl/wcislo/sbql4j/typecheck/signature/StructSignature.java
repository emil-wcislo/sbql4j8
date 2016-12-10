package pl.wcislo.sbql4j.typecheck.signature;

import java.util.ArrayList;
import java.util.List;

public class StructSignature extends Signature {

	private List<Signature> fields = new ArrayList<Signature>();
	
	public StructSignature(Cardinality card) {
		super(card);
	}
	
	/**
	 * Adds a new field to the struct mSignature.
	 * @param sig is the mSignature to be added. 
	 */	
	public void addField(Signature sig) {
		if(sig instanceof StructSignature) {
			StructSignature ss = (StructSignature) sig;
			for(Signature structS : ss.getFields()) {
				fields.add(structS);
			}
		} else {
			fields.add(sig);			
		}
		
	}

	/**
	 * Adds a new fields to the struct mSignature.
	 * @param sig is the array with signatures to be added. 
	 */	
	public void addFields(Signature[] sig) {
		for (Signature s : sig) {
			addField(s);
		}
	}

	/**
	 * Return the fields of the struct mSignature.
	 * @return is the array of subsignatures of the struct mSignature. 
	 */	
	public Signature[] getFields() {
		return fields.toArray(new Signature[fields.size()]);
	}

	/**
	 * @return the number of structure fields
	 */
	public int fieldsNumber(){
		return fields.size();
	}
	
}

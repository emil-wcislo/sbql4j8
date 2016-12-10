package pl.wcislo.sbql4j.typecheck;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import pl.wcislo.sbql4j.typecheck.signature.Cardinality;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;
import pl.wcislo.sbql4j.typecheck.signature.Signature;
import pl.wcislo.sbql4j.typecheck.signature.ValueSignature;

public class TypeCheckTestUtils {
	public static void checkCard(Signature sig, /*int expMinBound, int expMaxBound, */CardinalityType expCardType) {
		assertNotNull("signature should not be null", sig);
		Cardinality card = sig.getCard();
		assertNotNull("cardinality should not be null", card);
//		assertEquals("min bound doesn't match", card.getLowerBound(), expMinBound);
//		assertEquals("max bound doesn't match", card.getUpperBound(), expMaxBound);
		assertTrue("cardinality type doesn't match", card.match(expCardType));
	}
	
	public static void checkValueType(Signature sig, Class expType) {
		assertTrue("expected value signature", sig instanceof ValueSignature);
		ValueSignature vSig = (ValueSignature) sig;
		assertEquals("result type doesn't match", expType, vSig.getValueClass());
	}
}

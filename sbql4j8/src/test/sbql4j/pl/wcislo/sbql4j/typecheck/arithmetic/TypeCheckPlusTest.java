package pl.wcislo.sbql4j.typecheck.arithmetic;

import static pl.wcislo.sbql4j.typecheck.TypeCheckTestUtils.checkCard;
import static pl.wcislo.sbql4j.typecheck.TypeCheckTestUtils.checkValueType;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pl.wcislo.sbql4j.ast.binary.PlusExpression;
import pl.wcislo.sbql4j.ast.literal.DoubleLiteral;
import pl.wcislo.sbql4j.ast.literal.IntegerLiteral;
import pl.wcislo.sbql4j.ast.literal.StringLiteral;
import pl.wcislo.sbql4j.typecheck.TypeChecker;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;
import pl.wcislo.sbql4j.typecheck.signature.Signature;

public class TypeCheckPlusTest {
	private TypeChecker tChecker;
	
	@BeforeClass
	public void setup() {
		tChecker = new TypeChecker();
	}
	
	@Test(groups="typecheck.arithmetic.plus", dependsOnGroups = { "typecheck.literal" })
	public void plusIntInt() {
		PlusExpression expr = new PlusExpression(new IntegerLiteral(10), new IntegerLiteral(-10));
		expr.accept(tChecker);
		Signature resSig = expr.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Integer.class);
	}
	
	@Test(groups="typecheck.arithmetic.plus", dependsOnGroups = { "typecheck.literal" })
	public void plusIntDouble() {
		PlusExpression expr = new PlusExpression(new IntegerLiteral(10), new DoubleLiteral(-10.0));
		expr.accept(tChecker);
		Signature resSig = expr.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Double.class);
	}
	
	@Test(groups="typecheck.arithmetic.plus", dependsOnGroups = { "typecheck.literal" })
	public void plusDoubleInt() {
		PlusExpression expr = new PlusExpression(new DoubleLiteral(10.1), new IntegerLiteral(10));
		expr.accept(tChecker);
		Signature resSig = expr.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Double.class);
	}
	
	@Test(groups="typecheck.arithmetic.plus", dependsOnGroups = { "typecheck.literal" })
	public void plusDoubleDouble() {
		PlusExpression expr = new PlusExpression(new DoubleLiteral(10.1), new DoubleLiteral(10.2));
		expr.accept(tChecker);
		Signature resSig = expr.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Double.class);
	}
	
	@Test(groups="typecheck.arithmetic.plus", dependsOnGroups = { "typecheck.literal" })
	public void plusIntString() {
		PlusExpression expr = new PlusExpression(new IntegerLiteral(10), new StringLiteral("aa"));
		expr.accept(tChecker);
		Signature resSig = expr.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, String.class);
	}
	
	@Test(groups="typecheck.arithmetic.plus", dependsOnGroups = { "typecheck.literal" })
	public void plusStringInt() {
		PlusExpression expr = new PlusExpression(new StringLiteral("aa"), new IntegerLiteral(10));
		expr.accept(tChecker);
		Signature resSig = expr.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, String.class);
	}
}

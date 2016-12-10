package pl.wcislo.sbql4j.typecheck.arithmetic;

import static pl.wcislo.sbql4j.typecheck.TypeCheckTestUtils.checkCard;
import static pl.wcislo.sbql4j.typecheck.TypeCheckTestUtils.checkValueType;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pl.wcislo.sbql4j.ast.binary.MinusExpression;
import pl.wcislo.sbql4j.ast.binary.PlusExpression;
import pl.wcislo.sbql4j.ast.literal.DoubleLiteral;
import pl.wcislo.sbql4j.ast.literal.IntegerLiteral;
import pl.wcislo.sbql4j.ast.literal.StringLiteral;
import pl.wcislo.sbql4j.typecheck.TypeChecker;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;
import pl.wcislo.sbql4j.typecheck.signature.Signature;

public class TypeCheckMinusTest {
	private TypeChecker tChecker;
	
	@BeforeClass
	public void setup() {
		tChecker = new TypeChecker();
	}
	
	@Test(groups="typecheck.arithmetic.minus", dependsOnGroups = { "typecheck.literal" })
	public void minusIntInt() {
		MinusExpression expr = new MinusExpression(new IntegerLiteral(10), new IntegerLiteral(-10));
		expr.accept(tChecker);
		Signature resSig = expr.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Integer.class);
	}
	
	@Test(groups="typecheck.arithmetic.minus", dependsOnGroups = { "typecheck.literal" })
	public void minusIntDouble() {
		MinusExpression expr = new MinusExpression(new IntegerLiteral(10), new DoubleLiteral(-10.0));
		expr.accept(tChecker);
		Signature resSig = expr.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Double.class);
	}
	
	@Test(groups="typecheck.arithmetic.minus", dependsOnGroups = { "typecheck.literal" })
	public void minusDoubleInt() {
		MinusExpression expr = new MinusExpression(new DoubleLiteral(10.1), new IntegerLiteral(10));
		expr.accept(tChecker);
		Signature resSig = expr.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Double.class);
	}
	
	@Test(groups="typecheck.arithmetic.minus", dependsOnGroups = { "typecheck.literal" })
	public void minusDoubleDouble() {
		MinusExpression expr = new MinusExpression(new DoubleLiteral(10.1), new DoubleLiteral(10.2));
		expr.accept(tChecker);
		Signature resSig = expr.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Double.class);
	}
	
}

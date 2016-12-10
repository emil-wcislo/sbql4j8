package pl.wcislo.sbql4j.typecheck;

import static pl.wcislo.sbql4j.typecheck.TypeCheckTestUtils.checkCard;
import static pl.wcislo.sbql4j.typecheck.TypeCheckTestUtils.checkValueType;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import pl.wcislo.sbql4j.ast.literal.BooleanLiteral;
import pl.wcislo.sbql4j.ast.literal.DoubleLiteral;
import pl.wcislo.sbql4j.ast.literal.IntegerLiteral;
import pl.wcislo.sbql4j.ast.literal.StringLiteral;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;
import pl.wcislo.sbql4j.typecheck.signature.Signature;


public class TypeCheckLiteralTest {

	private TypeChecker tChecker;
	
	@BeforeClass
	@BeforeGroups(groups="typecheck.literal")
	public void setup() {
		tChecker = new TypeChecker();
	}
	
	@Test(groups="typecheck.literal")
	public void integerLiteral() {
		IntegerLiteral integerLiteral = new IntegerLiteral(10);
		integerLiteral.accept(tChecker);
		Signature resSig = integerLiteral.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Integer.class);
	}
	
	@Test(groups="typecheck.literal")
	public void doubleLiteral() {
		DoubleLiteral doubleLiteral = new DoubleLiteral(10.1);
		doubleLiteral.accept(tChecker);
		Signature resSig = doubleLiteral.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Double.class);
	}
	
	@Test(groups="typecheck.literal")
	public void stringLiteral() {
		StringLiteral stringLiteral = new StringLiteral("asdf");
		stringLiteral.accept(tChecker);
		Signature resSig = stringLiteral.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, String.class);
	}

	@Test(groups="typecheck.literal")
	public void booleanLiteral() {
		BooleanLiteral booleanLiteral = new BooleanLiteral(true);
		booleanLiteral.accept(tChecker);
		Signature resSig = booleanLiteral.getSignature();
		checkCard(resSig, CardinalityType.SINGLE_RESULT);
		checkValueType(resSig, Boolean.class);
	}
	
//	@Test
//	public void plus() {
//		PlusExpression ex = new PlusExpression(leftExpr, rightExpr)
//	}
	

}

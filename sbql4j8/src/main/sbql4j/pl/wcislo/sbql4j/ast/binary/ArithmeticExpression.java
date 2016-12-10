package pl.wcislo.sbql4j.ast.binary;

import pl.wcislo.sbql4j.ast.Expression;
import pl.wcislo.sbql4j.typecheck.rule.SignatureDef;
import pl.wcislo.sbql4j.typecheck.rule.BinaryTypeRule;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;

public abstract class ArithmeticExpression extends BinaryExpression {

	public ArithmeticExpression(Expression leftExpr, Expression rightExpr) {
		super(leftExpr, rightExpr);
	}



}

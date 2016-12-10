package pl.wcislo.sbql4j.ast.binary;

import pl.wcislo.sbql4j.ast.Expression;
import pl.wcislo.sbql4j.ast.visitor.AstTreeVisitor;
import pl.wcislo.sbql4j.typecheck.rule.BinaryCardinalityRule;
import pl.wcislo.sbql4j.typecheck.rule.BinaryTypeRule;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;

@BinaryTypeRule(
	expectedLeft=	Integer.class,
	expectedRight=	Integer.class,
	result=			Integer.class
)
@BinaryTypeRule(
	expectedLeft=	Number.class, 
	expectedRight=	Double.class, 
	result=			Double.class 
)
@BinaryTypeRule(
	expectedLeft=	Double.class, 
	expectedRight=	Number.class, 
	result=			Double.class 
)
@BinaryCardinalityRule(
	expectedLeft=CardinalityType.SINGLE_RESULT,
	expectedRight=CardinalityType.SINGLE_RESULT,
	result=CardinalityType.SINGLE_RESULT
)
public class DivideExpression extends ArithmeticExpression {

	public DivideExpression(Expression leftExpr, Expression rightExpr) {
		super(leftExpr, rightExpr);
	}

	@Override
	public void accept(AstTreeVisitor visitor) {
		visitor.divide(this);
	}

}

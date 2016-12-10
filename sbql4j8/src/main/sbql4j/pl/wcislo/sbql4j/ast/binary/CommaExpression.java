package pl.wcislo.sbql4j.ast.binary;

import pl.wcislo.sbql4j.ast.Expression;
import pl.wcislo.sbql4j.ast.visitor.AstTreeVisitor;
import pl.wcislo.sbql4j.typecheck.rule.BinaryCardinalityRule;
import pl.wcislo.sbql4j.typecheck.rule.BinaryTypeRule;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;

@BinaryCardinalityRule(
	expectedLeft=CardinalityType.SINGLE_RESULT,
	expectedRight=CardinalityType.SINGLE_RESULT,
	result=CardinalityType.SINGLE_RESULT
)
@BinaryCardinalityRule(
	expectedLeft=CardinalityType.ANY_COLLECTION,
	expectedRight=CardinalityType.SINGLE_RESULT,
	result=CardinalityType.BAG
)
@BinaryCardinalityRule(
	expectedLeft=CardinalityType.SINGLE_RESULT,
	expectedRight=CardinalityType.ANY_COLLECTION,
	result=CardinalityType.BAG
)
public class CommaExpression extends ArithmeticExpression {

	public CommaExpression(Expression leftExpr, Expression rightExpr) {
		super(leftExpr, rightExpr);
	}

	@Override
	public void accept(AstTreeVisitor visitor) {
		visitor.comma(this);
	}

}

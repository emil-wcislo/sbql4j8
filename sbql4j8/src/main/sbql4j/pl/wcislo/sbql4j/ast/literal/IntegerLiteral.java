package pl.wcislo.sbql4j.ast.literal;

import pl.wcislo.sbql4j.ast.visitor.AstTreeVisitor;
import pl.wcislo.sbql4j.typecheck.rule.NoArgsRule;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;

@NoArgsRule(
	resultCardType=CardinalityType.SINGLE_RESULT,
	resultType=Integer.class
)
public class IntegerLiteral extends LiteralExpression<Integer> {

	public IntegerLiteral(Integer value) {
		super(value);
	}

	@Override
	public void accept(AstTreeVisitor visitor) {
		visitor.integerLiteral(this);
	}

}

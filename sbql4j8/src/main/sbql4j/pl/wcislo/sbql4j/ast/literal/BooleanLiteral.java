package pl.wcislo.sbql4j.ast.literal;

import pl.wcislo.sbql4j.ast.visitor.AstTreeVisitor;
import pl.wcislo.sbql4j.typecheck.rule.NoArgsRule;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;

@NoArgsRule(
	resultCardType=CardinalityType.SINGLE_RESULT,
	resultType=Boolean.class
)
public class BooleanLiteral extends LiteralExpression<Boolean> {

	public BooleanLiteral(Boolean value) {
		super(value);
	}

	@Override
	public void accept(AstTreeVisitor visitor) {
		visitor.booleanLiteral(this);
	}
}

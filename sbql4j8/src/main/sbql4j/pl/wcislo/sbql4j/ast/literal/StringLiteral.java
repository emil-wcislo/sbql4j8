package pl.wcislo.sbql4j.ast.literal;

import pl.wcislo.sbql4j.ast.visitor.AstTreeVisitor;
import pl.wcislo.sbql4j.typecheck.rule.NoArgsRule;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;

@NoArgsRule(
	resultCardType=CardinalityType.SINGLE_RESULT,
	resultType=String.class
)
public class StringLiteral extends LiteralExpression<String> {

	public StringLiteral(String value) {
		super(value);
	}

	@Override
	public void accept(AstTreeVisitor visitor) {
		visitor.stringLiteral(this);
	}

}

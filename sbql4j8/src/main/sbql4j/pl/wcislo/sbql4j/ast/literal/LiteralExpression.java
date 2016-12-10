package pl.wcislo.sbql4j.ast.literal;

import pl.wcislo.sbql4j.ast.Expression;
import pl.wcislo.sbql4j.ast.visitor.AstTreeVisitor;
import pl.wcislo.sbql4j.typecheck.rule.NoArgsRule;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;

public abstract class LiteralExpression<T> extends Expression {
	private T value;

	public LiteralExpression(T value) {
		super();
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
}

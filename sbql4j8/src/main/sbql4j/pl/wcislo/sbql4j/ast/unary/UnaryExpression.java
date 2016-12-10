package pl.wcislo.sbql4j.ast.unary;

import pl.wcislo.sbql4j.ast.Expression;

public abstract class UnaryExpression extends Expression {
	private Expression subExpr;

	public UnaryExpression(Expression subExpr) {
		super();
		this.subExpr = subExpr;
	}
	
	public Expression getSubExpr() {
		return subExpr;
	}
}
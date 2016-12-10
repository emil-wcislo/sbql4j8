package pl.wcislo.sbql4j.ast.binary;

import pl.wcislo.sbql4j.ast.Expression;

public abstract class BinaryExpression extends Expression {
	private Expression leftExpr, rightExpr;

	public BinaryExpression(Expression leftExpr, Expression rightExpr) {
		super();
		this.leftExpr = leftExpr;
		this.rightExpr = rightExpr;
	}
	
	public Expression getLeftExpr() {
		return leftExpr;
	}
	public Expression getRightExpr() {
		return rightExpr;
	}
}
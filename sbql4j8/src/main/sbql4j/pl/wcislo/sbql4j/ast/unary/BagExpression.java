package pl.wcislo.sbql4j.ast.unary;

import pl.wcislo.sbql4j.ast.Expression;
import pl.wcislo.sbql4j.ast.visitor.AstTreeVisitor;

public class BagExpression extends UnaryExpression {

	public BagExpression(Expression subExpr) {
		super(subExpr);
	}

	@Override
	public void accept(AstTreeVisitor visitor) {
		// TODO Auto-generated method stub

	}

}

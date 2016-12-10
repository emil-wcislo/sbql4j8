package pl.wcislo.sbql4j.ast.visitor;

import pl.wcislo.sbql4j.ast.binary.CommaExpression;
import pl.wcislo.sbql4j.ast.binary.DivideExpression;
import pl.wcislo.sbql4j.ast.binary.MinusExpression;
import pl.wcislo.sbql4j.ast.binary.MultiplyExpression;
import pl.wcislo.sbql4j.ast.binary.PlusExpression;
import pl.wcislo.sbql4j.ast.literal.BooleanLiteral;
import pl.wcislo.sbql4j.ast.literal.DoubleLiteral;
import pl.wcislo.sbql4j.ast.literal.IntegerLiteral;
import pl.wcislo.sbql4j.ast.literal.StringLiteral;
import pl.wcislo.sbql4j.ast.unary.BagExpression;

public interface AstTreeVisitor {
	public void booleanLiteral(BooleanLiteral expr);
	public void doubleLiteral(DoubleLiteral expr);
	public void integerLiteral(IntegerLiteral expr);
	public void stringLiteral(StringLiteral expr);
	
	public void divide(DivideExpression expr);
	public void minus(MinusExpression expr);
	public void multiply(MultiplyExpression expr);
	public void plus(PlusExpression expr);
	
	public void comma(CommaExpression expr);
	
	public void bag(BagExpression expr);
}

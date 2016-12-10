package pl.wcislo.sbql4j.typecheck;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pl.wcislo.sbql4j.ast.Expression;
import pl.wcislo.sbql4j.ast.binary.BinaryExpression;
import pl.wcislo.sbql4j.ast.binary.CommaExpression;
import pl.wcislo.sbql4j.ast.binary.DivideExpression;
import pl.wcislo.sbql4j.ast.binary.MinusExpression;
import pl.wcislo.sbql4j.ast.binary.MultiplyExpression;
import pl.wcislo.sbql4j.ast.binary.PlusExpression;
import pl.wcislo.sbql4j.ast.literal.BooleanLiteral;
import pl.wcislo.sbql4j.ast.literal.DoubleLiteral;
import pl.wcislo.sbql4j.ast.literal.IntegerLiteral;
import pl.wcislo.sbql4j.ast.literal.LiteralExpression;
import pl.wcislo.sbql4j.ast.literal.StringLiteral;
import pl.wcislo.sbql4j.ast.unary.BagExpression;
import pl.wcislo.sbql4j.ast.visitor.AstTreeVisitor;
import pl.wcislo.sbql4j.typecheck.rule.BinaryCardinalityRule;
import pl.wcislo.sbql4j.typecheck.rule.BinaryTypeRule;
import pl.wcislo.sbql4j.typecheck.rule.NoArgsRule;
import pl.wcislo.sbql4j.typecheck.signature.Cardinality;
import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;
import pl.wcislo.sbql4j.typecheck.signature.Signature;
import pl.wcislo.sbql4j.typecheck.signature.StructSignature;
import pl.wcislo.sbql4j.typecheck.signature.ValueSignature;

/**
 * Typechecker for SBQL4J query
 * @author Emil
 *
 */
public class TypeChecker implements AstTreeVisitor {

	public TypeChecker() {
	}

	@Override
	public void divide(DivideExpression expr) {
		binaryArithmeticOperator(expr);
	}

	@Override
	public void minus(MinusExpression expr) {
		binaryArithmeticOperator(expr);
	}

	@Override
	public void multiply(MultiplyExpression expr) {
		binaryArithmeticOperator(expr);
	}

	@Override
	public void plus(PlusExpression expr) {
		binaryArithmeticOperator(expr);
	}

	@Override
	public void comma(CommaExpression expr) {
		acceptLeft(expr);
		acceptRight(expr);
		Signature leftSig = expr.getLeftExpr().getSignature();
		assert leftSig != null;
		Cardinality leftCard = leftSig.getCard();
		assert leftCard != null;
		Signature rightSig = expr.getRightExpr().getSignature();
		assert rightSig != null;
		Cardinality rightCard = rightSig.getCard();
		assert rightCard != null;
		
		int resLowBound = leftCard.getLowerBound() * rightCard.getLowerBound();
		int resUpBound = leftCard.getUpperBound() * rightCard.getUpperBound();
		
		Cardinality resCard = new Cardinality(resLowBound, resUpBound, false);
		
		StructSignature resSig = new StructSignature(resCard);
		resSig.addField(leftSig);
		resSig.addField(rightSig);
	}
	
	@Override
	public void booleanLiteral(BooleanLiteral expr) {
		literalExpression(expr);
	}

	@Override
	public void doubleLiteral(DoubleLiteral expr) {
		literalExpression(expr);
	}

	@Override
	public void integerLiteral(IntegerLiteral expr) {
		literalExpression(expr);
	}

	@Override
	public void stringLiteral(StringLiteral expr) {
		literalExpression(expr);
	}
	
	@Override
	public void bag(BagExpression expr) {
		acceptSubExpr(expr);
		Signature subSig = expr.getSignature();
		assert subSig != null;
		
		if(subSig instanceof StructSignature) {
			StructSignature ssig = (StructSignature) subSig;
			Signature first = ssig.getFields()[0];
			ValueSignature vFirst = (ValueSignature) first;
		}
	}
	
	private void binaryArithmeticOperator(BinaryExpression expr) {
		acceptLeft(expr);
		checkCardinality(expr.getLeftExpr(), Cardinality.singleMandatory());
		acceptRight(expr);
		checkCardinality(expr.getRightExpr(), Cardinality.singleMandatory());
		checkBinaryOperatorRule(expr);
	}
	
	private void literalExpression(LiteralExpression expr) {
		Signature sig = readTypecheckerNoArgsRule(expr);
		expr.setSignature(sig);
	}
	
	
	private void acceptLeft(BinaryExpression expr) {
		expr.getLeftExpr().accept(this);
	}
	private void acceptRight(BinaryExpression expr) {
		expr.getRightExpr().accept(this);
	}
	private void acceptSubExpr(pl.wcislo.sbql4j.ast.unary.UnaryExpression expr) {
		expr.getSubExpr().accept(this);
	}
	private boolean checkCardinality(Expression e, Cardinality expected) {
		assert e != null;
		assert e.getSignature() != null;
		assert expected != null;
		Cardinality card = e.getSignature().getCard();
		if(card.equals(expected)) {
			return true;
		} 
		return false;
	}
	
	private boolean checkCardinalityType(Expression e, CardinalityType expected) {
		assert e != null;
		assert e.getSignature() != null;
		assert expected != null;
		Cardinality card = e.getSignature().getCard();
		if(expected == CardinalityType.ANY) {
			return true;
		} else if(expected == CardinalityType.ANY_COLLECTION) {
			return card.getUpperBound() > 1;
		} else if(expected == CardinalityType.BAG) {
			return card.getUpperBound() > 1 && !card.isOrdered();
		} else if(expected == CardinalityType.SEQUENCE) {
			return card.getUpperBound() > 1 && card.isOrdered();
		} else if(expected == CardinalityType.SINGLE_RESULT) {
			return card.getUpperBound() == 1;
		}
		return false;
	}

	/**
	 * Creates and assigns signature for expression with @NoArgsRule definition
	 * @param e
	 * @return
	 */
	private Signature readTypecheckerNoArgsRule(Expression e) {
		assert e != null;
		NoArgsRule rule = e.getClass().getAnnotation(NoArgsRule.class);
		assert rule != null;
		Cardinality card = Cardinality.ofType(rule.resultCardType());
		assert card != null;
		Class resultType = rule.resultType();
		ValueSignature resSig = new ValueSignature<>(resultType, card);
		return resSig;
	}
	
	/**
	 * Method uses type and cardinality rules defined in the AST element by annotations
	 * It first checks, if the left sub-expression is valid, then it evaluates right sub-expression 
	 * and checks it as well
	 * @param e
	 * @return
	 */
	private boolean checkBinaryOperatorRule(BinaryExpression e) {
		assert e != null;
		acceptLeft(e);
		Signature leftSig = e.getLeftExpr().getSignature();
		if(!(leftSig instanceof ValueSignature)) {
			return false;
		}
		ValueSignature leftValSig = (ValueSignature) leftSig;
		assert leftSig != null;
		
		BinaryCardinalityRule[] binaryCardRules = e.getClass().getAnnotationsByType(BinaryCardinalityRule.class);
		assert binaryCardRules != null;
		assert binaryCardRules.length > 0;
		List<BinaryCardinalityRule> matchingCards = new ArrayList<>();
		for(BinaryCardinalityRule ruleC : binaryCardRules) {
			CardinalityType expectedLeftCard = ruleC.expectedLeft();
			boolean cardMatch = isCardinalityMatch(leftValSig, expectedLeftCard);
			if(!cardMatch) {
				continue;
			}
			matchingCards.add(ruleC);
		}
		
		if(matchingCards.isEmpty()) {
			//cannot find matching card for left subexpression 
			return false;	
		}
		
		BinaryTypeRule[] binaryTypeRules = e.getClass().getAnnotationsByType(BinaryTypeRule.class);
		assert binaryTypeRules != null;
		assert binaryTypeRules.length > 0;
		List<BinaryTypeRule> matchingTypes = new ArrayList<>();
		//check signature match for left sub-expression first
		for(BinaryTypeRule ruleT : binaryTypeRules) {
			Class expectedType = ruleT.expectedLeft();
			boolean typeMatch = isClassMatching(leftValSig, expectedType);
			if(!typeMatch) {
				continue;
			}
			matchingTypes.add(ruleT);
		}
		if(matchingTypes.isEmpty()) {
			//cannot find matching type for left subexpression 
			return false;	
		}
		//left side seems OK (we have at least one matching card rule and one for the type)
		//now let's check the right subexpression
		acceptRight(e);
		Signature rightSig = e.getRightExpr().getSignature();
		assert rightSig != null;
		if(!(rightSig instanceof ValueSignature)) {
			return false;
		}
		ValueSignature rightValSig = (ValueSignature) rightSig;
		
		for(Iterator<BinaryCardinalityRule> it = matchingCards.iterator(); it.hasNext(); ) {
			BinaryCardinalityRule ruleC = it.next();
			CardinalityType expectedRightCard = ruleC.expectedRight();
			boolean cardMatch = isCardinalityMatch(rightValSig, expectedRightCard);
			if(!cardMatch) {
				it.remove();
			}
		}
		if(matchingCards.isEmpty()) {
			//cannot find matching card both for left and right subexpression 
			return false;	
		}
		for(Iterator<BinaryTypeRule> it = matchingTypes.iterator(); it.hasNext(); ) {
			BinaryTypeRule ruleT = it.next();
			Class expectedType = ruleT.expectedRight();
			boolean typeMatch = isClassMatching(rightValSig, expectedType);
			if(!typeMatch) {
				it.remove();
			}
		}
		if(matchingTypes.isEmpty()) {
			//cannot find matching type for both left and right subexpression 
			return false;	
		}
		//creating signature for the result
		CardinalityType resultCard = matchingCards.get(0).result();
		Class resultClass = matchingTypes.get(0).result();
		
		ValueSignature resSig = new ValueSignature<>(resultClass, Cardinality.ofType(resultCard));
		e.setSignature(resSig);
		return true;
	}
	
	private boolean isCardinalityMatch(Signature sig, CardinalityType expectedCard) {
		Cardinality card = sig.getCard();
		boolean matchCard = card.match(expectedCard);
		return matchCard;
	}
	
	private boolean isClassMatching(ValueSignature vSig, Class expectedType) {
//		Class expectedType = def.resultType();
		Class currentType = vSig.getValueClass();
		boolean classMatch = expectedType.isAssignableFrom(currentType);
//		boolean cardMatch = vSig.getCard().match(def.resultCardType());
		return classMatch;
	}
	
}
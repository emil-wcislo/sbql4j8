package pl.wcislo.sbql4j.ast;

import pl.wcislo.sbql4j.ast.visitor.AstTreeVisitor;
import pl.wcislo.sbql4j.typecheck.signature.Signature;

public abstract class Expression {
	/**
	 * Typechecker 
	 */
	private Signature signature;
	
	public abstract void accept(AstTreeVisitor visitor);
	
	public Signature getSignature() {
		return signature;
	}
	public void setSignature(Signature signature) {
		this.signature = signature;
	}
}

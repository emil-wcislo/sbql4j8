package pl.wcislo.sbql4j.typecheck.rule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;

@Repeatable(BinaryCardinalityRules.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BinaryCardinalityRule {
	CardinalityType expectedLeft();
	CardinalityType expectedRight();
	CardinalityType result();
}

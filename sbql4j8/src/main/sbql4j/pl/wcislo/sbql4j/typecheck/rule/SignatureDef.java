package pl.wcislo.sbql4j.typecheck.rule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import pl.wcislo.sbql4j.typecheck.signature.CardinalityType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SignatureDef {
	CardinalityType resultCardType();
	Class resultType();
}

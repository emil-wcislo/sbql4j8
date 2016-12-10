package pl.wcislo.sbql4j.typecheck.rule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(BinaryTypeRules.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BinaryTypeRule {
	Class expectedLeft();
	Class expectedRight();
	Class result();
}

package ru.akh.spring.validation.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import ru.akh.spring.validation.constraintvalidators.SpELClassValidator;

/**
 * Validates class with SpEL expression.
 * 
 * @see <a href=
 *      "http://javatar81.blogspot.com/2016/06/hibernate-validator-spring-expression.html">How
 *      to SpEL Validation - Class-Level & Cross-Parameter Constraints with
 *      Spring Expression Language</a>
 */
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValidateClassExpression.List.class)
@Constraint(validatedBy = SpELClassValidator.class)
@Documented
public @interface ValidateClassExpression {

    String message() default "{expression.validation.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String value();

    /**
     * Defines several {@code @ValidateClassExpression} annotations on the same
     * element.
     */
    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface List {

        ValidateClassExpression[] value();

    }

}

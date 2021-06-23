package ru.akh.spring.validation.constraintvalidators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import ru.akh.spring.validation.constraints.ValidateClassExpression;

/**
 * Checks that a given class satisfies a SpEL expression.
 * 
 * @see <a href=
 *      "http://javatar81.blogspot.com/2016/06/hibernate-validator-spring-expression.html">How
 *      to SpEL Validation - Class-Level & Cross-Parameter Constraints with
 *      Spring Expression Language</a>
 */
public class SpELClassValidator implements ConstraintValidator<ValidateClassExpression, Object> {

    private Expression expression;

    @Override
    public void initialize(ValidateClassExpression constraintAnnotation) {
        expression = new SpelExpressionParser().parseExpression(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        StandardEvaluationContext spelContext = new StandardEvaluationContext(value);
        return (Boolean) expression.getValue(spelContext);
    }

}

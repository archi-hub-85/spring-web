package ru.akh.spring.validation.constraintvalidators;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import ru.akh.spring.validation.constraints.ValidateParametersExpression;

/**
 * Checks that given method parameters satisfies a SpEL expression.
 * 
 * @see <a href=
 *      "http://javatar81.blogspot.com/2016/06/hibernate-validator-spring-expression.html">How
 *      to SpEL Validation - Class-Level & Cross-Parameter Constraints with
 *      Spring Expression Language</a>
 */
public class SpELParametersValidator implements ConstraintValidator<ValidateParametersExpression, Object[]> {

    private Expression expression;

    @Override
    public void initialize(ValidateParametersExpression constraintAnnotation) {
        expression = new SpelExpressionParser().parseExpression(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(Object[] values, ConstraintValidatorContext context) {
        StandardEvaluationContext spelContext = new StandardEvaluationContext(values);
        Map<String, Object> spelVars = IntStream.range(0, values.length).boxed()
                .collect(Collectors.toMap(i -> "arg" + i, i -> values[i]));
        spelContext.setVariables(spelVars);
        return (Boolean) expression.getValue(spelContext);
    }

}

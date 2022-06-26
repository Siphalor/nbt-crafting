package de.siphalor.nbtcrafting.dollar.operator;

import java.util.Stack;
import java.util.function.Function;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;

public interface UnaryOperator extends Operator {
	Object apply(Object value);

	default void apply(Stack<Object> stack, Function<String, Object> referenceResolver) throws DollarEvaluationException {
		stack.push(apply(stack.pop()));
	}
}

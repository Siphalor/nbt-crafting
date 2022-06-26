package de.siphalor.nbtcrafting.dollar.operator;

import java.util.Stack;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.token.DollarToken;

public interface UnaryPrefixOperator extends Operator {
	Object apply(Object value, Function<String, Object> referenceResolver) throws DollarEvaluationException;

	default void apply(Stack<Object> stack, Function<String, Object> referenceResolver) throws DollarEvaluationException {
		stack.push(apply(stack.pop(), referenceResolver));
	}

	@Override
	default DollarToken.@NotNull Type getTokenType() {
		return DollarToken.Type.PREFIX_OPERATOR;
	}
}

package de.siphalor.nbtcrafting.dollar.operator;

import java.util.Stack;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;

public interface BinaryOperator extends Operator {
	@Nullable Object apply(@Nullable Object left, @Nullable Object right, @NotNull Function<String, Object> referenceResolver) throws DollarEvaluationException;

	@Override
	default void apply(Stack<Object> stack, Function<String, Object> referenceResolver) throws DollarEvaluationException {
		Object right = stack.pop();
		Object left = stack.pop();
		stack.push(apply(left, right, referenceResolver));
	}
}

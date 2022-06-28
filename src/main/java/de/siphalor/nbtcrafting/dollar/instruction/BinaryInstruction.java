package de.siphalor.nbtcrafting.dollar.instruction;

import java.util.Stack;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;

public interface BinaryInstruction extends Instruction {
	@Nullable Object apply(@Nullable Object left, @Nullable Object right, @NotNull Function<String, Object> referenceResolver) throws DollarEvaluationException;

	@Override
	default void apply(Stack<Object> stack, DollarRuntime.Context context) throws DollarEvaluationException {
		Object right = stack.pop();
		Object left = stack.pop();
		stack.push(apply(left, right, context::resolveReference));
	}
}

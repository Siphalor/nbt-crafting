package de.siphalor.nbtcrafting.dollar.instruction;

import java.util.Stack;
import java.util.function.Function;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;

public interface UnaryPrefixInstruction extends Instruction {
	Object apply(Object value, Function<String, Object> referenceResolver) throws DollarEvaluationException;

	default void apply(Stack<Object> stack, DollarRuntime.Context context) throws DollarEvaluationException {
		stack.push(apply(stack.pop(), context::resolveReference));
	}

}

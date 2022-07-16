package de.siphalor.nbtcrafting.dollar.instruction;

import java.util.Stack;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;
import de.siphalor.nbtcrafting.dollar.function.DollarFunction;

public class StaticCallInstruction implements Instruction {
	private final int parameterCount;
	private final DollarFunction function;

	public StaticCallInstruction(int parameterCount, DollarFunction function) {
		this.parameterCount = parameterCount;
		this.function = function;
	}

	@Override
	public int getPrecedence() {
		return 0;
	}

	@Override
	public void apply(Stack<Object> stack, DollarRuntime.Context context) throws DollarEvaluationException {
		Object[] parameters = new Object[parameterCount];
		for (int p = parameterCount - 1; p >= 0; p--) {
			parameters[p] = stack.pop();
		}
		stack.push(function.call(parameters, context));
	}
}

package de.siphalor.nbtcrafting.dollar.instruction;

import java.util.Stack;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;

public class PushInstruction implements Instruction {
	private final Object value;

	public PushInstruction(Object value) {
		this.value = value;
	}

	@Override
	public int getPrecedence() {
		return 0;
	}

	@Override
	public void apply(Stack<Object> stack, DollarRuntime.Context context) throws DollarEvaluationException {
		stack.push(value);
	}
}

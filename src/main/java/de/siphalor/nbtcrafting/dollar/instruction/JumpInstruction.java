package de.siphalor.nbtcrafting.dollar.instruction;

import java.util.Stack;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;

public class JumpInstruction implements Instruction {
	public int offset;

	public JumpInstruction(int offset) {
		this.offset = offset;
	}

	@Override
	public int getPrecedence() {
		return 0;
	}

	@Override
	public void apply(Stack<Object> stack, DollarRuntime.Context context) throws DollarEvaluationException {
		context.jumpOffset(offset - 1); // -1 because the instruction pointer is incremented after the jump
	}
}

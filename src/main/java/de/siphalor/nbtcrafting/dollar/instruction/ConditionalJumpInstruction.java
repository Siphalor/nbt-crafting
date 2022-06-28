package de.siphalor.nbtcrafting.dollar.instruction;

import java.util.Stack;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;
import de.siphalor.nbtcrafting.dollar.DollarUtil;

public class ConditionalJumpInstruction extends JumpInstruction {
	public ConditionalJumpInstruction(int offset) {
		super(offset);
	}

	@Override
	public void apply(Stack<Object> stack, DollarRuntime.Context context) throws DollarEvaluationException {
		if (DollarUtil.asBoolean(stack.pop())) {
			super.apply(stack, context);
		}
	}
}

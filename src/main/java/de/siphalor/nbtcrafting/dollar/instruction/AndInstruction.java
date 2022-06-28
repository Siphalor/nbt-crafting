package de.siphalor.nbtcrafting.dollar.instruction;

import java.util.Stack;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;
import de.siphalor.nbtcrafting.dollar.DollarUtil;

public class AndInstruction extends JumpInstruction {
	public AndInstruction(int offset) {
		super(offset);
	}

	@Override
	public void apply(Stack<Object> stack, DollarRuntime.Context context) throws DollarEvaluationException {
		if (!DollarUtil.asBoolean(stack.peek())) {
			super.apply(stack, context);
		} else {
			stack.pop();
		}
	}
}

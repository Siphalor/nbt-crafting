package de.siphalor.nbtcrafting.dollar.jump;

import java.util.Stack;

import de.siphalor.nbtcrafting.dollar.DollarUtil;

public class ConditionalJump extends Jump {
	public ConditionalJump(int offset) {
		super(offset);
	}

	@Override
	public int apply(int position, Stack<Object> stack) {
		if (DollarUtil.asBoolean(stack.pop())) {
			return position + offset - 1;
		}
		return position;
	}
}

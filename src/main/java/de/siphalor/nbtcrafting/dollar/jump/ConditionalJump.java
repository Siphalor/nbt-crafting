package de.siphalor.nbtcrafting.dollar.jump;

import java.util.Stack;

import de.siphalor.nbtcrafting.dollar.DollarUtil;

public class ConditionalJump implements Jump {
	public int offset;

	public ConditionalJump(int offset) {
		this.offset = offset;
	}

	@Override
	public int apply(int position, Stack<Object> stack) {
		if (DollarUtil.asBoolean(stack.pop())) {
			return position + offset;
		}
		return position;
	}
}

package de.siphalor.nbtcrafting.dollar.jump;

import java.util.Stack;

public class UnconditionalJump implements Jump {
	public int offset;

	public UnconditionalJump(int offset) {
		this.offset = offset;
	}

	@Override
	public int apply(int position, Stack<Object> stack) {
		return position + offset - 1;
	}
}

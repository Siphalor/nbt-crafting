package de.siphalor.nbtcrafting.dollar.jump;

import java.util.Stack;

public class UnconditionalJump extends Jump {
	public UnconditionalJump(int offset) {
		super(offset);
	}

	@Override
	public int apply(int position, Stack<Object> stack) {
		return position + offset - 1;
	}
}

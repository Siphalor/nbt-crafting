package de.siphalor.nbtcrafting.dollar.jump;

import java.util.Stack;

public abstract class Jump {
	public int offset;

	public Jump(int offset) {
		this.offset = offset;
	}

	public abstract int apply(int position, Stack<Object> stack);
}

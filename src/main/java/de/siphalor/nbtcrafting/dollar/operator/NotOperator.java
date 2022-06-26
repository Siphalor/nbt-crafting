package de.siphalor.nbtcrafting.dollar.operator;

import de.siphalor.nbtcrafting.dollar.DollarUtil;

public class NotOperator implements UnaryPrefixOperator {
	@Override
	public int getPrecedence() {
		return 20;
	}

	@Override
	public Object apply(Object value) {
		return !DollarUtil.asBoolean(value);
	}
}

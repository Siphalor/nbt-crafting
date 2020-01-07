package de.siphalor.nbtcrafting.dollar.part;

import java.util.Map;

public abstract class ConstantDollarPart implements DollarPart {
	@Override
	public Object evaluate(Map<String, Object> reference) {
		return getValue();
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	public abstract Object getValue();
}

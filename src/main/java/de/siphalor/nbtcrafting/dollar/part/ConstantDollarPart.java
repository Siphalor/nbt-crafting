package de.siphalor.nbtcrafting.dollar.part;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public abstract class ConstantDollarPart implements DollarPart {
	@Override
	public Object evaluate(Map<String, CompoundTag> reference) {
		return getValue();
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	public abstract Object getValue();
}

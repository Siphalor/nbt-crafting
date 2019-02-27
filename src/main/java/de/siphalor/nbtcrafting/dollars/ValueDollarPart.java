package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;

public class ValueDollarPart implements DollarPart {
	public Object value;

	@Override
	public ValueDollarPart apply(HashMap<String, CompoundTag> references) throws DollarException {
		return this;
	}
}

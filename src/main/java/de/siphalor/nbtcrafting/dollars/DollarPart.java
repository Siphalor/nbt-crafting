package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;

public interface DollarPart {
	public ValueDollarPart apply(HashMap<String, CompoundTag> references) throws DollarException;
}

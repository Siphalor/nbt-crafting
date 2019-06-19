package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public interface DollarPart {
	ValueDollarPart apply(Map<String, CompoundTag> references) throws DollarException;
}

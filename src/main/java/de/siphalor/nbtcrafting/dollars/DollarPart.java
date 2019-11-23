package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public interface DollarPart {
	DollarValue apply(Map<String, CompoundTag> reference) throws DollarException;
}

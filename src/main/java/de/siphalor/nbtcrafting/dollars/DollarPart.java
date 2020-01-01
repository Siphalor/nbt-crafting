package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.util.Map;

public interface DollarPart {
	Tag evaluate(Map<String, CompoundTag> reference) throws DollarException;

	interface Deserializer {
		boolean matches(int character, DollarParser dollarParser, boolean hasOtherPart);
		DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException;
	}
}

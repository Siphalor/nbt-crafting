package de.siphalor.nbtcrafting.dollar.part;

import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.io.IOException;
import java.util.Map;

public interface DollarPart {
	Tag evaluate(Map<String, CompoundTag> reference) throws DollarException;

	interface Deserializer {
		boolean matches(int character, DollarParser dollarParser);
		DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException;
	}

	interface UnaryDeserializer {
		boolean matches(int character, DollarParser dollarParser);
		DollarPart parse(DollarParser dollarParser) throws DollarException;
	}
}

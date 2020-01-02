package de.siphalor.nbtcrafting.dollar.part;

import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public interface DollarPart {
	Object evaluate(Map<String, CompoundTag> reference) throws DollarException;
	default boolean isConstant() {
		return false;
	}

	interface Deserializer {
		boolean matches(int character, DollarParser dollarParser);
		DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException;
	}

	interface UnaryDeserializer {
		boolean matches(int character, DollarParser dollarParser);
		DollarPart parse(DollarParser dollarParser) throws DollarException;
	}
}

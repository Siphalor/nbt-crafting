package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.dollars.value.DollarValue;
import net.minecraft.nbt.CompoundTag;

import java.io.IOException;
import java.util.Map;

public interface DollarPart {
	DollarValue apply(Map<String, CompoundTag> reference) throws DollarException;

	interface Factory<T extends DollarPart> {
		boolean matches(int character);
		T parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException;
	}
}

package de.siphalor.nbtcrafting.dollars.operator;

import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarPart;
import de.siphalor.nbtcrafting.dollars.value.DollarValue;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public abstract class BinaryDollarOperator implements DollarPart {
	private DollarPart first;
	private DollarPart second;

	public BinaryDollarOperator(DollarPart first, DollarPart second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public DollarValue apply(Map<String, CompoundTag> reference) throws DollarException {
		return apply(first.apply(reference), second.apply(reference));
	}

	public abstract DollarValue apply(DollarValue first, DollarValue second);
}

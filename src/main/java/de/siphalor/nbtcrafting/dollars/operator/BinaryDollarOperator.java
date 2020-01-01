package de.siphalor.nbtcrafting.dollars.operator;

import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public abstract class BinaryDollarOperator implements DollarPart {
	private DollarPart first;
	private DollarPart second;

	public BinaryDollarOperator(DollarPart first, DollarPart second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public Tag evaluate(Map<String, CompoundTag> reference) throws DollarException {
		return apply(first.evaluate(reference), second.evaluate(reference));
	}

	public abstract Tag apply(Tag first, Tag second);
}

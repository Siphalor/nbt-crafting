package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public class ConstantDollarPart implements DollarPart {
	private Tag tag;

	private ConstantDollarPart(Tag tag) {
		this.tag = tag;
	}

	public static ConstantDollarPart of(Tag tag) {
		return new ConstantDollarPart(tag);
	}

	public static ConstantDollarPart copy(Tag tag) {
		return new ConstantDollarPart(tag.copy());
	}

	@Override
	public Tag evaluate(Map<String, CompoundTag> reference) {
		return tag;
	}
}

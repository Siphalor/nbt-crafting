package de.siphalor.nbtcrafting.dollars.operator;

import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public abstract class UnaryDollarOperator implements DollarPart {
	DollarPart dollarPart;

	public UnaryDollarOperator(DollarPart dollarPart) {
		this.dollarPart = dollarPart;
	}

	@Override
	public final Tag evaluate(Map<String, CompoundTag> reference) throws DollarException {
		return evaluate(dollarPart.evaluate(reference));
	}

	public abstract Tag evaluate(Tag tag);
}

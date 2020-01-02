package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
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

package de.siphalor.nbtcrafting.dollar;

import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.Tag;

import java.util.Map;

public abstract class Dollar {
	protected DollarPart expression;

	protected Dollar(DollarPart expression) {
		this.expression = expression;
	}

	protected Tag evaluate(Map<String, Object> references) throws DollarEvaluationException {
		return NbtUtil.asTag(expression.evaluate(references));
	}

	public abstract void apply(ItemStack stack, Map<String, Object> references) throws DollarException;
}

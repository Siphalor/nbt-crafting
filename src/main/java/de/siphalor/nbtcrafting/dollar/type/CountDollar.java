package de.siphalor.nbtcrafting.dollar.type;

import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public class CountDollar extends Dollar {
	public CountDollar(DollarPart expression) {
		super(expression);
	}

	@Override
	public void apply(ItemStack stack, Map<String, Object> references) throws DollarException {
		Tag value = NbtUtil.asTag(expression.evaluate(references));
		if (!(value instanceof AbstractNumberTag)) {
			throw new DollarEvaluationException("Couldn't set dollar computed count of stack as it's not a number");
		} else {
			stack.setCount(((AbstractNumberTag) value).getInt());
		}
	}
}

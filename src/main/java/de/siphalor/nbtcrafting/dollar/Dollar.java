package de.siphalor.nbtcrafting.dollar;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.nbt.NbtException;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;

public class Dollar {
	protected final String path;
	protected DollarPart expression;

	protected Dollar(String path) {
		this.path = path;
	}

	public void apply(ItemStack stack, Map<String, Object> references) throws DollarException {
		Tag value = NbtUtil.asTag(expression.evaluate(references));
		if(path.isEmpty()) {
			if (!(value instanceof CompoundTag)) {
				throw new DollarEvaluationException("Couldn't set stacks main tag as given dollar expression evaluates to non-object value.");
			} else {
				NbtUtil.mergeInto(stack.getOrCreateTag(), (CompoundTag) value, false);
			}
		} else if (path.equals(NbtCrafting.MOD_ID + ":count")) {
			if (!(value instanceof AbstractNumberTag)) {
				throw new DollarEvaluationException("Couldn't set dollar computed count of stack as it's not a number");
			} else {
				stack.setCount(((AbstractNumberTag) value).getInt());
			}
		} else {
			CompoundTag compoundTag = stack.getOrCreateTag();
			String[] pathParts = NbtUtil.splitPath(path);
			try {
				CompoundTag parent = NbtUtil.getTagOrCreate(compoundTag, ArrayUtils.subarray(pathParts, 0, pathParts.length - 1));
				if (value != null)
					parent.put(pathParts[pathParts.length - 1], value);
			} catch (NbtException e) {
				e.printStackTrace();
			}
		}
	}
}

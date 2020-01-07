package de.siphalor.nbtcrafting.dollar;

import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.util.nbt.NbtException;
import de.siphalor.nbtcrafting.util.nbt.NbtHelper;
import net.minecraft.item.ItemStack;
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
		Tag value = NbtHelper.asTag(expression.evaluate(references));
		if(path.isEmpty()) {
			if(!(value instanceof CompoundTag)) {
				throw new DollarEvaluationException("Couldn't set stacks main tag as given dollar expression evaluates to non-object value.");
			} else {
				NbtHelper.mergeInto(stack.getOrCreateTag(), (CompoundTag) value, false);
			}
		} else {
			CompoundTag compoundTag = stack.getOrCreateTag();
			String[] pathParts = NbtHelper.splitPath(path);
			try {
				CompoundTag parent = NbtHelper.getTagOrCreate(compoundTag, ArrayUtils.subarray(pathParts, 0, pathParts.length - 1));
				if (value != null)
					parent.put(pathParts[pathParts.length - 1], value);
			} catch (NbtException e) {
				e.printStackTrace();
			}
		}
	}
}

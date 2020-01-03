package de.siphalor.nbtcrafting.dollar;

import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public class Dollar {
	protected final String key;
	protected DollarPart expression;

	protected Dollar(String key) {
		this.key = key;
	}

	public void apply(ItemStack stack, Map<String, CompoundTag> references) throws DollarException {
		CompoundTag compoundTag = stack.getOrCreateTag();
		String[] pathParts = NbtHelper.splitPath(key);
		CompoundTag parent = NbtHelper.getParentTagOrCreate(compoundTag, pathParts);
		Tag value = NbtHelper.asTag(expression.evaluate(references));
		if(value != null)
			parent.put(pathParts[pathParts.length - 1], value);
	}
}

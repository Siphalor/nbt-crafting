package de.siphalor.nbtcrafting.dollar;

import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public class Dollar {
	protected final String key;
	protected final String lastKeyPart;
	protected DollarPart expression;

	protected Dollar(String key) {
		this.key = key;
		this.lastKeyPart = key.substring(key.lastIndexOf('.') + 1);
	}

	public void apply(ItemStack stack, Map<String, CompoundTag> references) throws DollarException {
		CompoundTag compoundTag = stack.getOrCreateTag();
		CompoundTag parent = NbtHelper.getParentTagOrCreate(compoundTag, key);
		Tag value = NbtHelper.asTag(expression.evaluate(references));
		if(value != null)
			parent.put(lastKeyPart, value);
	}
}

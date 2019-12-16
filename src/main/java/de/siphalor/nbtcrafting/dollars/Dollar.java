package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.dollars.value.DollarValue;
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
		DollarValue value = expression.apply(references);

		if(value.isNumeric()) {
			parent.putDouble(lastKeyPart, value.asNumber().doubleValue());
		} else {
			parent.putString(lastKeyPart, value.toString());
		}
	}
}

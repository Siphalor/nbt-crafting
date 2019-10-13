package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class DollarNbt {

	private Map<String, String> dollars;

	private DollarNbt() {
		dollars = new HashMap<>();
	}

	public void apply(ItemStack to, Map<String, ItemStack> reference) {

	}

	public static DollarNbt fromCompoundTag(CompoundTag from) {
        DollarNbt dollarNbt = new DollarNbt();
        dollarNbt.loadFromCompoundTagRecurse(from, "");
        return dollarNbt;
	}

	private void loadFromCompoundTagRecurse(CompoundTag from, String path) {
		for(String key : from.getKeys()) {
			if(NbtHelper.isCompound(from.get(key))) {
				loadFromCompoundTagRecurse(from.getCompound(key), path + "." + key);
			} else if(NbtHelper.isString(from.get(key))) {
				if(from.getString(key).charAt(0) == '$') {
					dollars.put(path + "." + key, from.getString(key).substring(1));
				}
			}
		}
	}
}

package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;

public class Dollar {
	protected final String key;
	protected final String lastKeyPart;
	protected GroupDollarPart expression;

	protected Dollar(String key) {
		this.key = key;
		this.lastKeyPart = key.substring(key.lastIndexOf('.') + 1);
	}

	public void apply(ItemStack stack, HashMap<String, CompoundTag> references) throws DollarException {
		CompoundTag compoundTag = stack.getOrCreateTag();
        CompoundTag parent = NbtHelper.getParentTagOrCreate(compoundTag, key);
        Object value = expression.apply(references).value;
        if(value instanceof Tag)
        	compoundTag.put(lastKeyPart, (Tag) value);
        else if(value instanceof Double) {
	        compoundTag.putDouble(lastKeyPart, (Double) value);
	        if(key.equals("Damage")) {
	        	if(stack.getDamage() >= stack.getDurability())
	        		stack.split(1);
	        }
        } else if(value instanceof String)
        	compoundTag.putString(lastKeyPart, (String) value);
        else
        	throw new DollarException("Unknown type in dollar expression");
	}

	public static Dollar[] extractDollars(CompoundTag compoundTag) {
		ArrayList<Dollar> dollars = new ArrayList<>();
		NbtHelper.iterateCompounds(compoundTag, (path, tag) -> {
			if(NbtHelper.isString(tag)) {
				if(tag.asString().charAt(0) == '$') {
					dollars.add(new DollarParser().parse(path, tag.asString().substring(1)));
					return true;
				}
			}
			return false;
		});
		return dollars.toArray(new Dollar[0]);
	}

}

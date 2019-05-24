package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

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
        if(value instanceof CompoundTag) {
			if(lastKeyPart.equals("$")) {
				NbtHelper.mergeInto(parent, (CompoundTag) value, true);
			} else {
				NbtHelper.mergeInto(parent.getCompound(lastKeyPart), (CompoundTag) value, true);
			}
		} if(value instanceof Tag)
        	parent.put(lastKeyPart, (Tag) value);
        else if(value instanceof Double) {
	        parent.putDouble(lastKeyPart, (Double) value);
	        if(key.equals("Damage")) {
	        	if(stack.getDamage() >= stack.getDurability())
	        		stack.split(1);
	        }
        } else if(value instanceof String)
        	parent.putString(lastKeyPart, (String) value);
        else
        	throw new DollarException("Unknown type in dollar expression");
	}

}

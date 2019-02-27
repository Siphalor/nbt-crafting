package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;

public class ReferenceDollarPart implements DollarPart {
	public String path;
	public String ingredientId;

	public ReferenceDollarPart(String path) {
		int index = path.indexOf('.');
		this.ingredientId = path.substring(0, index);
		this.path = path.substring(index + 1);
	}

	public ValueDollarPart apply(HashMap<String, CompoundTag> references) throws DollarException {
		CompoundTag parent = Dollar.getParentTagOrCreate(references.get(ingredientId), path);
		Tag tag = parent.getTag(path.substring(path.lastIndexOf('.') + 1));
		ValueDollarPart part = new ValueDollarPart();
		if(NbtHelper.isString(tag))
			part.value = tag.asString();
		else if(NbtHelper.isNumeric(tag))
			part.value = ((AbstractNumberTag) tag).getDouble();
		else
			part.value = tag;
		return part;
	}
}

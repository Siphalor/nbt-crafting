package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;

public class ReferenceDollarPart implements DollarPart {
	public String ingredientId;
	public String path;
	public String key;

	public ReferenceDollarPart(String path) {
		int index = path.indexOf('.');
		ingredientId = path.substring(0, index);
		this.path = path.substring(index + 1);
		index = this.path.lastIndexOf('.');
		if(index == -1) {
			key = this.path;
			this.path = "";
		} else {
			key = this.path.substring(index);
			this.path = this.path.substring(index);
		}
	}

	public ValueDollarPart apply(HashMap<String, CompoundTag> references) throws DollarException {
		CompoundTag parent = NbtHelper.getParentTagOrCreate(references.get(ingredientId), path);
		if(!parent.containsKey(key))
			return new ValueDollarPart();
		Tag tag = parent.getTag(this.key);
		if(NbtHelper.isString(tag))
			return new ValueDollarPart(tag.asString());
		else if(NbtHelper.isNumeric(tag))
			return new ValueDollarPart(((AbstractNumberTag) tag).getDouble());
		else
            return new ValueDollarPart(tag);
	}
}

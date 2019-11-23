package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public class ReferenceDollarPart implements DollarPart {
	public String ingredientId;
	public String path;
	public String key;

	public ReferenceDollarPart(String path) {
		int index = path.indexOf('.');
		if(index >= 0) {
			this.ingredientId = path.substring(0, index);
			this.path = path.substring(index + 1);
			index = this.path.lastIndexOf('.');
			if (index == -1) {
				this.key = this.path;
				this.path = "";
			} else {
				this.key = this.path.substring(index);
				this.path = this.path.substring(index);
			}
		} else {
			this.ingredientId = path;
			this.path = "";
			this.key = "";
		}
	}

	public ValueDollarPart apply(Map<String, CompoundTag> reference) throws DollarException {
		CompoundTag parent = NbtHelper.getParentTagOrCreate(reference.get(ingredientId), path);
		if(key.equals(""))
			return new ValueDollarPart(parent);
		if(!parent.contains(key))
			return new ValueDollarPart();
		Tag tag = parent.get(this.key);
		if(NbtHelper.isString(tag))
			return new ValueDollarPart(tag.asString());
		else if(NbtHelper.isNumeric(tag))
			return new ValueDollarPart(((AbstractNumberTag) tag).getDouble());
		else
            return new ValueDollarPart(tag);
	}
}

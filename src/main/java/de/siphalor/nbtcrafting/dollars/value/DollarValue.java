package de.siphalor.nbtcrafting.dollars.value;

import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

import java.util.Map;

public abstract class DollarValue<T> implements DollarPart {
	protected T value;

	public DollarValue(T value) {
		this.value = value;
	}

	@Override
	public DollarValue apply(Map<String, CompoundTag> reference) throws DollarException {
		return this;
	}

	public T getValue() {
		return value;
	}

	public String toString() {
		return value.toString();
	}
	public abstract boolean asBoolean();
	public abstract boolean isNumeric();
	public abstract Number asNumber();

	public static DollarValue from(Tag tag) {
		if(tag instanceof IntTag) {
			return new
		}
	}
}

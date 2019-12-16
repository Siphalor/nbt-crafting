package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.dollars.value.DollarValue;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public class ReferenceDollarPart implements DollarPart {
	private String mainKey;
	private String path;

	public ReferenceDollarPart(String mainKey, String path) {
		this.mainKey = mainKey;
		this.path = path;
	}

	@Override
	public DollarValue apply(Map<String, CompoundTag> reference) throws DollarException {
		if(!reference.containsKey(mainKey)) {
			throw new DollarException("Could not resolve reference to nbt tag '" + mainKey + "'");
		}
		return reference.get(mainKey).();
	}
}

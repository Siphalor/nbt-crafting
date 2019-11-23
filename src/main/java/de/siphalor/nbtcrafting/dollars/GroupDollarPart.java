package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Map;

public class GroupDollarPart implements DollarPart {
	public ArrayList<DollarPart> parts;
	public ArrayList<DollarOperator> operators;

	public GroupDollarPart() {
		parts = new ArrayList<>();
		operators = new ArrayList<>();
	}

	public ValueDollarPart apply(Map<String, CompoundTag> reference) throws DollarException {
		if(parts.size() <= 0)
			return new ValueDollarPart();
		Object value = parts.get(0).apply(reference).value;
		for(int i = 0; i < operators.size(); i++) {
			value = DollarOperator.executeOperator(value, operators.get(i), parts.get(i + 1).apply(reference).value);
		}
		return new ValueDollarPart(value);
	}
}

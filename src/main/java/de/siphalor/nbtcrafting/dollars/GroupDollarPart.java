package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupDollarPart implements DollarPart {
	public ArrayList<DollarPart> parts;
	public ArrayList<DollarOperator> operators;

	public GroupDollarPart() {
		parts = new ArrayList<>();
		operators = new ArrayList<>();
	}

	public ValueDollarPart apply(HashMap<String, CompoundTag> references) throws DollarException {
		if(parts.size() <= 0)
			return new ValueDollarPart();
		Object value = parts.get(0).apply(references);
		for(int i = 0; i < operators.size(); i++) {
			value = DollarOperator.executeOperator(value, operators.get(i), parts.get(i + 1).apply(references));
		}
		return new ValueDollarPart(value);
	}
}

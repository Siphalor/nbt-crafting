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
		Object value = parts.get(0).apply(references).value;
		for(int i = 0; i < operators.size(); i++) {
			Object other = parts.get(i + 1).apply(references).value;
            switch(operators.get(i)) {
	            case ADD:
	            	if(value == null) {
	            		if(other instanceof Double)
	            			value = (Double) other;
	            		else if(other instanceof String)
	            			value = (String) other;
		            } else if(value instanceof Double) {
	            		if(other instanceof Double) {
	            			value = (Double) value + (Double) other;
			            } else
			            	throw new DollarException("type conflict at '+'");
		            } else if(value instanceof String) {
	            		value = (String) value + other.toString();
		            }
	            	break;
	            case SUBTRACT:
	            	if(value instanceof Double && other instanceof Double) {
	            		value = (Double) value + (Double) other;
		            } else
		            	throw new DollarException("type conflict at '-'");
	            	break;
	            case MULTIPLY:
	            	if(value instanceof Double && other instanceof Double) {
	            		value = (Double) value * (Double) other;
		            } else
			            throw new DollarException("type conflict at '*'");
		            break;
	            case DIVIDE:
		            if(value instanceof Double && other instanceof Double) {
			            value = (Double) value / (Double) other;
		            } else
			            throw new DollarException("type conflict at '/'");
		            break;
            }
		}
		return new ValueDollarPart(value);
	}
}

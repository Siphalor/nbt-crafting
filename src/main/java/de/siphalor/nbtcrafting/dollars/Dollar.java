package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public class Dollar {
	protected final String key;
	protected final String lastKeyPart;
	protected DollarGroupPart expression;

	protected Dollar(String key) {
		this.key = key;
		this.lastKeyPart = key.substring(key.lastIndexOf('.') + 1);
	}

	public void apply(CompoundTag compoundTag, HashMap<String, CompoundTag> references) throws DollarException {
        CompoundTag parent = getParentTagOrCreate(compoundTag, key);
        Object value = expression.apply(references).value;
        if(value instanceof Tag)
        	compoundTag.put(lastKeyPart, (Tag) value);
        else if(value instanceof Double)
            compoundTag.putDouble(lastKeyPart, (Double) value);
        else if(value instanceof String)
        	compoundTag.putString(lastKeyPart, (String) value);
        else
        	throw new DollarException("Unknown type in expression");
	}

	public static Dollar parse(String key, String value) {
        Dollar dollar = new Dollar(key);
		try {
			dollar.expression = parse(value);
		} catch (DollarException e) {
			e.printStackTrace();
		}
		return dollar;
	}

	private static DollarGroupPart parse(String expression) throws DollarException {
		DollarGroupPart groupPart = new DollarGroupPart();
		groupPart.parts.add(parsePart(expression));
		while(expression != "") {
			switch(expression.charAt(0)) {
				case '+':
					groupPart.operators.add(DollarOperator.ADD);
					break;
				case '-':
					groupPart.operators.add(DollarOperator.SUBTRACT);
					break;
				case '*':
					groupPart.operators.add(DollarOperator.MULTIPLY);
					break;
				case '/':
					groupPart.operators.add(DollarOperator.DIVIDE);
					break;
			}
			expression.substring(1);
			groupPart.parts.add(parsePart(expression));
		}
		return groupPart;
	}

	private static DollarPart parsePart(String expression) throws DollarException {
		if(StringUtils.isWhitespace(expression.substring(0, 2))) {
			expression = expression.substring(1);
			if(expression == "") throw new DollarException("Illegal whitespacey statement");
		}
		if(expression.matches("^-?\\d*(.\\d+)?.*")) {
			ValueDollarPart value = new ValueDollarPart();
			int index = StringUtils.indexOfAny(expression.substring(1), "+-*/");
			value.value = Double.parseDouble(expression.substring(0, index == -1 ? expression.length() : index));
			expression = expression.substring(index);
			return value;
		}
		int index = StringUtils.indexOfAny(expression, "+-*/");
		if(!expression.substring(0, index).matches("[\\w\\d.]+"))
			throw new DollarException("Illegal statement: " + expression);
		DollarPart part = new ReferenceDollarPart(expression.substring(0, index));
		expression = expression.substring(index);
		return part;
	}

	public static CompoundTag getParentTagOrCreate(CompoundTag main, String path) throws DollarException {
		CompoundTag currentCompound = main;
		String[] pathParts = path.split(".");
		for (int i = 0; i < pathParts.length - 1; i++) {
			if(!currentCompound.containsKey(pathParts[i])) {
				currentCompound.put(pathParts[i], new CompoundTag());
				currentCompound = currentCompound.getCompound(pathParts[i]);
			} else if(NbtHelper.isCompound(currentCompound.getTag(pathParts[i]))) {
				currentCompound = currentCompound.getCompound(pathParts[i]);
			} else {
				throw new DollarException(path + " is not a valid path in " + main.asString());
			}
		}
		return currentCompound;
	}
}

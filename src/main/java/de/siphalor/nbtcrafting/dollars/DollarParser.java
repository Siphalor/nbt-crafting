package de.siphalor.nbtcrafting.dollars;

import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public final class DollarParser {
	private String expression;

	public static Dollar[] extractDollars(CompoundTag compoundTag) {
		ArrayList<Dollar> dollars = new ArrayList<>();
		NbtHelper.iterateTags(compoundTag, (path, tag) -> {
			if(NbtHelper.isString(tag) && !tag.asString().isEmpty()) {
				if(tag.asString().charAt(0) == '$') {
					dollars.add(new DollarParser().parse(path, tag.asString().substring(1)));
					return true;
				}
			}
			return false;
		});
		return dollars.toArray(new Dollar[0]);
	}

	public Dollar parse(String key, String value) {
        Dollar dollar = new Dollar(key);
		try {
			this.expression = value;
			dollar.expression = parse();
		} catch (DollarException e) {
			e.printStackTrace();
		}
		return dollar;
	}

	public static DollarPart parse(Reader reader, DollarOperator operator) throws IOException {
		reader.mark(1);
		int peek = reader.read();

		DollarValue dollarValue;
		if(peek == '"' || peek == '\'') {
			reader.reset();
			int start = reader.read();
			StringBuilder stringBuilder = new StringBuilder();
			boolean escaped = false;
			int character = -1;
			while (character != start) {
				character = reader.read();
				if(escaped) {
					stringBuilder.append(character);
					escaped = false;
				} else {
					if (character == '\\') {
						escaped = true;
					} else {
						stringBuilder.append(character);
					}
				}
			}
			dollarValue = new DollarValue.StringValue(stringBuilder.toString());
		}
	}

	private GroupDollarPart parse() throws DollarException {
		GroupDollarPart groupPart = new GroupDollarPart();
		groupPart.parts.add(parsePart());
		while(!expression.equals("")) {
			if(StringUtils.isWhitespace(expression.substring(0, 1))) {
				eatTo(1);
				if(expression.equals("")) throw new DollarException("Illegal whitespacey statement");
			}
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
				case ')':
					eatTo(1);
					return groupPart;
			}
            eatTo(1);
			groupPart.parts.add(parsePart());
		}
		return groupPart;
	}

	private DollarPart parsePart() throws DollarException {
		if(StringUtils.isWhitespace(expression.substring(0, 1))) {
            eatTo(1);
			if(expression.equals("")) throw new DollarException("Illegal whitespacey statement");
		}
		if(expression.matches("-?\\d*\\.?\\d+.*")) {
			int index = StringUtils.indexOfAny(expression.substring(1), " \n\t\r+-*/");
			if(index == -1)
				index = expression.length();
			ValueDollarPart value = new ValueDollarPart(Double.parseDouble(expression.substring(0, index)));
            eatTo(index);
			return value;
		}
		if(expression.charAt(0) == '(') {
			eatTo(1);
			return parse();
		}
		int index = StringUtils.indexOfAny(expression, " \n\t\r+-*/)");
		index = index < 0 ? expression.length() : index;
		if(!expression.substring(0, index).matches("[\\w\\d_.\\[\\]]+"))
			throw new DollarException("Illegal statement: " + expression);
		DollarPart part = new ReferenceDollarPart(expression.substring(0, index));
        eatTo(index);
		return part;
	}

	private void eatTo(int index) {
		expression = expression.substring(index);
	}
}

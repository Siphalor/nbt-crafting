package de.siphalor.nbtcrafting.dollars.value;

import de.siphalor.nbtcrafting.dollars.DollarParser;
import de.siphalor.nbtcrafting.dollars.DollarPart;

import java.io.IOException;

public class StringDollarValue extends DollarValue<String> {
	public StringDollarValue(String value) {
		super(value);
	}

	@Override
	public boolean asBoolean() {
		return !value.isEmpty();
	}

	@Override
	public boolean isNumeric() {
		return false;
	}

	@Override
	public Number asNumber() {
		return null;
	}

	public static class Factory implements DollarPart.Factory<StringDollarValue> {
		@Override
		public boolean matches(int character) {
			return character == '"' || character == '\'';
		}

		@Override
		public StringDollarValue parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws IOException {
			int character = dollarParser.eat();
			return new StringDollarValue(dollarParser.readTo(character));
		}
	}
}

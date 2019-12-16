package de.siphalor.nbtcrafting.dollars.value;

import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarParser;
import de.siphalor.nbtcrafting.dollars.DollarPart;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

import java.io.IOException;

public class NumberDollarValue<N extends Number> extends DollarValue<N> {
	public NumberDollarValue(N value) {
		super(value);
	}

	@Override
	public boolean asBoolean() {
		return value.doubleValue() != 0;
	}

	@Override
	public boolean isNumeric() {
		return true;
	}

	@Override
	public Number asNumber() {
		return value;
	}

	public static class Factory implements DollarPart.Factory<NumberDollarValue> {
		@Override
		public boolean matches(int character) {
			return isDigit(character);
		}

		@Override
		public NumberDollarValue parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
			int character = dollarParser.peek();
			StringBuilder stringBuilder = new StringBuilder();
			while(isDigit(character)) {
				dollarParser.eat();
				stringBuilder.appendCodePoint(character);
				character = dollarParser.peek();
			}
			return new NumberDollarValue<>(Integer.parseUnsignedInt(stringBuilder.toString()));
		}

		private boolean isDigit(int character) {
			return character >= 48 && character <= 57;
		}
	}
}

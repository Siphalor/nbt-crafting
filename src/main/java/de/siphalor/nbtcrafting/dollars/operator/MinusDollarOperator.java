package de.siphalor.nbtcrafting.dollars.operator;

import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarParser;
import de.siphalor.nbtcrafting.dollars.DollarPart;
import de.siphalor.nbtcrafting.dollars.value.DollarValue;
import de.siphalor.nbtcrafting.dollars.value.NumberDollarValue;
import de.siphalor.nbtcrafting.dollars.value.StringDollarValue;

import java.io.IOException;

public class MinusDollarOperator extends BinaryDollarOperator {
	public MinusDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	@Override
	public DollarValue apply(DollarValue first, DollarValue second) {
		if(first == null) {
			if(second.isNumeric()) {
				return new NumberDollarValue<>(-second.asNumber().doubleValue());
			} else {

			}
		}
		if(first.isNumeric() && second.isNumeric())
			return new NumberDollarValue<>(first.asNumber().doubleValue() - second.asNumber().doubleValue());
		else
			return new StringDollarValue(first.toString().replace(second.toString(), ""));
	}

	public static class Factory implements DollarPart.Factory {
		@Override
		public boolean matches(int character) {
			return character == '-';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
			dollarParser.eat();
			return new MinusDollarOperator(lastDollarPart, dollarParser.parse(priority));
		}
	}
}

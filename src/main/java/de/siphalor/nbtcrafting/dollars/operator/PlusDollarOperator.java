package de.siphalor.nbtcrafting.dollars.operator;

import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarParser;
import de.siphalor.nbtcrafting.dollars.DollarPart;
import de.siphalor.nbtcrafting.dollars.value.DollarValue;
import de.siphalor.nbtcrafting.dollars.value.NumberDollarValue;
import de.siphalor.nbtcrafting.dollars.value.StringDollarValue;

import java.io.IOException;

public class PlusDollarOperator extends BinaryDollarOperator {
	public PlusDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	@Override
	public DollarValue apply(DollarValue first, DollarValue second) {
		if(first.isNumeric() && second.isNumeric())
			return new NumberDollarValue<>(first.asNumber().doubleValue() + second.asNumber().doubleValue());
		return new StringDollarValue(first.toString() + second.toString());
	}

	public static class Factory implements DollarPart.Factory<PlusDollarOperator> {
		@Override
		public boolean matches(int character) {
			return character == '+';
		}

		@Override
		public PlusDollarOperator parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
			dollarParser.eat();
			if(lastDollarPart == null)
				throw new DollarException("Unexpected plus!");
			return new PlusDollarOperator(lastDollarPart, dollarParser.parse(priority));
		}
	}
}

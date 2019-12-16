package de.siphalor.nbtcrafting.dollars.operator;

import de.siphalor.nbtcrafting.dollars.DollarException;
import de.siphalor.nbtcrafting.dollars.DollarParser;
import de.siphalor.nbtcrafting.dollars.DollarPart;
import de.siphalor.nbtcrafting.dollars.value.DollarValue;
import de.siphalor.nbtcrafting.dollars.value.NumberDollarValue;
import de.siphalor.nbtcrafting.dollars.value.StringDollarValue;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class AsteriskDollarOperator extends BinaryDollarOperator {
	public AsteriskDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	@Override
	public DollarValue apply(DollarValue first, DollarValue second) {
		if(first.isNumeric()) {
			if(second.isNumeric())
				return new NumberDollarValue<>(first.asNumber().doubleValue() * second.asNumber().doubleValue());
			return new StringDollarValue(StringUtils.repeat(second.toString(), first.asNumber().intValue()));
		} else if(second.isNumeric()) {
			return new StringDollarValue(StringUtils.repeat(first.toString(), second.asNumber().intValue()));
		}
		return null;
	}

	public static class Factory implements DollarPart.Factory {
		@Override
		public boolean matches(int character) {
			return character == '*';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
			if(lastDollarPart == null) {
				throw new DollarException("Unexpected asterisk!");
			}
			dollarParser.eat();
			return new AsteriskDollarOperator(lastDollarPart, dollarParser.parse(priority));
		}
	}
}

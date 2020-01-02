package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;

public class CombinationDollarPartDeserializer implements DollarPart.UnaryDeserializer {
	@Override
	public boolean matches(int character, DollarParser dollarParser) {
		return character == '(';
	}

	@Override
	public DollarPart parse(DollarParser dollarParser) {
		dollarParser.skip();
		return dollarParser.parseTo(')');
	}
}

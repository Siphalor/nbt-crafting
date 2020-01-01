package de.siphalor.nbtcrafting.dollars;

import java.io.IOException;

public class CombinationDollarPartDeserializer implements DollarPart.Deserializer {
	@Override
	public boolean matches(int character, DollarParser dollarParser, boolean hasOtherPart) {
		return character == '(';
	}

	@Override
	public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException, IOException {
		if(lastDollarPart != null) {
			throw new DollarException("Unexpected parenthesis!");
		}
		dollarParser.eat();
		return dollarParser.parseTo(')');
	}
}

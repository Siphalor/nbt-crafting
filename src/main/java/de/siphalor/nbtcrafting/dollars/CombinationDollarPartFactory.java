package de.siphalor.nbtcrafting.dollars;

import java.io.IOException;

public class CombinationDollarPartFactory implements DollarPart.Factory<DollarPart> {
	@Override
	public boolean matches(int character) {
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

package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class DifferenceDollarOperator extends BinaryDollarOperator {
	private DifferenceDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	public static DollarPart of(DollarPart first, DollarPart second) throws DollarException {
		DollarPart instance = new DifferenceDollarOperator(first, second);
		if(first.isConstant() && second.isConstant()) {
			return ValueDollarPart.of(instance.evaluate(null));
		}
		return instance;
	}

	@Override
	public Object apply(Object first, Object second) {
		if(first instanceof Number && second instanceof Number)
			return NumberUtil.difference((Number) first, (Number) second);
		else
			return first.toString().replace(second.toString(), "");
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '-';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarException {
			dollarParser.skip();
			return DifferenceDollarOperator.of(lastDollarPart, dollarParser.parse(priority));
		}
	}
}

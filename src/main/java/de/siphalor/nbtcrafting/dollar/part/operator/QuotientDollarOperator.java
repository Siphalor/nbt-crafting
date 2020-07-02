package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;
import org.apache.commons.lang3.StringUtils;

public class QuotientDollarOperator extends BinaryDollarOperator {
	private QuotientDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	public static DollarPart of(DollarPart first, DollarPart second) throws DollarException {
		DollarPart instance = new QuotientDollarOperator(first, second);
		if (first.isConstant() && second.isConstant()) {
			return ValueDollarPart.of(instance.evaluate(null));
		}
		return instance;
	}

	@Override
	public Object apply(Object first, Object second) {
		if (first instanceof Number && second instanceof Number) {
			return NumberUtil.quotient((Number) first, (Number) second);
		} else {
			return StringUtils.countMatches(first.toString(), second.toString());
		}
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '/';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarDeserializationException {
			dollarParser.skip();
			return new QuotientDollarOperator(lastDollarPart, dollarParser.parse(priority));
		}
	}
}

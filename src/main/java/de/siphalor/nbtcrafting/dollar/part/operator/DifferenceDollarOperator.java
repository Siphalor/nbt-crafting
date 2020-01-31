package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class DifferenceDollarOperator extends BinaryDollarOperator {
	private DifferenceDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	public static DollarPart of(DollarPart first, DollarPart second) throws DollarDeserializationException {
		DollarPart instance = new DifferenceDollarOperator(first, second);
		if(first.isConstant() && second.isConstant()) {
			try {
				return ValueDollarPart.of(instance.evaluate(null));
			} catch (DollarEvaluationException e) {
				throw new DollarDeserializationException(e);
			}
		}
		return instance;
	}

	@Override
	public Object apply(Object first, Object second) {
		if((first instanceof Number || first == null) && (second instanceof Number || second == null))
			return NumberUtil.difference((Number) first, (Number) second);
		else
			if(first == null || second == null)
				return first;
			return first.toString().replace(second.toString(), "");
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '-';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarDeserializationException {
			dollarParser.skip();
			return DifferenceDollarOperator.of(lastDollarPart, dollarParser.parse(priority));
		}
	}
}

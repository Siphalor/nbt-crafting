package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class SumDollarOperator extends BinaryDollarOperator {
	private SumDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	public static DollarPart of(DollarPart first, DollarPart second) throws DollarDeserializationException {
		DollarPart instance = new SumDollarOperator(first, second);
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
			return NumberUtil.sum((Number) first, (Number) second);
		return first + "" + second;
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '+';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarDeserializationException {
			dollarParser.skip();
			if(lastDollarPart == null)
				throw new DollarDeserializationException("Unexpected plus!");
			return SumDollarOperator.of(lastDollarPart, dollarParser.parse(priority));
		}
	}
}

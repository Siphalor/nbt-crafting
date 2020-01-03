package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;

public class InverseDollarOperator extends UnaryDollarOperator {
	private InverseDollarOperator(DollarPart dollarPart) {
		super(dollarPart);
	}

	public static DollarPart of(DollarPart dollarPart) throws DollarDeserializationException {
		DollarPart instance = new InverseDollarOperator(dollarPart);
		if(dollarPart.isConstant()) {
			try {
				return ValueDollarPart.of(instance.evaluate(null));
			} catch (DollarEvaluationException e) {
				throw new DollarDeserializationException(e);
			}
		}
		return instance;
	}

	@Override
	public Object evaluate(Object value) {
		if(value instanceof Number) {
			if(value instanceof Double) {
				return -(Double) value;
			} else if(value instanceof Float) {
				return -(Float) value;
			} else if(value instanceof Long) {
				return -(Long) value;
			} else if(value instanceof Integer) {
				return -(Integer) value;
			} else if(value instanceof Short) {
				return -(Short) value;
			}
		}
		return 0;
	}

	public static class Deserializer implements DollarPart.UnaryDeserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '-';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser) throws DollarDeserializationException {
			return InverseDollarOperator.of(dollarParser.parseUnary());
		}
	}
}

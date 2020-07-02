package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;

import java.util.Map;

public class ConditionDollarOperator implements DollarPart {
	private final DollarPart condition;
	private final DollarPart thenPart;
	private final DollarPart elsePart;

	public ConditionDollarOperator(DollarPart condition, DollarPart thenPart, DollarPart elsePart) {
		this.condition = condition;
		this.thenPart = thenPart;
		this.elsePart = elsePart;
	}

	@Override
	public Object evaluate(Map<String, Object> reference) throws DollarEvaluationException {
		if (DollarUtil.asBoolean(condition.evaluate(reference))) {
			return thenPart.evaluate(reference);
		}
		return elsePart.evaluate(reference);
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '?';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarDeserializationException {
			dollarParser.skip();
			DollarPart thenPart = dollarParser.parseTo(':');
			DollarPart elsePart = dollarParser.parse(priority);

			if (lastDollarPart.isConstant()) {
				try {
					if (DollarUtil.asBoolean(lastDollarPart.evaluate(null))) {
						return thenPart;
					} else {
						return elsePart;
					}
				} catch (DollarEvaluationException e) {
					throw new DollarDeserializationException(e);
				}
			}

			return new ConditionDollarOperator(lastDollarPart, thenPart, elsePart);
		}
	}
}

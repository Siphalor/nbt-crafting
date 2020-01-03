package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;
import org.apache.commons.lang3.StringUtils;

public class ProductDollarOperator extends BinaryDollarOperator {
	public ProductDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	public static DollarPart of(DollarPart first, DollarPart second) throws DollarException {
		DollarPart instance = new ProductDollarOperator(first, second);
		if(first.isConstant() && second.isConstant()) {
			return ValueDollarPart.of(instance.evaluate(null));
		}
		return null;
	}

	@Override
	public Object apply(Object first, Object second) {
		if(first instanceof Number) {
			if(second instanceof Number)
				return NumberUtil.product((Number) first, (Number) second);
			return StringUtils.repeat(second.toString(), ((Number) first).intValue());
		} else if(second instanceof Number) {
			return StringUtils.repeat(first.toString(), ((Number) second).intValue());
		}
		return null;
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '*';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarDeserializationException {
			if(lastDollarPart == null) {
				throw new DollarDeserializationException("Unexpected asterisk!");
			}
			dollarParser.skip();
			return new ProductDollarOperator(lastDollarPart, dollarParser.parse(priority));
		}
	}
}

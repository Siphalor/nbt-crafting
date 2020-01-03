package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public class CastDollarOperator implements DollarPart {
	private final DollarPart dollarPart;
	private final int typeId;

	private CastDollarOperator(DollarPart dollarPart, int typeId) {
		this.dollarPart = dollarPart;
		this.typeId = typeId;
	}

	public static DollarPart of(DollarPart dollarPart, int typeId) throws DollarException {
		DollarPart instance = new CastDollarOperator(dollarPart, typeId);
		if(dollarPart.isConstant()) {
			return ValueDollarPart.of(instance.evaluate(null));
		}
		return instance;
	}

	@Override
	public Object evaluate(Map<String, CompoundTag> reference) throws DollarEvaluationException {
		Object value = dollarPart.evaluate(reference);
		switch(typeId) {
			case 'd':
				if(value instanceof Number)
					return ((Number) value).doubleValue();
				return 0D;
			case 'f':
				if(value instanceof Number)
					return ((Number) value).floatValue();
				return 0F;
			case 'b':
			case 'c':
			case 'C':
				if(value instanceof Number)
					return ((Number) value).byteValue();
				return (byte) 0;
			case 's':
				if(value instanceof Number)
					return ((Number) value).shortValue();
				return (short) 0;
			case 'i':
				if(value instanceof Number)
					return ((Number) value).intValue();
				return 0;
			case 'l':
				if(value instanceof Number)
					return ((Number) value).longValue();
				return 0L;
			case '"':
			case '\'':
			case 'a':
				return value.toString();
			default:
				return null;
		}
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '#';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) {
			dollarParser.skip();
			return new CastDollarOperator(lastDollarPart, dollarParser.eat());
		}
	}
}

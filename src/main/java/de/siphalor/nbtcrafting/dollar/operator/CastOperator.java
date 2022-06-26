package de.siphalor.nbtcrafting.dollar.operator;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.token.DollarToken;

public class CastOperator implements BinaryOperator {
	@Override
	public int getPrecedence() {
		return 20;
	}

	@Override
	public Object apply(Object left, Object right, @NotNull Function<String, Object> referenceResolver) throws DollarEvaluationException {
		left = tryResolveReference(left, referenceResolver);
		String typeId = assertParameterType(right, 1, String.class);
		switch (typeId) {
			case "string":
			case "S":
			case "a":
				return DollarUtil.asString(assertNotNull(left, 0));
			case "b":
			case "byte":
			case "c":
				return asNumber(left, Byte::parseByte, Number::byteValue);
			case "s":
			case "short":
				return asNumber(left, Short::parseShort, Number::shortValue);
			case "i":
			case "int":
			case "integer":
				return asNumber(left, Integer::parseInt, Number::intValue);
			case "l":
			case "long":
				return asNumber(left, Long::parseLong, Number::longValue);
			case "f":
			case "float":
				return asNumber(left, Float::parseFloat, Number::floatValue);
			case "d":
			case "double":
				return asNumber(left, Double::parseDouble, Number::doubleValue);
			case "B":
			case "bool":
			case "boolean":
				return DollarUtil.asBoolean(left);
			default:
				throw new DollarEvaluationException("Unknown type " + typeId);
		}
	}

	public <N extends Number> N asNumber(Object value, Function<String, N> parse, Function<Number, N> cast) throws DollarEvaluationException {
		value = assertNotNull(value, 0);
		if (value instanceof Number) {
			return cast.apply((Number) value);
		} else if (value instanceof String) {
			return parse.apply((String) value);
		} else {
			throw new IllegalArgumentException("Parameter 0 to " + this.getClass().getSimpleName() + " is not a number or a string");
		}
	}

	@Override
	public DollarToken.Type getTokenType() {
		return DollarToken.Type.INFIX_OPERATOR;
	}
}

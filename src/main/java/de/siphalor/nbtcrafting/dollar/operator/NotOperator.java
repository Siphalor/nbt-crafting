package de.siphalor.nbtcrafting.dollar.operator;

import java.util.function.Function;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarUtil;

public class NotOperator implements UnaryPrefixOperator {
	@Override
	public int getPrecedence() {
		return 20;
	}

	@Override
	public Object apply(Object value, Function<String, Object> referenceResolver) throws DollarEvaluationException {
		if (value == null) {
			return false;
		}
		value = tryResolveReference(value, referenceResolver);
		return !DollarUtil.asBoolean(value);
	}
}

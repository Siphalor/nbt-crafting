package de.siphalor.nbtcrafting.dollar.operator;

import java.util.function.Function;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class NegateOperator implements UnaryPrefixOperator {
	@Override
	public int getPrecedence() {
		return 10;
	}

	@Override
	public Object apply(Object value, Function<String, Object> referenceResolver) throws DollarEvaluationException {
		assertNotNull(value, 0);
		value = tryResolveReference(value, referenceResolver);
		return NumberUtil.product(assertParameterType(value, 0, Number.class), -1);
	}
}

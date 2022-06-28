package de.siphalor.nbtcrafting.dollar.instruction;

import java.util.function.Function;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarUtil;

public class NotInstruction implements UnaryPrefixInstruction {
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

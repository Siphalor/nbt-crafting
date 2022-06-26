package de.siphalor.nbtcrafting.dollar.operator;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.token.DollarToken;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class SubtractOperator implements BinaryOperator {
	@Override
	public @Nullable Object apply(@Nullable Object left, @Nullable Object right, @NotNull Function<String, Object> referenceResolver) throws DollarEvaluationException {
		left = assertNotNull(left, 0);
		left = tryResolveReference(left, referenceResolver);
		right = assertNotNull(right, 1);
		right = tryResolveReference(right, referenceResolver);
		if (left instanceof Number) {
			return NumberUtil.difference((Number) left, assertParameterType(right, 1, Number.class));
		} else if (left instanceof String) {
			return ((String) left).replace(DollarUtil.asString(right), "");
		}
		exceptParameterType(left, 0, Number.class, String.class);
		return null; // unreachable
	}

	@Override
	public int getPrecedence() {
		return 40;
	}

	@Override
	public DollarToken.@NotNull Type getTokenType() {
		return DollarToken.Type.INFIX_OPERATOR;
	}
}

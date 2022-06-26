package de.siphalor.nbtcrafting.dollar.operator;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.token.DollarToken;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class DivideOperator implements BinaryOperator {
	@Override
	public @Nullable Object apply(@Nullable Object left, @Nullable Object right, @NotNull Function<String, Object> referenceResolver) throws DollarEvaluationException {
		return NumberUtil.quotient(
				assertParameterType(left, 0, Number.class),
				assertParameterType(right, 1, Number.class)
		);
	}

	@Override
	public int getPrecedence() {
		return 30;
	}

	@Override
	public DollarToken.@NotNull Type getTokenType() {
		return DollarToken.Type.INFIX_OPERATOR;
	}
}

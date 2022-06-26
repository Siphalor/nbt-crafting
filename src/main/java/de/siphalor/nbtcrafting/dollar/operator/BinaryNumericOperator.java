package de.siphalor.nbtcrafting.dollar.operator;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;

public class BinaryNumericOperator implements BinaryOperator {
	private final BiFunction<Number, Number, Number> function;
	private final int precedence;

	public BinaryNumericOperator(BiFunction<Number, Number, Number> function, int precedence) {
		this.function = function;
		this.precedence = precedence;
	}

	@Override
	public int getPrecedence() {
		return precedence;
	}

	@Override
	public @Nullable Object apply(@Nullable Object left, @Nullable Object right, @NotNull Function<String, Object> referenceResolver) throws DollarEvaluationException {
		return function.apply(assertParameterType(left, 0, Number.class), assertParameterType(right, 1, Number.class));
	}
}

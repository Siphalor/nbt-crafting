package de.siphalor.nbtcrafting.dollar.instruction;

import java.util.Stack;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;
import de.siphalor.nbtcrafting.dollar.DollarUtil;

public interface Instruction {
	int getPrecedence();

	void apply(Stack<Object> stack, DollarRuntime.Context context) throws DollarEvaluationException;

	default @NotNull Object tryResolveReference(@NotNull Object parameter, Function<String, Object> referenceResolver) throws DollarEvaluationException {
		return DollarUtil.tryResolveReference(this.getClass().getSimpleName(), parameter, referenceResolver);
	}

	@Contract("null, _ -> fail")
	default <T> T assertNotNull(@Nullable T parameter, int index) throws DollarEvaluationException {
		return DollarUtil.assertNotNull(this.getClass().getSimpleName(), parameter, index);
	}

	default String assertStringOrLiteral(@Nullable Object parameter, int index) throws DollarEvaluationException {
		return DollarUtil.assertStringOrLiteral(this.getClass().getSimpleName(), parameter, index);
	}

	default <T> T assertParameterType(@Nullable Object parameter, int index, Class<T> type) throws DollarEvaluationException {
		return DollarUtil.assertParameterType(this.getClass().getSimpleName(), parameter, index, type);
	}

	@Contract("_, _, _ -> fail")
	default void exceptParameterType(@Nullable Object parameter, int index, Class<?> @NotNull ... types) throws DollarEvaluationException {
		DollarUtil.exceptParameterType(this.getClass().getSimpleName(), parameter, index, types);
	}
}

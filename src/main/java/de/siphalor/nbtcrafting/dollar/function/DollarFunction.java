package de.siphalor.nbtcrafting.dollar.function;

import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;
import de.siphalor.nbtcrafting.dollar.DollarUtil;

public abstract class DollarFunction {
	protected final String name;

	public DollarFunction(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract boolean isParameterCountCorrect(int parameterCount);

	public abstract Object call(Object[] parameters, DollarRuntime.Context context) throws DollarEvaluationException;


	protected @NotNull Object tryResolveReference(@NotNull Object parameter, Function<String, Object> referenceResolver) throws DollarEvaluationException {
		return DollarUtil.tryResolveReference(getName(), parameter, referenceResolver);
	}

	@Contract("null, _ -> fail")
	protected  <T> T assertNotNull(@Nullable T parameter, int index) throws DollarEvaluationException {
		return DollarUtil.assertNotNull(getName(), parameter, index);
	}

	protected String assertStringOrLiteral(@Nullable Object parameter, int index) throws DollarEvaluationException {
		return DollarUtil.assertStringOrLiteral(getName(), parameter, index);
	}

	protected  <T> T assertParameterType(@Nullable Object parameter, int index, Class<T> type) throws DollarEvaluationException {
		return DollarUtil.assertParameterType(getName(), parameter, index, type);
	}

	@Contract("_, _, _ -> fail")
	protected void exceptParameterType(@Nullable Object parameter, int index, Class<?> @NotNull ... types) throws DollarEvaluationException {
		DollarUtil.exceptParameterType(getName(), parameter, index, types);
	}
}

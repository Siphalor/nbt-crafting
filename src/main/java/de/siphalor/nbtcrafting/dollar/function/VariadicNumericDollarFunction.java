package de.siphalor.nbtcrafting.dollar.function;

import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.ListTag;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarRuntime;

public class VariadicNumericDollarFunction extends DollarFunction {
	private final Number start;
	private final BiFunction<Number, Number, Number> reduce;

	public VariadicNumericDollarFunction(String name, Number start, BiFunction<Number, Number, Number> reduce) {
		super(name);
		this.start = start;
		this.reduce = reduce;
	}

	@Override
	public boolean isParameterCountCorrect(int parameterCount) {
		return parameterCount > 0;
	}

	@Override
	public Object call(Object[] parameters, DollarRuntime.Context context) throws DollarEvaluationException {
		Number value = start;

		for (int p = 0; p < parameters.length; p++) {
			Object parameter = parameters[p];
			assertNotNull(parameter, p);
			parameter = tryResolveReference(parameters[p], context::resolveReference);
			if (parameter instanceof Number) {
				value = reduce.apply(value, ((Number) parameter));
			} else if (parameter instanceof List) {
				for (Object element : ((List<?>) parameter)) {
					element = tryResolveReference(element, context::resolveReference);
					if (element instanceof AbstractNumberTag) {
						value = reduce.apply(value, ((AbstractNumberTag) element).getNumber());
					} else if (element instanceof Number) {
						value = reduce.apply(value, ((Number) element));
					} else {
						exceptParameterType(element, p, Number.class);
					}
				}
			} else {
				exceptParameterType(parameter, p, Number.class, ListTag.class);
			}
		}

		return value;
	}
}

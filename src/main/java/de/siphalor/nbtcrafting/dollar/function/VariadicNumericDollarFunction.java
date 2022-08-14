package de.siphalor.nbtcrafting.dollar.function;

import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.nbt.AbstractNumberTag;

import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.exception.IllegalDollarFunctionParameterException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;
import de.siphalor.nbtcrafting.util.NumberUtil;

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
	public void checkParameter(int index, Object parameter) throws IllegalDollarFunctionParameterException {
		if (!(parameter instanceof Number) && !(parameter instanceof List) && parameter != null) {
			exceptParameterType(parameter, index, Number.class, List.class, null);
		}
	}

	@Override
	public Object call(DollarPart[] parameters, ReferenceResolver referenceResolver) throws DollarEvaluationException, IllegalDollarFunctionParameterException {
		Number value = start;

		for (int p = 0; p < parameters.length; p++) {
			Object parameter = parameters[p].evaluate(referenceResolver);
			if (parameter == null) {
				parameter = NumberUtil.denullify(null);
			}
			if (parameter instanceof Number) {
				value = reduce.apply(value, ((Number) parameter));
			} else if (parameter instanceof List) {
				for (Object element : ((List<?>) parameter)) {
					if (element instanceof AbstractNumberTag) {
						value = reduce.apply(value, ((AbstractNumberTag) element).getNumber());
					} else if (element instanceof Number) {
						value = reduce.apply(value, ((Number) element));
					} else {
						exceptParameterType(element, p, Number.class);
					}
				}
			} else {
				exceptParameterType(parameter, p, Number.class, List.class);
			}
		}

		return value;
	}
}

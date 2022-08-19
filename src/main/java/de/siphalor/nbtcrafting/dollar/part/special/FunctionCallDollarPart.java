package de.siphalor.nbtcrafting.dollar.part.special;

import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.exception.IllegalDollarFunctionParameterException;
import de.siphalor.nbtcrafting.dollar.function.DollarFunction;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ConstantDollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class FunctionCallDollarPart implements DollarPart {
	private final DollarFunction function;
	private final DollarPart[] parameters;

	private FunctionCallDollarPart(DollarFunction function, DollarPart[] parameters) {
		this.function = function;
		this.parameters = parameters;
	}

	public static DollarPart of(DollarFunction function, DollarPart... parameters) throws DollarDeserializationException {
		FunctionCallDollarPart instance = new FunctionCallDollarPart(function, parameters);
		int p = 0;
		for (DollarPart parameter : parameters) {
			if (parameter instanceof ConstantDollarPart) {
				try {
					function.checkParameter(p, ((ConstantDollarPart) parameter).getConstantValue());
				} catch (IllegalDollarFunctionParameterException e) {
					throw new DollarDeserializationException("Invalid parameter for function " + function.getName() + " at position " + p + ": " + e.getMessage(), e);
				}
			} else {
				return instance;
			}
			p++;
		}
		try {
			return ValueDollarPart.of(instance.evaluate(null));
		} catch (DollarEvaluationException e) {
			throw new DollarDeserializationException("Failed to short-circuit dollar function call", e);
		}
	}

	@Override
	public Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		try {
			return function.call(referenceResolver, parameters);
		} catch (IllegalDollarFunctionParameterException e) {
			throw new DollarEvaluationException("Invalid parameter for function " + function.getName() + ": " + e.getMessage(), e);
		}
	}
}

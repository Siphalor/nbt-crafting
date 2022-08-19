package de.siphalor.nbtcrafting.dollar.function;

import org.jetbrains.annotations.Contract;

import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.exception.IllegalDollarFunctionParameterException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public abstract class DollarFunction {
	protected final String name;

	public DollarFunction(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract boolean isParameterCountCorrect(int parameterCount);

	/**
	 * Checks if the given parameter is of the expected type.
	 * This is used for compile time type checking, if the parameters are constant.
	 * This allows to fail early if the parameters are not of the expected type.
	 *
	 * @param index the index of the parameter
	 * @param parameter the value of the parameter
	 * @throws IllegalDollarFunctionParameterException if the parameter is not of the expected type
	 */
	public abstract void checkParameter(int index, Object parameter) throws IllegalDollarFunctionParameterException;

	public Object callDirect(ReferenceResolver referenceResolver, Object... parameters) throws DollarEvaluationException, IllegalDollarFunctionParameterException {
		DollarPart[] dollarParts = new DollarPart[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			dollarParts[i] = ValueDollarPart.of(parameters[i]);
		}
		return call(referenceResolver, dollarParts);
	}
	public abstract Object call(ReferenceResolver referenceResolver, DollarPart... parameters) throws DollarEvaluationException, IllegalDollarFunctionParameterException;

	// Utility methods for checking parameters
	@Contract("_, _, _ -> fail")
	protected void exceptParameterType(Object parameter, Object parameterIdentifier, Class<?>... parameterClasses) throws IllegalDollarFunctionParameterException {
		if (parameter == null) {
			throw new IllegalDollarFunctionParameterException("Parameter " + parameterIdentifier + " to function " + name + " is null, but must not be null");
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Parameter ").append(parameterIdentifier.toString()).append(" to function ").append(getName()).append(" is of type ").append(parameter.getClass().getSimpleName());
		sb.append(", but expected to be one of ");
		for (int i = 0; i < parameterClasses.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			if (parameterClasses[i] == null) {
				sb.append("null");
			} else {
				sb.append(parameterClasses[i].getSimpleName());
			}
		}
		throw new IllegalDollarFunctionParameterException(sb.toString());
	}
}

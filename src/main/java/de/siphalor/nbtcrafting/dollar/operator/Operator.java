package de.siphalor.nbtcrafting.dollar.operator;

import java.util.Stack;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.Literal;
import de.siphalor.nbtcrafting.dollar.token.DollarToken;

public interface Operator {
	int getPrecedence();

	void apply(Stack<Object> stack, Function<String, Object> referenceResolver) throws DollarEvaluationException;

	DollarToken.Type getTokenType();
	default @NotNull Object tryResolveReference(@NotNull Object parameter, Function<String, Object> referenceResolver) throws DollarEvaluationException {
		if (parameter instanceof Literal) {
			Object value = referenceResolver.apply(((Literal) parameter).value);
			if (value == null) {
				throw new DollarEvaluationException("Reference '" + ((Literal) parameter).value + "' could not be resolved");
			}
			return value;
		}
		return parameter;
	}

	@Contract("null, _ -> fail")
	default <T> T assertNotNull(@Nullable T parameter, int index) throws DollarEvaluationException {
		if (parameter == null) {
			throw new DollarEvaluationException("Parameter " + index + " to " + this.getClass().getSimpleName() + " is null");
		}
		return parameter;
	}

	default String assertStringOrLiteral(@Nullable Object parameter, int index) throws DollarEvaluationException {
		parameter = assertNotNull(parameter, index);
		if (parameter instanceof String) {
			return (String) parameter;
		} else if (parameter instanceof Literal) {
			return ((Literal) parameter).value;
		}
		exceptParameterType(parameter, index, String.class, Literal.class);
		return null; // unreachable
	}

	default <T> T assertParameterType(@Nullable Object parameter, int index, Class<T> type) throws DollarEvaluationException {
		parameter = assertNotNull(parameter, index);
		if (!type.isInstance(parameter)) {
			exceptParameterType(parameter, index, type);
		}
		//noinspection unchecked
		return (T) parameter;
	}

	@Contract("_, _, _ -> fail")
	default void exceptParameterType(@NotNull Object parameter, int index, Class<?> @NotNull ... type) throws DollarEvaluationException {
		StringBuilder sb = new StringBuilder("Parameter ");
		sb.append(index);
		sb.append(" to ");
		sb.append(this.getClass().getSimpleName());
		if (type.length == 1) {
sb.append(" is not a ");
			sb.append(type[0].getSimpleName());
		} else {
			sb.append(" is not a ");
			sb.append(type[0].getSimpleName());
			for (int i = 1; i < type.length; i++) {
				sb.append(" or a ");
				sb.append(type[i].getSimpleName());
			}
		}
		sb.append(": ");
		sb.append(parameter);
		throw new DollarEvaluationException(sb.toString());
	}
}

/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.nbtcrafting.dollar;

import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DollarUtil {
	public static boolean asBoolean(Object o) {
		if (o instanceof Boolean) {
			return (boolean) o;
		}
		if (o instanceof Number) {
			return ((Number) o).intValue() != 0;
		}
		if (o instanceof String) {
			return !o.equals("");
		}
		return o != null;
	}

	public static String asString(Object o) {
		if (o == null) {
			return "<null>";
		}
		return o.toString();
	}

	public static @NotNull Object tryResolveReference(@NotNull String context, @NotNull Object parameter, Function<String, Object> referenceResolver) throws DollarEvaluationException {
		if (parameter instanceof Literal) {
			Object value = referenceResolver.apply(((Literal) parameter).value);
			if (value == null) {
				throw new DollarEvaluationException("Reference '" + ((Literal) parameter).value + "' in " + context + " could not be resolved");
			}
			return value;
		}
		return parameter;
	}

	@Contract("_, null, _ -> fail")
	public static  <T> T assertNotNull(@NotNull String context, @Nullable T parameter, int index) throws DollarEvaluationException {
		if (parameter == null) {
			throw new DollarEvaluationException("Parameter " + index + " to " + context + " is null");
		}
		return parameter;
	}

	public static String assertStringOrLiteral(@NotNull String context, @Nullable Object parameter, int index) throws DollarEvaluationException {
		parameter = assertNotNull(context, parameter, index);
		if (parameter instanceof String) {
			return (String) parameter;
		} else if (parameter instanceof Literal) {
			return ((Literal) parameter).value;
		}
		exceptParameterType(context, parameter, index, String.class, Literal.class);
		return null; // unreachable
	}

	public static <T> T assertParameterType(@NotNull String context, @Nullable Object parameter, int index, Class<T> type) throws DollarEvaluationException {
		parameter = assertNotNull(context, parameter, index);
		if (!type.isInstance(parameter)) {
			exceptParameterType(context, parameter, index, type);
		}
		//noinspection unchecked
		return (T) parameter;
	}

	@Contract("_, _, _, _ -> fail")
	public static void exceptParameterType(@NotNull String context, @Nullable Object parameter, int index, Class<?> @NotNull ... types) throws DollarEvaluationException {
		StringBuilder sb = new StringBuilder("Parameter ");
		sb.append(index);
		sb.append(" to ");
		sb.append(context);
		if (types.length == 1) {
			sb.append(" is not a ");
			sb.append(types[0] == null ? "null" : types[0].getSimpleName());
		} else {
			sb.append(" is not a ");
			sb.append(types[0].getSimpleName());
			for (int i = 1; i < types.length; i++) {
				sb.append(" or a ");
				sb.append(types[i] == null ? "null" : types[i].getSimpleName());
			}
		}
		sb.append(": ");
		sb.append(parameter);
		throw new DollarEvaluationException(sb.toString());
	}
}

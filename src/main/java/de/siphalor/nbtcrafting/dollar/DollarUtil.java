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

import java.util.Stack;
import java.util.function.Function;

import de.siphalor.nbtcrafting.dollar.operator.Operator;

public class DollarUtil {
	public static boolean asBoolean(Object o) {
		if (o instanceof Number) {
			return ((Number) o).intValue() != 0;
		}
		if (o instanceof String) {
			return !o.equals("");
		}
		return false;
	}

	public static String asString(Object o) {
		if (o == null) {
			return "<null>";
		}
		return o.toString();
	}

	public static Object evaluate(Object[] expression, Function<String, Object> referenceResolver) throws DollarEvaluationException {
		Stack<Object> stack = new Stack<>();
		for (Object instruction : expression) {
			if (instruction instanceof Operator) {
				((Operator) instruction).apply(stack, referenceResolver);
			} else {
				stack.push(instruction);
			}
		}
		return stack.pop();
	}
}

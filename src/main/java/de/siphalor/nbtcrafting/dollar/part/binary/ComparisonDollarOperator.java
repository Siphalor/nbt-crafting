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

package de.siphalor.nbtcrafting.dollar.part.binary;

import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class ComparisonDollarOperator extends BinaryDollarOperator {
	private final Type type;

	private ComparisonDollarOperator(Type type, DollarPart first, DollarPart second) {
		super(first, second);
		this.type = type;
	}

	@Override
	public Object apply(Object first, Object second) throws DollarEvaluationException {
		if ((first instanceof Number || first == null) && (second instanceof Number || second == null)) {
			Number firstNumber = NumberUtil.denullify((Number) first);
			Number secondNumber = NumberUtil.denullify((Number) second);

			return type.matches(Double.compare(firstNumber.doubleValue(), secondNumber.doubleValue()));
		} else if (first instanceof String && second instanceof String) {
			return type.matches(first.toString().compareTo(second.toString()));
		}
		if (type == Type.EQUAL) {
			return DollarUtil.equals(first, second);
		} else if (type == Type.NOT_EQUAL) {
			return !DollarUtil.equals(first, second);
		}

		throw new DollarEvaluationException("Cannot compare " + first + " with " + second + " with operator " + type.operator());
	}

	public static DollarPart of(Type type, DollarPart first, DollarPart second) throws DollarDeserializationException {
		return shortCircuitConstant(new ComparisonDollarOperator(type, first, second));
	}

	public enum Type {
		EQUAL, NOT_EQUAL,
		LESS, LESS_OR_EQUAL,
		GREATER, GREATER_OR_EQUAL;

		public static Type fromString(String string) {
			switch (string) {
				case "==":
					return EQUAL;
				case "!=":
					return NOT_EQUAL;
				case "<":
					return LESS;
				case "<=":
					return LESS_OR_EQUAL;
				case ">":
					return GREATER;
				case ">=":
					return GREATER_OR_EQUAL;
				default:
					throw new IllegalArgumentException("Unknown comparison operator: " + string);
			}
		}

		public String operator() {
			switch (this) {
				case EQUAL:
					return "==";
				case NOT_EQUAL:
					return "!=";
				case LESS:
					return "<";
				case LESS_OR_EQUAL:
					return "<=";
				case GREATER:
					return ">";
				case GREATER_OR_EQUAL:
					return ">=";
				default:
					throw new IllegalArgumentException("Unknown comparison type: " + this.name());
			}
		}

		private boolean matches(int comparisonResult) {
			switch (this) {
				case EQUAL:
					return comparisonResult == 0;
				case NOT_EQUAL:
					return comparisonResult != 0;
				case LESS:
					return comparisonResult < 0;
				case LESS_OR_EQUAL:
					return comparisonResult <= 0;
				case GREATER:
					return comparisonResult > 0;
				case GREATER_OR_EQUAL:
					return comparisonResult >= 0;
				default:
					throw new IllegalStateException("Unknown comparison type: " + this.name());
			}
		}
	}
}

/*
 * Copyright 2020 Siphalor
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

package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;

public class InverseDollarOperator extends UnaryDollarOperator {
	private InverseDollarOperator(DollarPart dollarPart) {
		super(dollarPart);
	}

	public static DollarPart of(DollarPart dollarPart) throws DollarDeserializationException {
		DollarPart instance = new InverseDollarOperator(dollarPart);
		if (dollarPart.isConstant()) {
			try {
				return ValueDollarPart.of(instance.evaluate(null));
			} catch (DollarEvaluationException e) {
				throw new DollarDeserializationException(e);
			}
		}
		return instance;
	}

	@Override
	public Object evaluate(Object value) {
		if (value instanceof Number) {
			if (value instanceof Double) {
				return -(Double) value;
			} else if (value instanceof Float) {
				return -(Float) value;
			} else if (value instanceof Long) {
				return -(Long) value;
			} else if (value instanceof Integer) {
				return -(Integer) value;
			} else if (value instanceof Short) {
				return -(Short) value;
			}
		}
		return 0;
	}

	public static class Deserializer implements DollarPart.UnaryDeserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '-';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser) throws DollarDeserializationException {
			return InverseDollarOperator.of(dollarParser.parseUnary());
		}
	}
}

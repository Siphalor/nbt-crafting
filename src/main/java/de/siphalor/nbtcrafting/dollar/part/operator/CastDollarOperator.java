/*
 * Copyright 2020-2021 Siphalor
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

package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;

import java.util.Map;

public class CastDollarOperator implements DollarPart {
	private final DollarPart dollarPart;
	private final int typeId;

	private CastDollarOperator(DollarPart dollarPart, int typeId) {
		this.dollarPart = dollarPart;
		this.typeId = typeId;
	}

	public static DollarPart of(DollarPart dollarPart, int typeId) throws DollarException {
		DollarPart instance = new CastDollarOperator(dollarPart, typeId);
		if (dollarPart.isConstant()) {
			return ValueDollarPart.of(instance.evaluate(null));
		}
		return instance;
	}

	@Override
	public Object evaluate(Map<String, Object> reference) throws DollarEvaluationException {
		Object value = dollarPart.evaluate(reference);
		switch (typeId) {
			case 'd':
				if (value instanceof Number)
					return ((Number) value).doubleValue();
				return 0D;
			case 'f':
				if (value instanceof Number)
					return ((Number) value).floatValue();
				return 0F;
			case 'b':
			case 'c':
			case 'C':
				if (value instanceof Number)
					return ((Number) value).byteValue();
				return (byte) 0;
			case 's':
				if (value instanceof Number)
					return ((Number) value).shortValue();
				return (short) 0;
			case 'i':
				if (value instanceof Number)
					return ((Number) value).intValue();
				return 0;
			case 'l':
				if (value instanceof Number)
					return ((Number) value).longValue();
				return 0L;
			case 'B':
				return DollarUtil.asBoolean(value);
			case '"':
			case '\'':
			case 'a':
			case 'S':
				return value.toString();
			default:
				return null;
		}
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '#';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) {
			dollarParser.skip();
			return new CastDollarOperator(lastDollarPart, dollarParser.eat());
		}
	}
}

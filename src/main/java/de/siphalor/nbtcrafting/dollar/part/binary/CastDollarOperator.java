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

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ConstantDollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;

import java.util.Map;

public class CastDollarOperator implements DollarPart {
	private final DollarPart dollarPart;
	private final int typeId;

	private CastDollarOperator(DollarPart dollarPart, int typeId) {
		this.dollarPart = dollarPart;
		this.typeId = typeId;
	}

	public static DollarPart of(DollarPart dollarPart, int typeId) throws DollarDeserializationException {
		DollarPart instance = new CastDollarOperator(dollarPart, typeId);
		if (dollarPart instanceof ConstantDollarPart) {
			try {
				return ValueDollarPart.of(instance.evaluate(null));
			} catch (DollarEvaluationException e) {
				throw new DollarDeserializationException("Failed to short-circuit cast operator: " + e.getMessage(), e);
			}
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
				if (value instanceof String)
					return Double.parseDouble((String) value);
				if (value instanceof Boolean)
					return ((Boolean) value) ? 1.0d : 0.0d;
				throw new DollarEvaluationException("Failed to cast " + value + " to double");
			case 'f':
				if (value instanceof Number)
					return ((Number) value).floatValue();
				if (value instanceof String)
					return Float.parseFloat((String) value);
				if (value instanceof Boolean)
					return ((Boolean) value) ? 1.0f : 0.0f;
				throw new DollarEvaluationException("Failed to cast " + value + " to float");
			case 'b':
			case 'c':
			case 'C':
				if (value instanceof Number)
					return ((Number) value).byteValue();
				if (value instanceof String)
					return Byte.parseByte((String) value);
				if (value instanceof Boolean)
					return ((Boolean) value) ? (byte) 1 : (byte) 0;
				throw new DollarEvaluationException("Failed to cast " + value + " to byte");
			case 's':
				if (value instanceof Number)
					return ((Number) value).shortValue();
				if (value instanceof String)
					return Short.parseShort((String) value);
				if (value instanceof Boolean)
					return ((Boolean) value) ? (short) 1 : (short) 0;
				throw new DollarEvaluationException("Failed to cast " + value + " to short");
			case 'i':
				if (value instanceof Number)
					return ((Number) value).intValue();
				if (value instanceof String)
					return Integer.parseInt((String) value);
				if (value instanceof Boolean)
					return ((Boolean) value) ? 1 : 0;
				throw new DollarEvaluationException("Failed to cast " + value + " to int");
			case 'l':
				if (value instanceof Number)
					return ((Number) value).longValue();
				if (value instanceof String)
					return Long.parseLong((String) value);
				if (value instanceof Boolean)
					return ((Boolean) value) ? 1L : 0L;
				throw new DollarEvaluationException("Failed to cast " + value + " to long");
			case 'B':
				return DollarUtil.asBoolean(value);
			case 'a':
			case 'S':
				return value.toString();
			case 'n':
				if (value instanceof Number)
					return value;
				if (value instanceof Boolean)
					return ((Boolean) value) ? (byte) 1 : (byte) 0;
				if (value == null)
					return 0;
				throw new DollarEvaluationException("Failed to cast " + DollarUtil.asString(value) + " to generic number");
			default:
				throw new DollarEvaluationException("Unknown cast type identifier " + new String(new int[]{typeId}, 0, 0));
		}
	}
}

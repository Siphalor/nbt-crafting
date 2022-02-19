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

package de.siphalor.nbtcrafting.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class NumberUtil {
	private static final Class<?>[] NUMBER_TYPES = {
			Character.class,
			Byte.class,
			Short.class,
			Integer.class,
			Long.class,
			Float.class,
			Double.class
	};
	private static final String[] NUMBER_TYPE_IDENTIFIERS = {
			"",
			"cCb",
			"s",
			"i",
			"l",
			"f",
			"d"
	};
	public static final int CHARACTER = 0;
	public static final int BYTE = 1;
	public static final int SHORT = 2;
	public static final int INTEGER = 3;
	public static final int LONG = 4;
	public static final int FLOAT = 5;
	public static final int DOUBlE = 6;

	public static int getType(String type) {
		for (int i = 0; i < NUMBER_TYPE_IDENTIFIERS.length; i++) {
			if (StringUtils.containsAny(NUMBER_TYPE_IDENTIFIERS[i], type))
				return i;
		}
		return -1;
	}

	public static int getType(Number number) {
		return ArrayUtils.indexOf(NUMBER_TYPES, number.getClass());
	}

	public static int findSmallestType(Number a, Number b) {
		int typeA = ArrayUtils.indexOf(NUMBER_TYPES, a.getClass());
		int typeB = ArrayUtils.indexOf(NUMBER_TYPES, b.getClass());

		return Math.max(typeA, typeB);
	}

	public static Number denullify(Number number) {
		if (number == null) {
			return (byte) 0;
		}
		return number;
	}

	public static Number cast(Number number, int type) {
		if (number == null)
			return null;
		switch (type) {
			case BYTE:
			case CHARACTER:
				return number.byteValue();
			case SHORT:
				return number.shortValue();
			case INTEGER:
				return number.intValue();
			case LONG:
				return number.longValue();
			case FLOAT:
				return number.floatValue();
			case DOUBlE:
			default:
				return number.doubleValue();
		}
	}

	public static Number sum(Number a, Number b) {
		a = denullify(a);
		b = denullify(b);
		switch (findSmallestType(a, b)) {
			case 0:
			case 1:
				return (byte) (a.byteValue() + b.byteValue());
			case 2:
				return (short) (a.shortValue() + b.shortValue());
			case 3:
				return a.intValue() + b.intValue();
			case 4:
				return a.longValue() + b.longValue();
			case 5:
				return a.floatValue() + b.floatValue();
			default:
				return a.doubleValue() + b.doubleValue();
		}
	}

	public static Number difference(Number a, Number b) {
		a = denullify(a);
		b = denullify(b);
		switch (findSmallestType(a, b)) {
			case 0:
			case 1:
				return (byte) (a.byteValue() - b.byteValue());
			case 2:
				return (short) (a.shortValue() - b.shortValue());
			case 3:
				return a.intValue() - b.intValue();
			case 4:
				return a.longValue() - b.longValue();
			case 5:
				return a.floatValue() - b.floatValue();
			default:
				return a.doubleValue() - b.doubleValue();
		}
	}

	public static Number product(Number a, Number b) {
		a = denullify(a);
		b = denullify(b);
		switch (findSmallestType(a, b)) {
			case 0:
			case 1:
				return (byte) (a.byteValue() * b.byteValue());
			case 2:
				return (short) (a.shortValue() * b.shortValue());
			case 3:
				return a.intValue() * b.intValue();
			case 4:
				return a.longValue() * b.longValue();
			case 5:
				return a.floatValue() * b.floatValue();
			default:
				return a.doubleValue() * b.doubleValue();
		}
	}

	public static Number quotient(Number a, Number b) {
		a = denullify(a);
		if (b == null || b.doubleValue() == 0.0D)
			return Math.signum(a.doubleValue()) * Double.POSITIVE_INFINITY;
		switch (findSmallestType(a, b)) {
			case 0:
			case 1:
				return (byte) (a.byteValue() / b.byteValue());
			case 2:
				return (short) (a.shortValue() / b.shortValue());
			case 3:
				return a.intValue() / b.intValue();
			case 4:
				return a.longValue() / b.longValue();
			case 5:
				return a.floatValue() / b.floatValue();
			default:
				return a.doubleValue() / b.doubleValue();
		}
	}
}

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

package de.siphalor.nbtcrafting3.api.nbt;

import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtInt;

public class NbtNumberRange {
	public static final NbtNumberRange ANY_INT = new NbtNumberRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
	public static final NbtNumberRange ANY_DOUBLE = new NbtNumberRange(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);

	public final double begin;
	public final double end;
	private final boolean isInteger;

	private NbtNumberRange(double begin, double end, boolean isInteger) {
		this.begin = begin;
		this.end = end;
		this.isInteger = isInteger;
	}

	public boolean matches(double c) {
		return c >= begin && c <= end;
	}

	public AbstractNbtNumber getExample() {
		if (Double.isFinite(begin)) {
			if (Double.isFinite(end)) {
				return isInteger ? NbtInt.of((int) Math.round((begin + end) / 2)) : NbtDouble.of((begin + end) / 2);
			} else {
				return isInteger ? NbtInt.of((int) Math.round(begin)) : NbtDouble.of(begin);
			}
		} else {
			if (Double.isFinite(end)) {
				return isInteger ? NbtInt.of((int) Math.round(end)) : NbtDouble.of(end);
			} else {
				return isInteger ? NbtInt.of(0) : NbtDouble.of(0);
			}
		}
	}

	public static NbtNumberRange equals(Number a) {
		return new NbtNumberRange(a.doubleValue(), a.doubleValue(), isInteger(a));
	}

	public static NbtNumberRange between(Number a, Number b) {
		return new NbtNumberRange(
				Math.min(a.doubleValue(), b.doubleValue()),
				Math.max(a.doubleValue(), b.doubleValue()),
				isInteger(a) && isInteger(b)
		);
	}

	public static NbtNumberRange fromInfinity(Number end) {
		return new NbtNumberRange(Double.NEGATIVE_INFINITY, end.doubleValue(), isInteger(end));
	}

	public static NbtNumberRange toInfinity(Number begin) {
		return new NbtNumberRange(begin.doubleValue(), Double.POSITIVE_INFINITY, isInteger(begin));
	}

	public static NbtNumberRange ofString(String string) {
		if (!string.contains("..")) {
			try {
				return NbtNumberRange.equals(parseNumber(string));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		int position = string.indexOf("..");
		if (position == 0) {
			try {
				return NbtNumberRange.fromInfinity(parseNumber(string.substring(2)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else if (position == string.length() - 2) {
			try {
				return NbtNumberRange.toInfinity(parseNumber(string.substring(0, string.length() - 2)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else {
			try {
				return NbtNumberRange.between(parseNumber(string.substring(0, position)), parseNumber(string.substring(position + 2)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return ANY_INT;
	}

	private static Number parseNumber(String string) throws NumberFormatException {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return Double.parseDouble(string);
		}
	}

	private static boolean isInteger(Number number) {
		return number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long;
	}
}

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

package de.siphalor.nbtcrafting.api.nbt;

public class NbtNumberRange {
	public final double begin;
	public final double end;

	private NbtNumberRange(double begin, double end) {
		this.begin = begin;
		this.end = end;
	}

	public boolean matches(double c) {
		return c >= begin && c <= end;
	}

	public static NbtNumberRange equals(double a) {
		return new NbtNumberRange(a, a);
	}

	public static NbtNumberRange between(double a, double b) {
		return new NbtNumberRange(Math.min(a, b), Math.max(a, b));
	}

	public static NbtNumberRange fromInfinity(double end) {
		return new NbtNumberRange(Double.NEGATIVE_INFINITY, end);
	}

	public static NbtNumberRange toInfinity(double begin) {
		return new NbtNumberRange(begin, Double.POSITIVE_INFINITY);
	}

	public static NbtNumberRange ofString(String string) {
		if (!string.contains("..")) {
			try {
				return NbtNumberRange.equals(Double.parseDouble(string));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		int position = string.indexOf("..");
		if (position == 0) {
			try {
				return NbtNumberRange.fromInfinity(Double.parseDouble(string.substring(2)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else if (position == string.length() - 2) {
			try {
				return NbtNumberRange.toInfinity(Double.parseDouble(string.substring(0, string.length() - 2)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else {
			try {
				return NbtNumberRange.between(Double.parseDouble(string.substring(0, position)), Double.parseDouble(string.substring(position + 2)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return NbtNumberRange.between(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
}

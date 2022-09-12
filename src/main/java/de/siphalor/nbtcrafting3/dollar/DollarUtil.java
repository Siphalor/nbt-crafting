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

package de.siphalor.nbtcrafting3.dollar;

import java.util.List;
import java.util.Objects;

import de.siphalor.nbtcrafting3.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting3.dollar.exception.DollarException;
import de.siphalor.nbtcrafting3.util.NumberUtil;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

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
		return !isEmpty(o);
	}

	public static String asString(Object o) {
		if (o == null) {
			return "<null>";
		}
		return o.toString();
	}

	public static boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		}
		if (o instanceof List) {
			return ((List<?>) o).isEmpty();
		}
		if (o instanceof NbtCompound) {
			return ((NbtCompound) o).isEmpty();
		}
		return false;
	}

	public static Number expectNumber(Object o) throws DollarException {
		if (o == null) {
			return (byte) 0;
		} else if (o instanceof Number) {
			return (Number) o;
		}
		throw new DollarException("Cannot implicitly cast " + asString(o) + " to a number");
	}

	/**
	 * Converts any value into the appropriate dollar representation.
	 * @param o the object to convert
	 * @return the dollar representation of the object
	 * @see NbtUtil#toDollarValue(NbtElement)
	 */
	public static Object toDollarValue(Object o) {
		if (o instanceof NbtElement) {
			return NbtUtil.toDollarValue((NbtElement) o);
		}
		return o;
	}

	public static boolean equals(Object a, Object b) {
		if (Objects.equals(a, b)) {
			return true;
		}

		if (a instanceof List && b instanceof List) {
			if (((List<?>) a).size() != ((List<?>) b).size()) {
				return false;
			}
			for (int i = 0; i < ((List<?>) a).size(); i++) {
				if (!equals(((List<?>) a).get(i), ((List<?>) b).get(i))) {
					return false;
				}
			}
			return true;
		} else if (a instanceof Number && b instanceof Number) {
			int type = NumberUtil.findSmallestType((Number) a, (Number) b);
			return Objects.equals(NumberUtil.cast((Number) a, type), NumberUtil.cast((Number) b, type));
		}
		return false;
	}
}

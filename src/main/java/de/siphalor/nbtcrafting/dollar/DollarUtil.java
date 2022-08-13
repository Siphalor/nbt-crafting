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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

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
		if (o instanceof ListTag) {
			return !((ListTag) o).isEmpty();
		}
		if (o instanceof CompoundTag) {
			return !((CompoundTag) o).isEmpty();
		}
		return o != null;
	}

	public static String asString(Object o) {
		if (o == null) {
			return "<null>";
		}
		return o.toString();
	}

	public static Number expectNumber(Object o) throws DollarException {
		if (o == null) {
			return (byte) 0;
		} else if (o instanceof Number) {
			return (Number) o;
		}
		throw new DollarException("Cannot implicitly cast " + asString(o) + " to a number");
	}
}

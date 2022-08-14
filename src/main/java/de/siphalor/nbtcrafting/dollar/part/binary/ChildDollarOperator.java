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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.DollarUtil;
import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;

public class ChildDollarOperator extends BinaryDollarOperator {
	public ChildDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	public static DollarPart of(DollarPart first, DollarPart second) throws DollarDeserializationException {
		return new ChildDollarOperator(first, second);
	}

	@Override
	public Object apply(Object first, Object second) throws DollarEvaluationException {
		if (first instanceof ItemStack) {
			first = NbtUtil.getTagOrEmpty((ItemStack) first);
		}

		if (first instanceof CompoundTag) {
			String key = second.toString();
			if (((CompoundTag) first).contains(key)) {
				return NbtUtil.toDollarValue(((CompoundTag) first).get(key));
			}
			return null;
		} else if (first instanceof ListTag && second instanceof Number) {
			int index = ((Number) second).intValue();
			if (index < ((ListTag) first).size()) {
				return NbtUtil.toDollarValue(((ListTag) first).get(index));
			}
			return null;
		}
		throw new DollarEvaluationException("Cannot access element " + DollarUtil.asString(second) + " on value " + DollarUtil.asString(first));
	}
}

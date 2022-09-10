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

package de.siphalor.nbtcrafting3.dollar.part.binary;

import net.minecraft.nbt.CompoundTag;

import de.siphalor.nbtcrafting3.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting3.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting3.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;
import de.siphalor.nbtcrafting3.util.NumberUtil;

public class SumDollarOperator extends BinaryDollarOperator {
	private SumDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	public static DollarPart of(DollarPart first, DollarPart second) throws DollarDeserializationException {
		return shortCircuitConstant(new SumDollarOperator(first, second));
	}

	@Override
	public Object apply(Object first, Object second) throws DollarEvaluationException {
		if ((first instanceof Number || first == null) && (second instanceof Number || second == null))
			return NumberUtil.sum((Number) first, (Number) second);
		if (first instanceof CompoundTag && second instanceof CompoundTag) {
			CompoundTag result = ((CompoundTag) first).copy();
			NbtUtil.mergeInto(result, (CompoundTag) second, true);
			return result;
		}
		return first + "" + second;
	}
}

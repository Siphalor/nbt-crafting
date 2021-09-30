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

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;
import de.siphalor.nbtcrafting.util.NumberUtil;

public class SumDollarOperator extends BinaryDollarOperator {
	private SumDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	public static DollarPart of(DollarPart first, DollarPart second) throws DollarDeserializationException {
		DollarPart instance = new SumDollarOperator(first, second);
		if (first.isConstant() && second.isConstant()) {
			try {
				return ValueDollarPart.of(instance.evaluate(null));
			} catch (DollarEvaluationException e) {
				throw new DollarDeserializationException(e);
			}
		}
		return instance;
	}

	@Override
	public Object apply(Object first, Object second) throws DollarEvaluationException {
		if ((first instanceof Number || first == null) && (second instanceof Number || second == null))
			return NumberUtil.sum((Number) first, (Number) second);
		{
			Object result = tryListSum(first, second);
			if (result != null) {
				return result;
			}
		}
		if (first instanceof NbtCompound && second instanceof NbtCompound) {
			NbtCompound result = ((NbtCompound) first).copy();
			NbtUtil.mergeInto(result, (NbtCompound) second, true);
			return result;
		}
		return first + "" + second;
	}

	private Object tryListSum(Object first, Object second) throws DollarEvaluationException {
		if (first instanceof NbtList) {
			if (second == null) {
				return first;
			}
			if (second instanceof NbtList) {
				if (((NbtList) first).getHeldType() == ((NbtList) second).getHeldType()) {
					NbtList result = ((NbtList) first).copy();
					result.addAll((NbtList) second);
					return result;
				}
			}
			NbtElement secondTag = NbtUtil.asTag(second);
			if (((NbtList) first).getHeldType() == secondTag.getType()) {
				NbtList result = ((NbtList) first).copy();
				result.add(secondTag);
				return result;
			}
			throw new DollarEvaluationException("Couldn't sum up list " + first.toString() + " with " + second.toString());
		}
		return first == null && second instanceof NbtList ? second : null;
	}

	public static class Deserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '+';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarDeserializationException {
			dollarParser.skip();
			if (lastDollarPart == null)
				throw new DollarDeserializationException("Unexpected plus!");
			return SumDollarOperator.of(lastDollarPart, dollarParser.parse(priority));
		}
	}
}

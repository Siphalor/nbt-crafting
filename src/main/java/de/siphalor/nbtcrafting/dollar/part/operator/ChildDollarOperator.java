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

package de.siphalor.nbtcrafting.dollar.part.operator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;

public class ChildDollarOperator extends BinaryDollarOperator {
	public ChildDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	@Override
	public Object apply(Object first, Object second) {
		if (first instanceof CompoundTag) {
			String key = second.toString();
			if (((CompoundTag) first).contains(key)) {
				return NbtUtil.toDollarValue(((CompoundTag) first).get(key));
			}
		} else if (first instanceof ListTag && second instanceof Number) {
			int index = ((Number) second).intValue();
			if (index < ((ListTag) first).size()) {
				return NbtUtil.toDollarValue(((ListTag) first).get(index));
			}
		}
		return null;
	}

	public static class DotDeserializer implements Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '.';
		}

		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) {
			dollarParser.skip();
			StringBuilder stringBuilder = new StringBuilder();
			int character = dollarParser.eat();
			if (Character.isJavaIdentifierStart(character)) {
				stringBuilder.append(Character.toChars(character));
				while (true) {
					character = dollarParser.peek();
					if (Character.isJavaIdentifierPart(character)) {
						dollarParser.skip();
						stringBuilder.append(Character.toChars(character));
					} else {
						break;
					}
				}
				return new ChildDollarOperator(lastDollarPart, ValueDollarPart.of(stringBuilder.toString()));
			}
			return null;
		}
	}

	public static class BracketDeserializer implements DollarPart.Deserializer {
		@Override
		public boolean matches(int character, DollarParser dollarParser) {
			return character == '[';
		}

		@Override
		public DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) {
			dollarParser.skip();
			return new ChildDollarOperator(lastDollarPart, dollarParser.parseTo(']'));
		}
	}
}

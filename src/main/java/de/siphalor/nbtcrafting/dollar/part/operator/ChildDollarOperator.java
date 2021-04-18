/*
 * Copyright 2020 Siphalor
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

import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class ChildDollarOperator extends BinaryDollarOperator {
	public ChildDollarOperator(DollarPart first, DollarPart second) {
		super(first, second);
	}

	@Override
	public Object apply(Object first, Object second) {
		if (first instanceof NbtCompound) {
			String key = second.toString();
			if (((NbtCompound) first).contains(key)) {
				return NbtUtil.toDollarValue(((NbtCompound) first).get(key));
			}
		} else if (first instanceof NbtList && second instanceof Number) {
			int index = ((Number) second).intValue();
			if (index < ((NbtList) first).size()) {
				return NbtUtil.toDollarValue(((NbtList) first).get(index));
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

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

package de.siphalor.nbtcrafting.dollar.part.unary;

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.ValueDollarPart;

public class NumberDollarPartDeserializer implements DollarPart.UnaryDeserializer {
	@Override
	public boolean matches(int character, DollarParser dollarParser) {
		return Character.isDigit(character);
	}

	@Override
	public DollarPart parse(DollarParser dollarParser) throws DollarDeserializationException {
		StringBuilder stringBuilder = new StringBuilder(String.valueOf(Character.toChars(dollarParser.eat())));
		boolean dot = false;
		int character;
		while (true) {
			character = dollarParser.peek();
			if (Character.isDigit(character)) {
				dollarParser.skip();
				stringBuilder.append(Character.toChars(character));
			} else if (!dot && character == '.') {
				dollarParser.skip();
				stringBuilder.append('.');
				dot = true;
			} else {
				break;
			}
		}

		try {
			if (dot)
				return ValueDollarPart.of(Double.parseDouble(stringBuilder.toString()));
			else
				return ValueDollarPart.of(Integer.parseInt(stringBuilder.toString()));
		} catch (NumberFormatException e) {
			throw new DollarDeserializationException(e);
		}
	}
}

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

import java.util.Arrays;
import java.util.stream.IntStream;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.siphalor.nbtcrafting3.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;
import de.siphalor.nbtcrafting3.dollar.part.value.ValueDollarPart;

public class ComparisonOperatorTests {
	@Test
	void evaluate_matrix() {
		ListTag someList = new ListTag();
		someList.add(StringTag.of("value"));
		Object[][] inputs = new Object[][] {
				{ (byte) 1, 2 },
				{ 0.2, 0.000001 },
				{ (short) 4, 4.0D },
				{ "a", "b" },
				{ "cd", "c" },
				{ "hi", "hi" },
				{ 123, "fail" },
				{ new ListTag(), new ListTag() },
				{ new ListTag(), someList },
		};
		Object[][] results = new Boolean[][] {
				{ false,  true,  true,  true, false, false },
				{ false,  true, false, false,  true,  true },
				{  true, false, false,  true, false,  true },
				{ false,  true,  true,  true, false, false },
				{ false,  true, false, false,  true,  true },
				{  true, false, false,  true, false,  true },
				{ false,  true,  null,  null,  null,  null },
				{  true, false,  null,  null,  null,  null },
				{ false,  true,  null,  null,  null,  null },
		};

		Assertions.assertAll(IntStream.range(0, inputs.length).mapToObj(i -> () -> {
			Object[] curInputs = inputs[i];
			Object[] curResults = results[i];
			Assertions.assertAll(Arrays.stream(ComparisonDollarOperator.Type.values()).map(type -> () -> {
				if (curResults[type.ordinal()] != null) {
					DollarPart operator = ComparisonDollarOperator.of(type, ValueDollarPart.of(curInputs[0]), ValueDollarPart.of(curInputs[1]));
					Object result = operator.evaluate(null);
					Assertions.assertEquals(curResults[type.ordinal()], result);
				} else {
					Assertions.assertThrows(
							DollarDeserializationException.class,
							() -> ComparisonDollarOperator.of(type, ValueDollarPart.of(curInputs[0]), ValueDollarPart.of(curInputs[1]))
					);
				}
			}));
		}));
	}
}

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

import java.util.stream.IntStream;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;

public class CastDollarOperatorTests {
	@Test
	void evaluate_castMatrix() {
		int[][] typeIds = new int[][] {
				{'d'}, {'f'}, {'b','c','C'}, {'s'}, {'i'}, {'l'}, {'B'}, {'a','S'}, {'n'}
		};
		Object[] inputs = new Object[] {
				-1.2d, -1.2f, (byte) -1, (short) -1, -1, -1L, true, 0d, 0L, false, "-1", "0"
		};
		Object[][] expected = new Object[][] {
				{-1.2d, -1.2f, (byte) -1, (short) -1, -1, -1L,  true,  "-1.2",      -1.2d},
				{ null, -1.2f, (byte) -1, (short) -1, -1, -1L,  true,  "-1.2",      -1.2f}, // null to ignore because of rounding errors
				{  -1d,   -1f, (byte) -1, (short) -1, -1, -1L,  true,    "-1",  (byte) -1},
				{  -1d,   -1f, (byte) -1, (short) -1, -1, -1L,  true,    "-1", (short) -1},
				{  -1d,   -1f, (byte) -1, (short) -1, -1, -1L,  true,    "-1",         -1},
				{  -1d,   -1f, (byte) -1, (short) -1, -1, -1L,  true,    "-1",        -1L},
				{   1d,    1f, (byte)  1, (short)  1,  1,  1L,  true,  "true",   (byte) 1},
				{   0d,    0f, (byte)  0, (short)  0,  0,  0L, false,   "0.0",         0d},
				{   0d,    0f, (byte)  0, (short)  0,  0,  0L, false,     "0",         0L},
				{   0d,    0f, (byte)  0, (short)  0,  0,  0L, false, "false",   (byte) 0},
				{  -1d,   -1f, (byte) -1, (short) -1, -1, -1L,  true,    "-1",       null}, // null, because throws exception
				{   0d,    0f, (byte)  0, (short)  0,  0,  0L,  true,     "0",       null}, // null, because throws exception
		};

		Assertions.assertAll(IntStream.range(0, inputs.length).mapToObj(input -> () -> {
			Assertions.assertAll(IntStream.range(0, typeIds.length).mapToObj(type -> () -> {
				Object curExpected = expected[input][type];
				if (curExpected == null) {
					return;
				}

				for (int typeId : typeIds[type]) {
					Object curInput = inputs[input];
					Object result = CastDollarOperator.of(ValueDollarPart.of(curInput), typeId).evaluate(null);
					String metadata = "input: " + curInput + "; inputClass: " + curInput.getClass().getSimpleName() + "; typeId: " + new String(new int[]{ typeId }, 0, 1) + ";";
					Assertions.assertEquals(curExpected.getClass(), result.getClass(), metadata);
					Assertions.assertEquals(curExpected, result, metadata);
				}
			}));
		}));
	}

	@ParameterizedTest
	@ValueSource(ints = { 'd', 'f', 'b', 'c', 'C', 's', 'i', 'l', 'n' })
	void evaluate_unsupportedTypeShouldFail(int typeId) {
		Assertions.assertThrows(DollarDeserializationException.class, () -> CastDollarOperator.of(ValueDollarPart.of(new CompoundTag()), typeId).evaluate(null));
	}
}

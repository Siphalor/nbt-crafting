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

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.siphalor.nbtcrafting3.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting3.dollar.part.value.ValueDollarPart;

public class ProductDollarOperatorTests {
	@Test
	void testNumeric() {
		Number[][] data = new Number[][] {
				{1, 2, 2},
				{3, -7, -21},
				{(short) 4, (short) 5, (short) 20},
				{(byte) 6, (byte) 7, (byte) 42},
				{(long) 8, (long) 9, (long) 72},
				{12.5, -13.2, -165.0},
				{2, null, 0},
				{null, null, (byte) 0}
		};
		Assertions.assertAll(Arrays.stream(data).map(row -> () -> {
			Assertions.assertEquals(row[2], ProductDollarOperator.of(ValueDollarPart.of(row[0]), ValueDollarPart.of(row[1])).evaluate(null), row[0] + " * " + row[1]);
			Assertions.assertEquals(row[2], ProductDollarOperator.of(ValueDollarPart.of(row[1]), ValueDollarPart.of(row[0])).evaluate(null), row[1] + " * " + row[0]);
		}));
	}

	@Test
	void testStringRepeat() {
		Object[][] data = new Object[][] {
				{2, "a", "aa"},
				{1, "heyho", "heyho"},
				{0, "blub", ""},
				{null, "blub", ""},
				{3.4, "test", "testtesttest"},
				{-1, "test", ""},
				{-1.2, "test", ""},
				{-1.2, "", ""},
				{10, "", ""},
		};

		Assertions.assertAll(Arrays.stream(data).map(row -> () -> {
			Assertions.assertEquals(row[2], ProductDollarOperator.of(ValueDollarPart.of(row[0]), ValueDollarPart.of(row[1])).evaluate(null), row[0] + " * " + row[1]);
			Assertions.assertEquals(row[2], ProductDollarOperator.of(ValueDollarPart.of(row[1]), ValueDollarPart.of(row[0])).evaluate(null), row[1] + " * " + row[0]);
		}));
	}

	@Test
	void testFails() {
		Object[][] data = new Object[][] {
				{"a", "b"},
				{new NbtList(), 2},
				{new NbtCompound(), 3},
		};
		Assertions.assertAll(Arrays.stream(data).map(row -> () -> {
			Assertions.assertThrows(DollarDeserializationException.class, () -> ProductDollarOperator.of(ValueDollarPart.of(row[0]), ValueDollarPart.of(row[1])).evaluate(null), row[0] + " * " + row[1]);
			Assertions.assertThrows(DollarDeserializationException.class, () -> ProductDollarOperator.of(ValueDollarPart.of(row[1]), ValueDollarPart.of(row[0])).evaluate(null), row[1] + " * " + row[0]);
		}));
	}
}

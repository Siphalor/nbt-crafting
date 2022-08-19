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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;

public class LogicalBinaryDollarOperatorTests {
	@ParameterizedTest
	@CsvSource({
			"true, true, true",
			"true, false, false",
			"false, true, false",
			"false, false, false",
	})
	void andTests(boolean a, boolean b, boolean expected) {
		Assertions.assertEquals(expected, Assertions.assertDoesNotThrow(() -> LogicalBinaryDollarOperator.andOf(ValueDollarPart.of(a), ValueDollarPart.of(b)).evaluate(null)));
	}

	@ParameterizedTest
	@CsvSource({
			"true, true, true",
			"true, false, true",
			"false, true, true",
			"false, false, false",
	})
	void orTests(boolean a, boolean b, boolean expected) {
		Assertions.assertEquals(expected, Assertions.assertDoesNotThrow(() -> LogicalBinaryDollarOperator.orOf(ValueDollarPart.of(a), ValueDollarPart.of(b)).evaluate(null)));
	}

	@Test
	void testCasting() {
		Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> (boolean) LogicalBinaryDollarOperator.andOf(ValueDollarPart.of("hi"), ValueDollarPart.of(123)).evaluate(null)));
		Assertions.assertFalse(Assertions.assertDoesNotThrow(() -> (boolean) LogicalBinaryDollarOperator.andOf(ValueDollarPart.of(new CompoundTag()), ValueDollarPart.of(123)).evaluate(null)));
		Assertions.assertFalse(Assertions.assertDoesNotThrow(() -> (boolean) LogicalBinaryDollarOperator.andOf(ValueDollarPart.of(new ListTag()), ValueDollarPart.of(123)).evaluate(null)));
		ListTag someList = new ListTag();
		someList.add(IntTag.of(1));
		Assertions.assertTrue(Assertions.assertDoesNotThrow(() -> (boolean) LogicalBinaryDollarOperator.andOf(ValueDollarPart.of(someList), ValueDollarPart.of(123)).evaluate(null)));
	}
}

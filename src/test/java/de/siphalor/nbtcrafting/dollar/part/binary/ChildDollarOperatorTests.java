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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.value.ReferenceDollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;

public class ChildDollarOperatorTests {
	@Test
	void evaluate_compounds() {
		CompoundTag compound = new CompoundTag();
		compound.putString("key", "value");
		compound.putInt("int", 123);

		Assertions.assertEquals(
				"value",
				Assertions.assertDoesNotThrow(() -> ChildDollarOperator.of(ValueDollarPart.of(compound), ValueDollarPart.of("key")).evaluate(null))
		);
		Assertions.assertEquals(
				123,
				Assertions.assertDoesNotThrow(() -> ChildDollarOperator.of(ValueDollarPart.of(compound), ValueDollarPart.of("int")).evaluate(null))
		);
		Assertions.assertNull(
				Assertions.assertDoesNotThrow(() -> ChildDollarOperator.of(ValueDollarPart.of(compound), ValueDollarPart.of("missing")).evaluate(null))
		);
	}

	@Test
	void evaluate_lists() {
		ListTag list = new ListTag();
		list.add(StringTag.of("value"));
		list.add(StringTag.of("123"));

		Assertions.assertEquals(
				"value",
				Assertions.assertDoesNotThrow(() -> ChildDollarOperator.of(ValueDollarPart.of(list), ValueDollarPart.of(0)).evaluate(null))
		);
		Assertions.assertEquals(
				"123",
				Assertions.assertDoesNotThrow(() -> ChildDollarOperator.of(ValueDollarPart.of(list), ValueDollarPart.of(1)).evaluate(null))
		);
		Assertions.assertNull(
				Assertions.assertDoesNotThrow(() -> ChildDollarOperator.of(ValueDollarPart.of(list), ValueDollarPart.of(2)).evaluate(null))
		);
		Assertions.assertThrows(
				DollarEvaluationException.class,
				() -> ChildDollarOperator.of(ValueDollarPart.of(list), ValueDollarPart.of(null)).evaluate(null)
		);
		Assertions.assertThrows(
				DollarEvaluationException.class,
				() -> ChildDollarOperator.of(ValueDollarPart.of(list), ValueDollarPart.of("1")).evaluate(null)
		);
	}

	void shortCircuiting() {
		CompoundTag compound = new CompoundTag();
		compound.putString("key", "value");
		compound.putInt("int", 123);

		Assertions.assertEquals(
				ValueDollarPart.of("value"),
				Assertions.assertDoesNotThrow(() -> ChildDollarOperator.of(ValueDollarPart.of(compound), ValueDollarPart.of("key")).evaluate(null))
		);
		Assertions.assertEquals(
				ChildDollarOperator.class,
				Assertions.assertDoesNotThrow(() -> ChildDollarOperator.of(ReferenceDollarPart.of("var"), ValueDollarPart.of("key"))).getClass()
		);
	}
}

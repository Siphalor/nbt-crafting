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

package de.siphalor.nbtcrafting3.dollar;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.siphalor.nbtcrafting3.dollar.exception.DollarException;

public class DollarUtilTests {
	@Test
	void asBoolean_truthy() {
		Assertions.assertTrue(DollarUtil.asBoolean(true));
		Assertions.assertTrue(DollarUtil.asBoolean(1));
		Assertions.assertTrue(DollarUtil.asBoolean(-1));
		Assertions.assertTrue(DollarUtil.asBoolean(1.57));
		Assertions.assertTrue(DollarUtil.asBoolean("true"));
		Assertions.assertTrue(DollarUtil.asBoolean("false"));
		Assertions.assertTrue(DollarUtil.asBoolean(" "));
		ListTag someList = new ListTag();
		someList.add(IntTag.of(1));
		Assertions.assertTrue(DollarUtil.asBoolean(someList));
		CompoundTag someCompound = new CompoundTag();
		someCompound.put("someKey", IntTag.of(1));
		Assertions.assertTrue(DollarUtil.asBoolean(someCompound));
	}

	@Test
	void asBoolean_falsy() {
		Assertions.assertFalse(DollarUtil.asBoolean(null));
		Assertions.assertFalse(DollarUtil.asBoolean(false));
		Assertions.assertFalse(DollarUtil.asBoolean(0));
		Assertions.assertFalse(DollarUtil.asBoolean(0.0));
		Assertions.assertFalse(DollarUtil.asBoolean(0.0f));
		Assertions.assertFalse(DollarUtil.asBoolean(""));
		Assertions.assertFalse(DollarUtil.asBoolean(new ListTag()));
		Assertions.assertFalse(DollarUtil.asBoolean(new CompoundTag()));
	}

	@Test
	void expectNumber() {
		Assertions.assertEquals(1, Assertions.assertDoesNotThrow(() -> DollarUtil.expectNumber(1)));
		Assertions.assertEquals(1.23, Assertions.assertDoesNotThrow(() -> DollarUtil.expectNumber(1.23)));
		Assertions.assertEquals((byte) 0, Assertions.assertDoesNotThrow(() -> DollarUtil.expectNumber(null)));
		Assertions.assertEquals((short) 0, Assertions.assertDoesNotThrow(() -> DollarUtil.expectNumber((short) 0)));
		Assertions.assertThrows(DollarException.class, () -> DollarUtil.expectNumber(false));
		Assertions.assertThrows(DollarException.class, () -> DollarUtil.expectNumber("fail"));
		Assertions.assertThrows(DollarException.class, () -> DollarUtil.expectNumber(""));
		Assertions.assertThrows(DollarException.class, () -> DollarUtil.expectNumber(new ListTag()));
		Assertions.assertThrows(DollarException.class, () -> DollarUtil.expectNumber(new CompoundTag()));
	}
}

package de.siphalor.nbtcrafting.dollar;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.siphalor.nbtcrafting.dollar.exception.DollarException;

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

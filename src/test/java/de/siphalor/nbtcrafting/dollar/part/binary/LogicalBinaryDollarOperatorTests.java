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

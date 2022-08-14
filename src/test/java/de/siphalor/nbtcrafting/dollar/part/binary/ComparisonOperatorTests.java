package de.siphalor.nbtcrafting.dollar.part.binary;

import java.util.Arrays;
import java.util.stream.IntStream;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;

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

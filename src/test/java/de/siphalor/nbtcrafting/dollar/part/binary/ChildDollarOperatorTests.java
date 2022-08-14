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

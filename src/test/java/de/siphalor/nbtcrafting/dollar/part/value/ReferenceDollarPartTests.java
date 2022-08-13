package de.siphalor.nbtcrafting.dollar.part.value;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;

public class ReferenceDollarPartTests {
	void evaluate() {
		Map<String, Object> references = ImmutableMap.of("a", 1, "b", 2, "c", 3);
		Assertions.assertEquals(1, Assertions.assertDoesNotThrow(() -> ReferenceDollarPart.of("a").evaluate(references)));
		Assertions.assertEquals(2, Assertions.assertDoesNotThrow(() -> ReferenceDollarPart.of("b").evaluate(references)));
		Assertions.assertEquals(3, Assertions.assertDoesNotThrow(() -> ReferenceDollarPart.of("c").evaluate(references)));
		Assertions.assertThrows(DollarEvaluationException.class, () -> ReferenceDollarPart.of("d").evaluate(references));
	}
}

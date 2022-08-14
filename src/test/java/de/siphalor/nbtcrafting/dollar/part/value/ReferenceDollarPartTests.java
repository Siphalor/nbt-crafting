package de.siphalor.nbtcrafting.dollar.part.value;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;

import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.reference.MapBackedReferenceResolver;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class ReferenceDollarPartTests {
	void evaluate() {
		ReferenceResolver referenceResolver = new MapBackedReferenceResolver(ImmutableMap.of("a", 1, "b", 2, "c", 3));
		Assertions.assertEquals(1, Assertions.assertDoesNotThrow(() -> ReferenceDollarPart.of("a").evaluate(referenceResolver)));
		Assertions.assertEquals(2, Assertions.assertDoesNotThrow(() -> ReferenceDollarPart.of("b").evaluate(referenceResolver)));
		Assertions.assertEquals(3, Assertions.assertDoesNotThrow(() -> ReferenceDollarPart.of("c").evaluate(referenceResolver)));
		Assertions.assertThrows(DollarEvaluationException.class, () -> ReferenceDollarPart.of("d").evaluate(referenceResolver));
	}
}

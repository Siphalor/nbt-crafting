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

package de.siphalor.nbtcrafting3.dollar.part.value;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;

import de.siphalor.nbtcrafting3.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting3.dollar.reference.MapBackedReferenceResolver;
import de.siphalor.nbtcrafting3.dollar.reference.ReferenceResolver;

public class ReferenceDollarPartTests {
	void evaluate() {
		ReferenceResolver referenceResolver = new MapBackedReferenceResolver(ImmutableMap.of("a", 1, "b", 2, "c", 3));
		Assertions.assertEquals(1, Assertions.assertDoesNotThrow(() -> ReferenceDollarPart.of("a").evaluate(referenceResolver)));
		Assertions.assertEquals(2, Assertions.assertDoesNotThrow(() -> ReferenceDollarPart.of("b").evaluate(referenceResolver)));
		Assertions.assertEquals(3, Assertions.assertDoesNotThrow(() -> ReferenceDollarPart.of("c").evaluate(referenceResolver)));
		Assertions.assertThrows(DollarEvaluationException.class, () -> ReferenceDollarPart.of("d").evaluate(referenceResolver));
	}
}

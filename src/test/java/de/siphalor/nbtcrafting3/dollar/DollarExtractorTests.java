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

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.siphalor.nbtcrafting3.dollar.exception.UnresolvedDollarReferenceException;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;

public class DollarExtractorTests {
	@ParameterizedTest
	@ValueSource(
			strings = {
					"",
					"a+b+",
					"a+/b",
					"a++",
					"a..b",
					"a + b +/ c +",
					"a+b)#FAIL!",
					"a!b",
					"/b"
			}
	)
	void parse_shouldFail(String input) {
		Assertions.assertNull(DollarExtractor.parse(input, false), "Should return null to indicate failure");
	}

	@ParameterizedTest
	@CsvSource(value = {
			"\"\",||",
			"'',||",
			"\"test\",test",
			"'test',test",
			"\"Hello World!\",Hello World!",
	}, quoteCharacter = '|')
	void parse_stringLiterals(String input, String expected) {
		DollarPart parsed = DollarExtractor.parse(input, false);
		Assertions.assertNotNull(parsed);
		Assertions.assertEquals(
				expected,
				Assertions.assertDoesNotThrow(() -> parsed.evaluate(null))
		);
	}

	@Test
	void parseAndRun_listMap() {
		DollarPart expression = DollarExtractor.parse("map(combine(list, [7, 10]), it -> it + 2) == [3,4,5,9,12]", false);
		Assertions.assertNotNull(expression);
		Assertions.assertEquals(
				true,
				Assertions.assertDoesNotThrow(() -> expression.evaluate(ref -> {
					if ("list".equals(ref)) {
						return Arrays.asList(1, 2, 3);
					}
					throw new UnresolvedDollarReferenceException(ref);
				}))
		);
	}
}

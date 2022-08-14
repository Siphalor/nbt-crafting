package de.siphalor.nbtcrafting.dollar;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.siphalor.nbtcrafting.dollar.exception.UnresolvedDollarReferenceException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;

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
		Assertions.assertEquals(ValueDollarPart.of(expected), DollarExtractor.parse(input, false));
	}

	@Test
	void parseAndRun_listMap() {
		DollarPart expression = DollarExtractor.parse("map(list, it + 2)", false);
		Assertions.assertNotNull(expression);
		Assertions.assertEquals(
				Arrays.asList(3, 4, 5),
				Assertions.assertDoesNotThrow(() -> expression.evaluate(ref -> {
					if ("list".equals(ref)) {
						return Arrays.asList(1, 2, 3);
					}
					throw new UnresolvedDollarReferenceException(ref);
				}))
		);
	}
}

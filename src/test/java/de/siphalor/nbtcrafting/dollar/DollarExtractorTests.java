package de.siphalor.nbtcrafting.dollar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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
}

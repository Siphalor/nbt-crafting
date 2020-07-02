package de.siphalor.nbtcrafting.dollar.part;

import de.siphalor.nbtcrafting.dollar.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarParser;

import java.util.Map;

public interface DollarPart {
	Object evaluate(Map<String, Object> reference) throws DollarEvaluationException;

	default boolean isConstant() {
		return false;
	}

	interface Deserializer {
		boolean matches(int character, DollarParser dollarParser);

		DollarPart parse(DollarParser dollarParser, DollarPart lastDollarPart, int priority) throws DollarDeserializationException;
	}

	interface UnaryDeserializer {
		boolean matches(int character, DollarParser dollarParser);

		DollarPart parse(DollarParser dollarParser) throws DollarDeserializationException;
	}
}

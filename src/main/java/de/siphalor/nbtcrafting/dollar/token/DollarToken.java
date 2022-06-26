package de.siphalor.nbtcrafting.dollar.token;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DollarToken {
	public final @Nullable Object value;
	public final @NotNull Type type;
	public final int begin;

	public DollarToken(@NotNull Type type, @Nullable Object value, int begin) {
		this.type = type;
		this.value = value;
		this.begin = begin;
	}

	@Override
	public String toString() {
		return type.name() + "(" + value + ") at position " + begin;
	}

	public enum Type {
		LITERAL, STRING, NUMBER,
		INFIX_OPERATOR, PREFIX_OPERATOR, POSTFIX_OPERATOR,
		PARENTHESIS_OPEN, PARENTHESIS_CLOSE,
		BRACKET_OPEN, BRACKET_CLOSE,
		COMMAND_DELIMITER,
		CONDITION_THEN, CONDITION_ELSE,
	}
}

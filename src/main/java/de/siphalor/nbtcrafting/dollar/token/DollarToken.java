package de.siphalor.nbtcrafting.dollar.token;

public class DollarToken {
	public final Object value;
	public final Type type;
	private final int begin;

	public DollarToken(Type type, Object value, int begin) {
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
	}
}

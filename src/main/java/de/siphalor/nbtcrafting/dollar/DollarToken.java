package de.siphalor.nbtcrafting.dollar;

import java.util.SortedMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DollarToken {
	public static final SortedMap<String, Type> SEQUENCES = new Object2ObjectAVLTreeMap<>();
	static {
		//noinspection ResultOfMethodCallIgnored
		DollarToken.Type.values();
	}

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
		DOT("."), OCTOTHORPE("#"),
		ASTERISK("*"), SLASH("/"),
		PLUS("+"), MINUS("-"),
		LESS_THAN("<"), GREATER_THAN(">"),
		LESS_THAN_EQUAL("<="), GREATER_THAN_EQUAL(">="),
		EQUAL("=="), NOT_EQUAL("!="),
		EXCLAMATION("!"),
		AND("&&"), OR("||"), // special, because they short-circuit
		QUESTION("?"), COLON(":"),
		PARENTHESIS_OPEN("("), PARENTHESIS_CLOSE(")"),
		BRACKET_OPEN("["), BRACKET_CLOSE("]");

		Type() {}

		Type(String... sequences) {
			for (String sequence : sequences) {
				SEQUENCES.put(sequence, this);
			}
		}

		public boolean isPrefixOperator() {
			switch (this) {
				case MINUS:
				case EXCLAMATION:
					return true;
			}
			return false;
		}

		public boolean isInfixOperator() {
			switch (this) {
				case DOT:
				case OCTOTHORPE:
				case ASTERISK:
				case SLASH:
				case PLUS:
				case MINUS:
				case LESS_THAN:
				case GREATER_THAN:
				case LESS_THAN_EQUAL:
				case GREATER_THAN_EQUAL:
				case EQUAL:
				case NOT_EQUAL:
				case AND:
				case OR:
				case QUESTION:
					return true;
			}
			return false;
		}

		public boolean isPostfixOperator() {
			return this == Type.BRACKET_OPEN;
		}

		public boolean isStop() {
			switch (this) {
				case PARENTHESIS_CLOSE:
				case BRACKET_CLOSE:
				case COLON:
					return true;
			}
			return false;
		}
	}
}

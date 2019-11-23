package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public enum DollarOperator {
	INVERT,
	SUM;

	static abstract interface Operator extends DollarPart {
		public boolean matchesBegin(int character);
	}

	static abstract class Unary implements Operator {
		final protected DollarPart dollarPart;

		Unary(DollarPart dollarPart) {
			this.dollarPart = dollarPart;
		}

		@Override
		public DollarValue apply(Map<String, CompoundTag> reference) throws DollarException {
			return apply(dollarPart.apply(reference), reference);
		}

		public abstract DollarValue apply(DollarValue value, Map<String, CompoundTag> reference);
	}

	static abstract class Binary implements Operator {
		final protected DollarPart first;
		final protected DollarPart second;

		protected Binary(DollarPart first, DollarPart second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public DollarValue apply(Map<String, CompoundTag> reference) throws DollarException {
			return apply(first.apply(reference), second.apply(reference));
		}

		public abstract DollarValue<Double> apply(DollarValue first, DollarValue second);
	}

	static abstract class Ternary implements Operator {
		final protected DollarPart first;
		final protected DollarPart second;
		final protected DollarPart third;

		protected Ternary(DollarPart first, DollarPart second, DollarPart third) {
			this.first = first;
			this.second = second;
			this.third = third;
		}

		@Override
		public DollarValue apply(Map<String, CompoundTag> reference) throws DollarException {
			return apply(first.apply(reference), second.apply(reference), third.apply(reference));
		}

		public abstract DollarValue apply(DollarValue first, DollarValue second, DollarValue third);
	}

	public static class Invert extends Unary {
		Invert(DollarPart dollarPart) {
			super(dollarPart);
		}

		@Override
		public DollarValue apply(DollarValue value, Map<String, CompoundTag> reference) {
			return null;
		}

		public static Invert read(Reader reader) throws IOException {
			reader.read();
			return new Invert(DollarParser.parse(reader, INVERT));
		}

		@Override
		public boolean matchesBegin(int character) {
			return character == '!';
		}
	}

	public static class Sum extends Binary {
		Sum(DollarPart first, DollarPart second) {
			super(first, second);
		}

		@Override
		public DollarValue apply(DollarValue first, DollarValue second) {
			if(first instanceof DollarValue.NumberValue && second instanceof DollarValue.NumberValue) {
				return new DollarValue.NumberValue<>(((Number) first.getValue()).doubleValue() + ((Number) second.getValue()).doubleValue());
			} else if(first instanceof DollarValue.StringValue || second instanceof DollarValue.StringValue) {
				return new DollarValue.StringValue(first.toString() + second.toString());
			}
			return new DollarValue.StringValue("");
		}

		public static Sum read(DollarPart first, Reader reader) throws IOException {
			reader.read();
			return new Sum(first, DollarParser.parse(reader, SUM));
		}

		@Override
		public boolean matchesBegin(int character) {
			return character == '+';
		}
	}
}

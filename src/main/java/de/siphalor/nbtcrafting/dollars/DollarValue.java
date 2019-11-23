package de.siphalor.nbtcrafting.dollars;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public abstract class DollarValue<T> implements DollarPart {
	protected T value;

	public DollarValue(T value) {
		this.value = value;
	}

	@Override
	public DollarValue apply(Map<String, CompoundTag> reference) throws DollarException {
		return this;
	}

	public T getValue() {
		return value;
	}

	public String toString() {
		return value.toString();
	}

	public abstract boolean isTrue();

	public static class NumberValue<T extends Number> extends DollarValue<T> {
		public NumberValue(T value) {
			super(value);
		}

		@Override
		public boolean isTrue() {
			return value.doubleValue() != 0D;
		}
	}

	public static class StringValue extends DollarValue<String> {
		public StringValue(String value) {
			super(value);
		}

		@Override
		public String toString() {
			return value;
		}

		@Override
		public boolean isTrue() {
			return !"".equals(value);
		}
	}
}

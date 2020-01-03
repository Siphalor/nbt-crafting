package de.siphalor.nbtcrafting.dollar.part;

public class ValueDollarPart extends ConstantDollarPart {
	private final Object value;

	private ValueDollarPart(Object value) {
		this.value = value;
	}

	public static ValueDollarPart of(Object value) {
		return new ValueDollarPart(value);
	}

	@Override
	public boolean isConstant() {
		return true;
	}

	@Override
	public Object getValue() {
		return value;
	}
}

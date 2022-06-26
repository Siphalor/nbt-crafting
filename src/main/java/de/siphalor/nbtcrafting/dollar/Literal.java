package de.siphalor.nbtcrafting.dollar;

public class Literal {
	public final String value;

	public Literal(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Literal(" + value + ")";
	}
}

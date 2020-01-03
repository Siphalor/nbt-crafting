package de.siphalor.nbtcrafting.dollar;

public class DollarDeserializationException extends DollarException {
	public DollarDeserializationException(String message) {
		super(message);
	}
	public DollarDeserializationException(Exception e) {
		super(e);
	}
}

package de.siphalor.nbtcrafting.dollar;

public class DollarException extends Exception {
	public DollarException(String message) {
		super(message);
	}
	public DollarException(Exception e) {
		super(e);
	}
}

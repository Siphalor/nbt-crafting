package de.siphalor.nbtcrafting.dollar;

public class DollarEvaluationException extends DollarException {
	public DollarEvaluationException(String message) {
		super(message);
	}
	public DollarEvaluationException(Exception e) {
		super(e);
	}
}

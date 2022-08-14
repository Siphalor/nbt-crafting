package de.siphalor.nbtcrafting.dollar.exception;

public class UnresolvedDollarReferenceException extends DollarEvaluationException {
	public UnresolvedDollarReferenceException(String reference) {
		super("Failed to resolve dollar reference to \"" + reference + "\"");
	}
}

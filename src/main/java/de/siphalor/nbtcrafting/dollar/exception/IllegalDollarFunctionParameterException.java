package de.siphalor.nbtcrafting.dollar.exception;

public class IllegalDollarFunctionParameterException extends DollarException {
	public IllegalDollarFunctionParameterException(String message) {
		super(message);
	}

	public IllegalDollarFunctionParameterException(Exception e) {
		super(e);
	}
}

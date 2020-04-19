package de.siphalor.nbtcrafting.dollar.part.operator;

import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;

import java.util.Map;

public abstract class BinaryDollarOperator implements DollarPart {
	private DollarPart first;
	private DollarPart second;

	public BinaryDollarOperator(DollarPart first, DollarPart second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public Object evaluate(Map<String, Object> reference) throws DollarEvaluationException {
		return apply(first.evaluate(reference), second.evaluate(reference));
	}

	public abstract Object apply(Object first, Object second) throws DollarEvaluationException;
}

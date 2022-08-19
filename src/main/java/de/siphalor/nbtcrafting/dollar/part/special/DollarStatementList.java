package de.siphalor.nbtcrafting.dollar.part.special;

import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class DollarStatementList implements DollarPart {
	private final DollarPart[] statements;

	private DollarStatementList(DollarPart[] statements) {
		this.statements = statements;
	}

	public static DollarPart of(DollarPart[] statements) {
		return new DollarStatementList(statements);
	}

	@Override
	public Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		Object result = null;
		for (DollarPart statement : statements) {
			result = statement.evaluate(referenceResolver);
		}
		return result;
	}
}

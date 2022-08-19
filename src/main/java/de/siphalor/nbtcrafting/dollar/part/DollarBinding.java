package de.siphalor.nbtcrafting.dollar.part;

import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public interface DollarBinding extends DollarPart {
	void assign(ReferenceResolver referenceResolver, Object value) throws DollarEvaluationException;
}

package de.siphalor.nbtcrafting.dollar.part.binary;

import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarBinding;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class AssignmentDollarPart implements DollarPart {
	private final DollarBinding binding;
	private final DollarPart value;

	private AssignmentDollarPart(DollarBinding binding, DollarPart value) {
		this.binding = binding;
		this.value = value;
	}

	public static DollarPart of(DollarBinding binding, DollarPart value) {
		return new AssignmentDollarPart(binding, value);
	}

	@Override
	public Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		Object value = this.value.evaluate(referenceResolver);
		binding.assign(referenceResolver, value);
		return value;
	}
}

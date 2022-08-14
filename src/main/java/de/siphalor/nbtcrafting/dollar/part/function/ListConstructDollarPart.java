package de.siphalor.nbtcrafting.dollar.part.function;

import java.util.ArrayList;

import de.siphalor.nbtcrafting.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ConstantDollarPart;
import de.siphalor.nbtcrafting.dollar.part.value.ValueDollarPart;
import de.siphalor.nbtcrafting.dollar.reference.ReferenceResolver;

public class ListConstructDollarPart implements DollarPart {
	private final DollarPart[] parts;

	private ListConstructDollarPart(DollarPart[] parts) {
		this.parts = parts;
	}

	public static DollarPart of(DollarPart... parts) throws DollarDeserializationException {
		ListConstructDollarPart instance = new ListConstructDollarPart(parts);
		for (DollarPart part : parts) {
			if (!(part instanceof ConstantDollarPart)) {
				return instance;
			}
		}
		try {
			return ValueDollarPart.of(instance.evaluate(null));
		} catch (DollarEvaluationException e) {
			throw new DollarDeserializationException("Failed to short-circuit dollar list construct", e);
		}
	}

	@Override
	public Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		ArrayList<Object> result = new ArrayList<>(parts.length);
		for (DollarPart part : parts) {
			result.add(part.evaluate(referenceResolver));
		}
		return result;
	}
}

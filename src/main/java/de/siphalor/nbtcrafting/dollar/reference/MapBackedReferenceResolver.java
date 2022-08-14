package de.siphalor.nbtcrafting.dollar.reference;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting.dollar.exception.UnresolvedDollarReferenceException;

public class MapBackedReferenceResolver implements ReferenceResolver {
	private final Map<String, Object> references;

	public MapBackedReferenceResolver(Map<String, Object> references) {
		this.references = references;
	}

	@Nullable
	@Override
	public Object resolve(String reference) throws UnresolvedDollarReferenceException {
		Object value = references.get(reference);
		if (value == null && !references.containsKey(reference)) {
			throw new UnresolvedDollarReferenceException(reference);
		}
		return value;
	}
}

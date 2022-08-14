package de.siphalor.nbtcrafting.dollar.reference;

import javax.annotation.Nullable;

import de.siphalor.nbtcrafting.dollar.exception.UnresolvedDollarReferenceException;

@FunctionalInterface
public interface ReferenceResolver {
	/**
	 * Resolves a dollar reference to a value.
	 * @param reference the dollar reference to resolve
	 * @return the associated value (may be {@code null})
	 * @throws UnresolvedDollarReferenceException if the reference could not be resolved
	 */
	@Nullable
	Object resolve(String reference) throws UnresolvedDollarReferenceException;
}

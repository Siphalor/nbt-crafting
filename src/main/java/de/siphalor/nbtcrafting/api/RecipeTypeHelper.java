package de.siphalor.nbtcrafting.api;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashSet;

public class RecipeTypeHelper {
	private static final Collection<Identifier> SYNC_BLACKLIST = new HashSet<>();

	/**
	 * Add a recipe type to the synchronization blacklist. These recipe types won't get synced by Fabric
	 *
	 * @param recipeTypeIdentifier the registry identifier
	 */
	public static void addToSyncBlacklist(Identifier recipeTypeIdentifier) {
		SYNC_BLACKLIST.add(recipeTypeIdentifier);
	}

	public static Collection<Identifier> getSyncBlacklist() {
		return SYNC_BLACKLIST;
	}
}

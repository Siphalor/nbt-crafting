/*
 * Copyright 2020 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

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

/*
 * Copyright 2020-2021 Siphalor
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

package de.siphalor.nbtcrafting.api.recipe;

import java.util.Map;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;

/**
 * An interface to use in exchange of {@link Recipe} which provides base functions used for remainder computation.
 *
 * @param <I>
 */
public interface NBTCRecipe<I extends Inventory> extends Recipe<I> {
	/**
	 * Builds the reference map used for dollar computation.
	 *
	 * @param inv the inventory for that this method is being called
	 * @return A map consisting of keys and belonging {@link net.minecraft.nbt.NbtCompound}s, {@link Number}s or {@link String}s
	 */
	Map<String, Object> buildDollarReference(I inv);
}

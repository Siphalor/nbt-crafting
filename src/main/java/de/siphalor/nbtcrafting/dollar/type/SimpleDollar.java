/*
 * Copyright 2020-2022 Siphalor
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

package de.siphalor.nbtcrafting.dollar.type;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import de.siphalor.nbtcrafting.api.nbt.NbtException;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.exception.DollarException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;

public class SimpleDollar extends Dollar {
	protected final String path;

	public SimpleDollar(DollarPart expression, String path) {
		super(expression);
		this.path = path;
	}

	@Override
	public void apply(ItemStack stack, Map<String, Object> references) throws DollarException {
		CompoundTag compoundTag = stack.getOrCreateTag();
		String[] pathParts = NbtUtil.splitPath(path);
		try {
			Tag value = evaluate(references);
			NbtUtil.put(compoundTag, pathParts, value);
		} catch (NbtException e) {
			e.printStackTrace();
		}
	}
}

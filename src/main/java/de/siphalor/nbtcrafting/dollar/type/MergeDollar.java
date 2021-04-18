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

package de.siphalor.nbtcrafting.dollar.type;

import com.mojang.datafixers.util.Pair;
import de.siphalor.nbtcrafting.api.nbt.MergeMode;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarEvaluationException;
import de.siphalor.nbtcrafting.dollar.DollarException;
import de.siphalor.nbtcrafting.dollar.part.DollarPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

public class MergeDollar extends Dollar {
	protected final String path;
	protected final Collection<Pair<Pattern, MergeMode>> mergeModes;

	public MergeDollar(DollarPart expression, String path, Collection<Pair<Pattern, MergeMode>> mergeModes) {
		super(expression);
		this.path = path;
		this.mergeModes = mergeModes;
	}

	@Override
	public void apply(ItemStack stack, Map<String, Object> references) throws DollarException {
		NbtElement value = NbtUtil.asTag(evaluate(references));
		if (!(value instanceof NbtCompound)) {
			throw new DollarEvaluationException("Couldn't set stacks main tag as given dollar expression evaluates to non-object value.");
		} else {
			NbtUtil.mergeInto(stack.getOrCreateTag(), (NbtCompound) value, mergeModes, "");
		}
	}
}

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

package de.siphalor.nbtcrafting3.dollar.type;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import de.siphalor.nbtcrafting3.api.nbt.MergeContext;
import de.siphalor.nbtcrafting3.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting3.dollar.Dollar;
import de.siphalor.nbtcrafting3.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting3.dollar.exception.DollarException;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;
import de.siphalor.nbtcrafting3.dollar.reference.ReferenceResolver;

public class MergeDollar extends Dollar {
	protected final String path;
	protected final MergeContext mergeContext;

	public MergeDollar(DollarPart expression, String path, MergeContext mergeContext) {
		super(expression);
		this.path = path;
		this.mergeContext = mergeContext;
	}

	@Override
	public void apply(ItemStack stack, ReferenceResolver referenceResolver) throws DollarException {
		Tag value = NbtUtil.asTag(evaluate(referenceResolver));
		if (!(value instanceof CompoundTag)) {
			throw new DollarEvaluationException("Couldn't set stacks main tag as given dollar expression evaluates to non-object value.");
		} else {
			NbtUtil.mergeInto(stack.getOrCreateTag(), (CompoundTag) value, mergeContext, "");
		}
	}
}

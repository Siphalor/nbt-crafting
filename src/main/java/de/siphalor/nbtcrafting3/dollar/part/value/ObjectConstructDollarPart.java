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

package de.siphalor.nbtcrafting3.dollar.part.value;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Pair;

import de.siphalor.nbtcrafting3.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting3.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting3.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;
import de.siphalor.nbtcrafting3.dollar.reference.ReferenceResolver;

public class ObjectConstructDollarPart implements DollarPart {
	private final Pair<String, DollarPart>[] properties;

	private ObjectConstructDollarPart(Pair<String, DollarPart>[] properties) {
		this.properties = properties;
	}

	@SafeVarargs
	public static DollarPart of(Pair<String, DollarPart>... properties) throws DollarDeserializationException {
		ObjectConstructDollarPart instance = new ObjectConstructDollarPart(properties);
		for (Pair<String, DollarPart> property : properties) {
			if (!(property.getRight() instanceof ConstantDollarPart)) {
				return instance;
			}
		}
		try {
			return ValueDollarPart.of(instance.evaluate(null));
		} catch (DollarEvaluationException e) {
			throw new DollarDeserializationException("Failed to short-circuit dollar object construct", e);
		}
	}

	@Override
	public Object evaluate(ReferenceResolver referenceResolver) throws DollarEvaluationException {
		CompoundTag compound = new CompoundTag();
		for (Pair<String, DollarPart> property : properties) {
			compound.put(property.getLeft(), NbtUtil.asTag(property.getRight().evaluate(referenceResolver)));
		}
		return compound;
	}
}

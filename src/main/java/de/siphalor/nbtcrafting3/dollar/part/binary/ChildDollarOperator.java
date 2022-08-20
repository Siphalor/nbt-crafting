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

package de.siphalor.nbtcrafting3.dollar.part.binary;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import de.siphalor.nbtcrafting3.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting3.dollar.DollarUtil;
import de.siphalor.nbtcrafting3.dollar.exception.DollarDeserializationException;
import de.siphalor.nbtcrafting3.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting3.dollar.part.DollarBinding;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;
import de.siphalor.nbtcrafting3.dollar.reference.ReferenceResolver;

public class ChildDollarOperator extends BinaryDollarOperator implements DollarBinding {
	private final boolean propagateEmpty;

	public ChildDollarOperator(DollarPart first, DollarPart second) {
		this(first, second, false);
	}

	public ChildDollarOperator(DollarPart first, DollarPart second, boolean propagateEmpty) {
		super(first, second);
		this.propagateEmpty = propagateEmpty;
	}

	public static DollarPart of(DollarPart first, DollarPart second) throws DollarDeserializationException {
		return of(first, second, false);
	}

	public static DollarPart of(DollarPart first, DollarPart second, boolean coalesce) throws DollarDeserializationException {
		return new ChildDollarOperator(first, second, coalesce);
	}

	@Override
	public Object apply(Object first, Object second) throws DollarEvaluationException {
		if (first instanceof ItemStack) {
			first = NbtUtil.getTagOrEmpty((ItemStack) first);
		}

		if (propagateEmpty && DollarUtil.isEmpty(first)) {
			return first;
		}

		if (first instanceof CompoundTag) {
			String key = DollarUtil.asString(second);
			if (((CompoundTag) first).contains(key)) {
				return NbtUtil.toDollarValue(((CompoundTag) first).get(key));
			}
			return null;
		} else if (first instanceof List) {
			if (second instanceof Number) {
				int index = ((Number) second).intValue();
				if (index < 0) {
					index = ((List<?>) first).size() + index;
				}
				if (index < ((List<?>) first).size()) {
					return DollarUtil.toDollarValue(((List<?>) first).get(index));
				}
				return null;
			} else if (second instanceof String) {
				List<Object> results = new ArrayList<>(((List<?>) first).size());
				for (Object element : ((List<?>) first)) {
					if (element instanceof CompoundTag) {
						CompoundTag tag = (CompoundTag) element;
						if (tag.contains((String) second)) {
							results.add(NbtUtil.toDollarValue(tag.get((String) second)));
						} else {
							throw new DollarEvaluationException("Tag " + tag + " does not contain key " + second);
						}
					} else {
						throw new DollarEvaluationException("Cannot access child of " + first.getClass().getSimpleName() + " with key " + second);
					}
				}
				return results;
			}
		}

		throw new DollarEvaluationException("Cannot access element " + DollarUtil.asString(second) + " on value " + DollarUtil.asString(first));
	}

	@Override
	public void assign(ReferenceResolver referenceResolver, Object value) throws DollarEvaluationException {
		Object parent = first.evaluate(referenceResolver);
		if (parent instanceof ItemStack) {
			parent = ((ItemStack) parent).getOrCreateTag();
		}

		if (parent instanceof CompoundTag) {
			if (second == null) {
				throw new DollarEvaluationException("Cannot use anonymous child access on compound tags");
			}
			((CompoundTag) parent).put(
					DollarUtil.asString(second.evaluate(referenceResolver)),
					NbtUtil.asTag(value)
			);
		} else if (parent instanceof List) {
			int index;
			if (second == null) {
				index = ((List<?>) parent).size();
			} else {
				Object key = second.evaluate(referenceResolver);
				if (key instanceof Number) {
					index = ((Number) key).intValue();
				} else {
					throw new DollarEvaluationException("Cannot index list with " + DollarUtil.asString(key));
				}
			}
			if (index < 0) {
				index = ((List<?>) parent).size() + index;
			}
			if (index <= ((List<?>) parent).size()) {
				if (parent instanceof ListTag) {
					if (index == ((List<?>) parent).size()) {
						((ListTag) parent).add(NbtUtil.asTag(value));
					} else {
						((ListTag) parent).set(index, NbtUtil.asTag(value));
					}
				} else {
					if (index == ((List<?>) parent).size()) {
						//noinspection unchecked
						((List<Object>) parent).add(value);
					} else {
						//noinspection unchecked
						((List<Object>) parent).set(index, value);
					}
				}
			}
		} else {
			throw new DollarEvaluationException("Cannot access child on value " + DollarUtil.asString(parent));
		}
	}
}

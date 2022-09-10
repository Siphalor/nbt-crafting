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

package de.siphalor.nbtcrafting3.api.nbt;

import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.siphalor.nbtcrafting3.NbtCrafting;
import de.siphalor.nbtcrafting3.dollar.DollarExtractor;
import de.siphalor.nbtcrafting3.dollar.exception.DollarEvaluationException;
import de.siphalor.nbtcrafting3.dollar.exception.UnresolvedDollarReferenceException;
import de.siphalor.nbtcrafting3.dollar.part.DollarPart;

public interface MergeBehavior {
	MergeBehavior OVERWRITE = (base, addition, mergeContext, path) -> addition.copy();
	MergeBehavior KEEP = (base, addition, mergeContext, path) -> base == null ? addition.copy() : base;
	MergeBehavior UPDATE = (base, addition, mergeContext, path) -> base == null ? null : addition.copy();
	MergeBehavior MERGE = (base, addition, mergeContext, path) -> {
		if (base == null) {
			return addition.copy();
		} else if (NbtUtil.isCompound(base) && NbtUtil.isCompound(addition)) {
			NbtUtil.mergeInto(NbtUtil.asCompoundTag(base), NbtUtil.asCompoundTag(addition), mergeContext, path);
			return base;
		} else if (NbtUtil.isList(base) && NbtUtil.isList(addition)) {
			NbtUtil.mergeInto(NbtUtil.asListTag(base), NbtUtil.asListTag(addition), mergeContext, path);
			return base;
		}
		return base;
	};
	MergeBehavior APPEND = (base, addition, mergeContext, path) -> {
		if (base == null) {
			return addition.copy();
		} else if (NbtUtil.isList(base) && NbtUtil.isList(addition)) {
			NbtUtil.asListTag(base).addAll(NbtUtil.asListTag(addition));
			return base;
		}
		return base;
	};

	Tag merge(@Nullable Tag base, @NotNull Tag addition, @NotNull MergeContext mergeContext, @NotNull String path);

	static MergeBehavior valueOf(String input) {
		switch (input) {
			case "overwrite":
				return OVERWRITE;
			case "keep":
				return KEEP;
			case "update":
				return UPDATE;
			case "merge":
				return MERGE;
			case "append":
				return APPEND;
			default:
				if (input.length() > 1 && input.charAt(0) == '$') {
					DollarPart expression = DollarExtractor.parse(input.substring(1));
					return (base, addition, mergeContext, path) -> {
						try {
							return NbtUtil.asTag(expression.evaluate(ref -> {
								if ("base".equals(ref)) {
									return base;
								} else if ("addition".equals(ref)) {
									return addition;
								}
								throw new UnresolvedDollarReferenceException(ref);
							}));
						} catch (DollarEvaluationException e) {
							NbtCrafting.logError("Error evaluating merge behavior expression (" + input + ") for path " + path + ": " + e.getMessage());
							return base;
						}
					};
				}
				throw new IllegalArgumentException("Unknown merge mode: " + input);
		}
	}
}

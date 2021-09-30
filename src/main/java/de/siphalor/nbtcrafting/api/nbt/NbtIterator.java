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

package de.siphalor.nbtcrafting.api.nbt;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

@FunctionalInterface
public interface NbtIterator {
	boolean process(String path, String key, Tag tag);

	static void iterateTags(Tag tag, NbtIterator nbtIterator) {
		iterateTags(tag, nbtIterator, "");
	}

	static void iterateTags(Tag tag, NbtIterator nbtIterator, String path) {
		if (tag == null) return;
		if (tag instanceof CompoundTag) {
			CompoundTag compoundTag = (CompoundTag) tag;
			if (!path.equals(""))
				path += ".";
			Set<String> remove = new HashSet<>();
			for (String key : compoundTag.getKeys()) {
				Tag currentTag = compoundTag.get(key);
				if (nbtIterator.process(path, key, currentTag)) {
					remove.add(key);
				} else {
					if (NbtUtil.isCompound(currentTag) || NbtUtil.isList(currentTag)) {
						iterateTags(compoundTag.get(key), nbtIterator, path + key);
					}
				}
			}
			for (String key : remove) {
				compoundTag.remove(key);
			}
		} else if (tag instanceof AbstractListTag) {
			//noinspection unchecked
			AbstractListTag<Tag> listTag = (AbstractListTag<Tag>) tag;
			int i = 0;
			for (Iterator<Tag> iterator = listTag.iterator(); iterator.hasNext(); ) {
				Tag currentTag = iterator.next();
				if (nbtIterator.process(path, "[" + i + "]", currentTag)) {
					iterator.remove();
				} else {
					iterateTags(currentTag, nbtIterator, path + "[" + i + "]");
					i++;
				}
			}
		}
	}
}

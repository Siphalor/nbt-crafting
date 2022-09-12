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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

@FunctionalInterface
public interface NbtIterator {
	Action process(String path, String key, NbtElement tag);

	static void iterateTags(NbtElement tag, NbtIterator nbtIterator) {
		iterateTags(tag, nbtIterator, "");
	}

	static void iterateTags(NbtElement tag, NbtIterator nbtIterator, String path) {
		if (tag == null) return;
		if (tag instanceof NbtCompound) {
			NbtCompound compoundTag = (NbtCompound) tag;
			if (!path.equals(""))
				path += ".";
			Set<String> remove = new HashSet<>();
			for (String key : compoundTag.getKeys()) {
				NbtElement currentTag = compoundTag.get(key);
				switch (nbtIterator.process(path, key, currentTag)) {
					case REMOVE:
						remove.add(key);
						break;
					case RECURSE:
						iterateTags(currentTag, nbtIterator, path + key);
						break;
					case SKIP:
						break;
				}
			}
			for (String key : remove) {
				compoundTag.remove(key);
			}
		} else if (tag instanceof AbstractNbtList) {
			//noinspection unchecked
			AbstractNbtList<NbtElement> listTag = (AbstractNbtList<NbtElement>) tag;
			int i = 0;
			for (Iterator<NbtElement> iterator = listTag.iterator(); iterator.hasNext(); ) {
				NbtElement currentTag = iterator.next();
				switch (nbtIterator.process(path, "[" + i + "]", currentTag)) {
					case REMOVE:
						iterator.remove();
						break;
					case RECURSE:
						iterateTags(currentTag, nbtIterator, path + "[" + i + "]");
						break;
					case SKIP:
						break;
				}
			}
		}
	}

	enum Action {
		RECURSE, SKIP, REMOVE
	}
}

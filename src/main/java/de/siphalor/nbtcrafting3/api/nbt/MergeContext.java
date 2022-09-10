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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class MergeContext {
	public static final MergeContext EMPTY = new MergeContext(Collections.emptyList());

	private final Collection<Entry> entries;

	public MergeContext(Collection<Entry> entries) {
		this.entries = entries;
	}

	public static MergeContext parse(String basePath, CompoundTag compound) {
		if (compound.contains("paths", 10)) {
			if (!basePath.isEmpty()) {
				basePath += ".";
			}
			CompoundTag paths = compound.getCompound("paths");
			List<Entry> entries = new ArrayList<>(paths.getSize());
			for (String key : paths.getKeys()) {
				MergeBehavior mergeBehavior = MergeBehavior.valueOf(paths.getString(key));
				if (key.startsWith("/") && key.endsWith("/")) {
					entries.add(new PatternEntry(
							mergeBehavior,
							Pattern.compile(Pattern.quote(basePath) + key.substring(1, key.length() - 1))
					));
				} else {
					entries.add(new SimpleEntry(
							mergeBehavior,
							basePath + key
					));
				}
			}
			return new MergeContext(entries);
		}
		return MergeContext.EMPTY;
	}

	public MergeBehavior getMergeMode(Tag tag, String path) {
		for (Entry entry : entries) {
			if (entry.test(tag, path)) {
				return entry.mergeBehavior;
			}
		}
		return MergeBehavior.MERGE;
	}

	public static abstract class Entry {
		private final MergeBehavior mergeBehavior;

		public Entry(MergeBehavior mergeBehavior) {
			this.mergeBehavior = mergeBehavior;
		}

		public abstract boolean test(Tag tag, String path);
	}

	public static class SimpleEntry extends Entry {
		private final String path;

		public SimpleEntry(MergeBehavior mergeBehavior, String path) {
			super(mergeBehavior);
			this.path = path;
		}

		@Override
		public boolean test(Tag tag, String path) {
			return this.path.equals(path);
		}
	}

	public static class PatternEntry extends Entry {
		private final Pattern pattern;

		public PatternEntry(MergeBehavior mergeBehavior, Pattern pattern) {
			super(mergeBehavior);
			this.pattern = pattern;
		}

		@Override
		public boolean test(Tag tag, String path) {
			return pattern.matcher(path).matches();
		}
	}
}

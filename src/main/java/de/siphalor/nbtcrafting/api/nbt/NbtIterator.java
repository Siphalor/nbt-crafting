package de.siphalor.nbtcrafting.api.nbt;

import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

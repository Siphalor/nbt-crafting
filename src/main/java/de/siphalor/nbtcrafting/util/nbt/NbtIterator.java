package de.siphalor.nbtcrafting.util.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
		if(tag == null) return;
		if(tag instanceof CompoundTag) {
			CompoundTag compoundTag = (CompoundTag) tag;
			if(!path.equals(""))
				path += ".";
			Set<String> remove = new HashSet<>();
			for(String key : compoundTag.getKeys()) {
				if(compoundTag.get(key) instanceof CompoundTag || compoundTag.get(key) instanceof ListTag)
					iterateTags(compoundTag.get(key), nbtIterator, path + key);
				else {
					if(nbtIterator.process(path, key, compoundTag.get(key)))
                        remove.add(key);
				}
			}
			for(String key : remove) {
				compoundTag.remove(key);
			}
		} else if(tag instanceof ListTag) {
			ListTag listTag = (ListTag) tag;
			int i = 0;
			for(Iterator<Tag> iterator = listTag.iterator(); iterator.hasNext(); ) {
				Tag currentTag = iterator.next();
				if(currentTag instanceof CompoundTag || currentTag instanceof ListTag) {
					iterateTags(currentTag, nbtIterator, path + "[" + i + "]");
					i++;
				} else {
					if(nbtIterator.process(path, "[" + i + "]", currentTag)) {
						iterator.remove();
					} else {
						i++;
					}
				}
			}
		}
	}
}

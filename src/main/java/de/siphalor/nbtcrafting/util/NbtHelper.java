package de.siphalor.nbtcrafting.util;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Iterator;
import java.util.Objects;

public class NbtHelper {
	
	public static boolean tagsMatch(Tag main, Tag reference) {
		if(isString(reference) && reference.asString().equals(""))
			return true;
		if(isString(main) && isString(reference))
			return main.asString().equals(reference.asString());
		if(isNumeric(main)) {
			if(isNumeric(reference))
				return ((AbstractNumberTag) main).getDouble() == ((AbstractNumberTag) reference).getDouble();
			if(isString(reference) && reference.asString().startsWith("$"))
				return NbtNumberRange.ofString(reference.asString().substring(1)).matches(((AbstractNumberTag) main).getDouble());
			return false;
		}
		return false;
	}

	public static boolean compoundsOverlap(CompoundTag main, CompoundTag reference) {
		for(String key : main.getKeys()) {
			if(!reference.containsKey(key))
				continue;
			if(isCompound(main.getTag(key)) && isCompound(reference.getTag(key))) {
				if(compoundsOverlap(main.getCompound(key), reference.getCompound(key)))
					return true;
			} else if(isList(main.getTag(key)) && isList(reference.getTag(key))) {
				if(listsOverlap((ListTag) main.getTag(key), (ListTag) reference.getTag(key)))
					return true;
			} else if(tagsMatch(main.getTag(key), reference.getTag(key))) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean listsOverlap(ListTag main, ListTag reference) {
		for (Iterator<Tag> it_1 = main.iterator(); it_1.hasNext();) {
			Tag mainTag = it_1.next();
			for (Iterator<Tag> it_2 = main.iterator(); it_2.hasNext();) {
				Tag referenceTag = it_2.next();
				if(isCompound(mainTag) && isCompound(referenceTag)) {
					if(compoundsOverlap((CompoundTag) mainTag, (CompoundTag) referenceTag))
						return true;
				} else if(isList(mainTag) && isList(referenceTag)) {
					if(listsOverlap((ListTag) mainTag, (ListTag) referenceTag))
						return true;
				} else if(tagsMatch(mainTag, referenceTag)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isCompoundContained(CompoundTag inner, CompoundTag outer) {
		for(String key : inner.getKeys()) {
			Tag innerTag = inner.getTag(key);
			if(!outer.containsKey(key))
				return false;
			Tag outerTag = outer.getTag(key);
			if(isCompound(innerTag) && isCompound(outerTag)) {
				if(isCompoundContained((CompoundTag) innerTag, (CompoundTag) outerTag))
					continue;
				return false;
			} else if(isList(innerTag) && isList(outerTag)) {
				if(isListContained((ListTag) innerTag, (ListTag) outerTag))
					continue;
				return false;
			} else if(tagsMatch(outerTag, innerTag))
				continue;
			return false;
		}
		return true;
	}
	
	public static boolean isListContained(ListTag inner, ListTag outer) {
		for (Iterator<Tag> it_1 = inner.iterator(); it_1.hasNext();) {
			Tag innerTag = it_1.next();
			boolean success = false;
			for (Iterator<Tag> it_2 = outer.iterator(); it_2.hasNext();) {
				Tag outerTag = it_2.next();
				if(isCompound(innerTag) && isCompound(outerTag) && isCompoundContained((CompoundTag) innerTag, (CompoundTag) outerTag)) {
					success = true;
					break;
				} else if(isList(innerTag) && isList(outerTag) && isListContained((ListTag) innerTag, (ListTag) outerTag)) {
					success = true;
					break;
				} else if(tagsMatch(innerTag, outerTag)) {
					success = true;
					break;
				}
			}
			if(!success)
				return false;
		}
		return true;
	}
	
	public static boolean sameType(Tag tag1, Tag tag2) {
		return Objects.equals(NbtOps.INSTANCE.getType(tag1), NbtOps.INSTANCE.getType(tag2));
	}
	
	public static boolean isString(Tag tag) {
		return Objects.equals(NbtOps.INSTANCE.getType(tag), DSL.string());
	}
	
	public static boolean isCompound(Tag tag) {
		return Objects.equals(NbtOps.INSTANCE.getType(tag), DSL.compoundList(DSL.remainderType(), DSL.remainderType()));
	}
	
	public static boolean isList(Tag tag) {
		return Objects.equals(NbtOps.INSTANCE.getType(tag), DSL.list(DSL.remainderType()));
	}
	
	public static boolean isNumeric(Tag tag) {
		Type<?> type = NbtOps.INSTANCE.getType(tag);
		return Objects.equals(type, DSL.byteType()) ||
		       Objects.equals(type, DSL.shortType()) ||
		       Objects.equals(type, DSL.intType()) ||
		       Objects.equals(type, DSL.longType()) ||
		       Objects.equals(type, DSL.floatType()) ||
		       Objects.equals(type, DSL.doubleType());
	}
}

package de.siphalor.nbtcrafting.util;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import de.siphalor.nbtcrafting.dollars.DollarException;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
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
			if(!reference.contains(key))
				continue;
			if(isCompound(main.get(key)) && isCompound(reference.get(key))) {
				if(compoundsOverlap(main.getCompound(key), reference.getCompound(key)))
					return true;
			} else if(isList(main.get(key)) && isList(reference.get(key))) {
				if(listsOverlap((ListTag) main.get(key), (ListTag) reference.get(key)))
					return true;
			} else if(tagsMatch(main.get(key), reference.get(key))) {
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
			Tag innerTag = inner.get(key);
			if(!outer.contains(key))
				return false;
			Tag outerTag = outer.get(key);
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

	public static CompoundTag getParentTagOrCreate(Tag main, String path) throws DollarException {
		Tag currentTag = main;
		String[] pathParts = path.split("\\.|(?=\\[)");
		for (int i = 0; i < pathParts.length - 1; i++) {
			if(pathParts[i].charAt(0) == '[') {
				if(!isList(currentTag)) {
					throw new DollarException(path + " doesn't match on " + main.asString());
				}
				ListTag currentList = (ListTag) currentTag;
                int index = Integer.parseUnsignedInt(pathParts[i].substring(1, pathParts[i].length()));
                if(currentList.size() <= index) {
                	throw new DollarException(path + " contains invalid list in " + main.asString());
                } else if(isCompound(currentList.get(index)) || isList(currentList.get(index))) {
                	currentTag = currentList.get(index);
                } else {
	                throw new DollarException(path + " doesn't match on " + main.asString());
                }
			} else {
				if(!isCompound(currentTag)) {
					throw new DollarException(path + " doesn't match on " + main.asString());
				}
				CompoundTag currentCompound = (CompoundTag) currentTag;
				if(!currentCompound.contains(pathParts[i])) {
					CompoundTag newCompound = new CompoundTag();
					currentCompound.put(pathParts[i], newCompound);
					currentTag = newCompound;
				} else if(isCompound(currentCompound.get(pathParts[i])) || isList(currentCompound.get(pathParts[i]))) {
					currentTag = currentCompound.get(pathParts[i]);
				} else {
					throw new DollarException(path + " doesn't match on " + main.asString());
				}
			}
		}
		if(!isCompound(currentTag))
			throw new DollarException(path + "'s parent does not specify an object in " + main.asString());
		return (CompoundTag) currentTag;
	}

	public static String getLastKey(String path) {
		return path.substring(path.lastIndexOf('.') + 1);
	}

	public static void iterateTags(Tag tag, BiFunction<String, Tag, Boolean> biFunction) {
		iterateTags(tag, biFunction, "");
	}

	private static void iterateTags(Tag tag, BiFunction<String, Tag, Boolean> biFunction, String path) {
		if(tag == null) return;
		if(isCompound(tag)) {
			CompoundTag compoundTag = (CompoundTag) tag;
			if(path != "")
				path += ".";
			Set<String> remove = new HashSet<>();
			for(String key : compoundTag.getKeys()) {
				if(isCompound(compoundTag.get(key)) || isList(compoundTag.get(key)))
					iterateTags(compoundTag.get(key), biFunction, path + key);
				else {
					if(biFunction.apply(path + key, compoundTag.get(key)))
                        remove.add(key);
				}
			}
			for(String key : remove) {
				compoundTag.remove(key);
			}
		} else if(isList(tag)) {
			ListTag listTag = (ListTag) tag;
			for(int i = 0; i < listTag.size(); i++) {
				if(isCompound(listTag.get(i)) || isList(listTag.get(i))) {
					iterateTags(listTag.get(i), biFunction, path + "[" + i + "]");
				} else {
					if(biFunction.apply(path + "[" + i + "]", listTag.get(i)))
						listTag.remove(i);
				}
			}
		}
	}

	public static CompoundTag mergeInto(CompoundTag target, CompoundTag additions, boolean replace) {
		if(target == null) {
			if(additions == null)
				return new CompoundTag();
			return additions;
		}
		if(additions == null) return target;

		for(String key : additions.getKeys()) {
			if(!target.contains(key)) {
				target.put(key, additions.get(key).copy());
				continue;
			}

            Tag targetTag = target.get(key);
			Tag additionsTag = target.get(key);
			if(isCompound(targetTag) && isCompound(additionsTag)) {
				mergeInto((CompoundTag) targetTag, (CompoundTag) additionsTag, replace);
			} else if(isList(targetTag) && isList(additionsTag)) {
				((ListTag) targetTag).addAll(((ListTag) additionsTag).stream().map(Tag::copy).collect(Collectors.toList()));
			} else {
				if(replace)
					target.put(key, additionsTag.copy());
			}
		}

        return target;
	}
}

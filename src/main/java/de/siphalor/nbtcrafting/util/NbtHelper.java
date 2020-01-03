package de.siphalor.nbtcrafting.util;

import de.siphalor.nbtcrafting.dollars.DollarException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class NbtHelper {
	public static final CompoundTag EMPTY_COMPOUND = new CompoundTag();

	public static CompoundTag getTagOrEmpty(ItemStack itemStack) {
		if(itemStack.hasTag())
			return itemStack.getTag();
		else
			return EMPTY_COMPOUND;
	}

	public static CompoundTag copyOrEmpty(CompoundTag compoundTag) {
		if(compoundTag == null)
			return EMPTY_COMPOUND;
		else
			return compoundTag.copy();
	}
	
	public static boolean tagsMatch(Tag main, Tag reference) {
		if(reference instanceof StringTag && reference.asString().equals(""))
			return true;
		if(main instanceof StringTag && reference instanceof StringTag)
			return main.asString().equals(reference.asString());
		if(main instanceof AbstractNumberTag) {
			if(reference instanceof AbstractNumberTag)
				return ((AbstractNumberTag) main).getDouble() == ((AbstractNumberTag) reference).getDouble();
			if(reference instanceof StringTag && reference.asString().startsWith("$"))
				return NbtNumberRange.ofString(reference.asString().substring(1)).matches(((AbstractNumberTag) main).getDouble());
			return false;
		}
		return false;
	}

	public static boolean compoundsOverlap(CompoundTag main, CompoundTag reference) {
		for(String key : main.getKeys()) {
			if(!reference.contains(key))
				continue;
			if(main.get(key) instanceof CompoundTag && reference.get(key) instanceof CompoundTag) {
				if(compoundsOverlap(main.getCompound(key), reference.getCompound(key)))
					return true;
			} else if(main.get(key) instanceof ListTag && reference.get(key) instanceof ListTag) {
				//noinspection ConstantConditions
				if(listsOverlap((ListTag) main.get(key), (ListTag) reference.get(key)))
					return true;
			} else if(tagsMatch(main.get(key), reference.get(key))) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean listsOverlap(ListTag main, ListTag reference) {
		for(Tag mainTag : main) {
			for(Tag referenceTag : main) {
				if(mainTag instanceof CompoundTag && referenceTag instanceof CompoundTag) {
					if(compoundsOverlap((CompoundTag) mainTag, (CompoundTag) referenceTag))
						return true;
				} else if(mainTag instanceof ListTag && referenceTag instanceof ListTag) {
					if (listsOverlap((ListTag) mainTag, (ListTag) referenceTag))
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
			if(innerTag instanceof CompoundTag && outerTag instanceof CompoundTag) {
				if(isCompoundContained((CompoundTag) innerTag, (CompoundTag) outerTag))
					continue;
				return false;
			} else if(innerTag instanceof ListTag && outerTag instanceof ListTag) {
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
		for(Tag innerTag : inner) {
			boolean success = false;
			for(Tag outerTag : outer) {
				if(innerTag instanceof CompoundTag && outerTag instanceof CompoundTag && isCompoundContained((CompoundTag) innerTag, (CompoundTag) outerTag)) {
					success = true;
					break;
				} else if(innerTag instanceof ListTag && outerTag instanceof ListTag && isListContained((ListTag) innerTag, (ListTag) outerTag)) {
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
		return tag1.getType() == tag2.getType();
	}

	public static CompoundTag getParentTagOrCreate(Tag main, String path) throws DollarException {
		Tag currentTag = main;
		String[] pathParts = path.split("\\.|(?=\\[)");
		for (int i = 0; i < pathParts.length - 1; i++) {
			if(pathParts[i].charAt(0) == '[') {
				if(!(currentTag instanceof ListTag)) {
					throw new DollarException(path + " doesn't match on " + main.asString());
				}
				ListTag currentList = (ListTag) currentTag;
                int index = Integer.parseUnsignedInt(pathParts[i].substring(1, pathParts[i].length() - 1));
				if(currentList.size() <= index) {
                	throw new DollarException(path + " contains invalid list in " + main.asString());
                } else if(currentList.get(index) instanceof CompoundTag || currentList.get(index) instanceof ListTag) {
                	currentTag = currentList.get(index);
                } else {
	                throw new DollarException(path + " doesn't match on " + main.asString());
                }
			} else {
				if(!(currentTag instanceof CompoundTag)) {
					throw new DollarException(path + " doesn't match on " + main.asString());
				}
				CompoundTag currentCompound = (CompoundTag) currentTag;
				if(!currentCompound.contains(pathParts[i])) {
					CompoundTag newCompound = new CompoundTag();
					currentCompound.put(pathParts[i], newCompound);
					currentTag = newCompound;
				} else if(currentCompound.get(pathParts[i]) instanceof CompoundTag || currentCompound.get(pathParts[i]) instanceof ListTag) {
					currentTag = currentCompound.get(pathParts[i]);
				} else {
					throw new DollarException(path + " doesn't match on " + main.asString());
				}
			}
		}
		if(!(currentTag instanceof CompoundTag))
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
		if(tag instanceof CompoundTag) {
			CompoundTag compoundTag = (CompoundTag) tag;
			if(!path.equals(""))
				path += ".";
			Set<String> remove = new HashSet<>();
			for(String key : compoundTag.getKeys()) {
				if(compoundTag.get(key) instanceof CompoundTag || compoundTag.get(key) instanceof ListTag)
					iterateTags(compoundTag.get(key), biFunction, path + key);
				else {
					if(biFunction.apply(path + key, compoundTag.get(key)))
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
					iterateTags(currentTag, biFunction, path + "[" + i + "]");
					i++;
				} else {
					if(biFunction.apply(path + "[" + i + "]", currentTag)) {
						iterator.remove();
					} else {
						i++;
					}
				}

			}
		}
	}

	public static void mergeInto(CompoundTag target, CompoundTag additions, boolean replace) {
		if(additions == null) return;

		for(String key : additions.getKeys()) {
			if(!target.contains(key)) {
				//noinspection ConstantConditions
				target.put(key, additions.get(key).copy());
				continue;
			}

            Tag targetTag = target.get(key);
			Tag additionsTag = target.get(key);
			if(targetTag instanceof CompoundTag && additionsTag instanceof CompoundTag) {
				mergeInto((CompoundTag) targetTag, (CompoundTag) additionsTag, replace);
			} else if(targetTag instanceof ListTag && additionsTag instanceof ListTag) {
				((ListTag) targetTag).addAll(((ListTag) additionsTag).stream().map(Tag::copy).collect(Collectors.toList()));
			} else {
				if(replace)
					//noinspection ConstantConditions
					target.put(key, additionsTag.copy());
			}
		}
	}
}

package de.siphalor.nbtcrafting.api.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class NbtUtil {
	public static final CompoundTag EMPTY_COMPOUND = new CompoundTag();

	public static CompoundTag getTagOrEmpty(ItemStack itemStack) {
		if(itemStack.hasTag())
			return itemStack.getTag();
		else
			return EMPTY_COMPOUND;
	}

	public static CompoundTag copyOrEmpty(CompoundTag compoundTag) {
		if(compoundTag == null)
			return new CompoundTag();
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
	
	public static boolean isString(Tag tag) {
		return tag instanceof StringTag;
	}

	public static boolean isCompound(Tag tag) {
		return tag instanceof CompoundTag;
	}

	public static boolean isList(Tag tag) {
		return tag instanceof ListTag;
	}

	public static boolean isNumeric(Tag tag) {
		return tag instanceof AbstractNumberTag;
	}

	public static String asString(Tag tag) {
		if(tag instanceof AbstractNumberTag) {
			return ((AbstractNumberTag) tag).getNumber().toString();
		} else if(tag instanceof StringTag) {
			return tag.asString();
		} else if(tag instanceof ListTag) {
			return ((ListTag) tag).stream().map(NbtUtil::asString).collect(Collectors.joining(", "));
		} else {
			return tag.toString();
		}
	}

	public static Tag getTag(Tag main, String path) {
		return getTag(main, splitPath(path));
	}

	public static Tag getTag(Tag main, String[] pathKeys) {
		Tag currentTag = main;
		for(String pathKey : pathKeys) {
			if("".equals(pathKey))
				continue;
			if(currentTag == null)
				return null;
			if(pathKey.charAt(0) == '[') {
				int index = Integer.parseUnsignedInt(pathKey.substring(1, pathKey.length() - 2), 10);
				if(currentTag instanceof ListTag) {
					ListTag list = (ListTag) currentTag;
					if(index >= list.size())
						return null;
					else
						currentTag = list.get(index);
				} else {
					return null;
				}
			} else {
				if(currentTag instanceof CompoundTag) {
					CompoundTag compound = (CompoundTag) currentTag;
					if(compound.contains(pathKey)) {
						currentTag = compound.get(pathKey);
					} else {
						return null;
					}
				} else {
					return null;
				}
			}
		}
		return currentTag;
	}

	public static CompoundTag getTagOrCreate(Tag main, String path) throws NbtException {
		return getTagOrCreate(main, splitPath(path));
	}

	public static CompoundTag getTagOrCreate(Tag main, String[] pathParts) throws NbtException {
		Tag currentTag = main;
		for(String pathPart : pathParts) {
			if("".equals(pathPart))
				continue;
			if(pathPart.charAt(0) == '[') {
				if(!(currentTag instanceof ListTag)) {
					throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
				}
				ListTag currentList = (ListTag) currentTag;
				int index = Integer.parseUnsignedInt(pathPart.substring(1, pathPart.length() - 1));
				if(currentList.size() <= index) {
					throw new NbtException(String.join(".", pathParts) + " contains invalid list in " + main.asString());
				} else if(currentList.get(index) instanceof CompoundTag || currentList.get(index) instanceof ListTag) {
					currentTag = currentList.get(index);
				} else {
					throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
				}
			} else {
				if(!(currentTag instanceof CompoundTag)) {
					throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
				}
				CompoundTag currentCompound = (CompoundTag) currentTag;
				if(!currentCompound.contains(pathPart)) {
					CompoundTag newCompound = new CompoundTag();
					currentCompound.put(pathPart, newCompound);
					currentTag = newCompound;
				} else if(currentCompound.get(pathPart) instanceof CompoundTag || currentCompound.get(pathPart) instanceof ListTag) {
					currentTag = currentCompound.get(pathPart);
				} else {
					throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
				}
			}
		}
		if(!(currentTag instanceof CompoundTag))
			throw new NbtException(String.join(".", pathParts) + "'s parent does not specify an object in " + main.asString());
		return (CompoundTag) currentTag;
	}

	public static String[] splitPath(String path) {
		return path.split("\\.|(?=\\[)");
	}

	public static String getLastKey(String path) {
		return path.substring(path.lastIndexOf('.') + 1);
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
			Tag additionsTag = additions.get(key);
			if(targetTag instanceof CompoundTag && additionsTag instanceof CompoundTag) {
					mergeInto((CompoundTag) targetTag, (CompoundTag) additionsTag, replace);
			} else if(targetTag instanceof ListTag && additionsTag instanceof ListTag) {
				int targetSize = ((ListTag) targetTag).size();
				((ListTag) targetTag).addAll(((ListTag) additionsTag).stream().map(Tag::copy).collect(Collectors.toList()));
			} else {
				if(replace)
					//noinspection ConstantConditions
					target.put(key, additionsTag.copy());
			}
		}
	}

	public static Tag asTag(Object value) {
		if(value instanceof Tag) {
			return (Tag) value;
		} else if(value instanceof String) {
			return StringTag.of((String) value);
		} else if(value instanceof Float) {
			return FloatTag.of((Float) value);
		} else if(value instanceof Double) {
			return DoubleTag.of((Double) value);
		} else if(value instanceof Byte) {
			return ByteTag.of((Byte) value);
		} else if(value instanceof Character) {
			return StringTag.of(String.valueOf(value));
		} else if(value instanceof Short) {
			return ShortTag.of((Short) value);
		} else if(value instanceof Integer) {
			return IntTag.of((Integer) value);
		} else if(value instanceof Long) {
			return LongTag.of((Long) value);
		} else if(value instanceof Boolean) {
			return ByteTag.of((byte) ((Boolean) value ? 1 : 0));
		} else {
			return null;
		}
	}

	public static Object toDollarValue(Tag value) {
		if(value instanceof StringTag) {
			return value.asString();
		} else if(value instanceof FloatTag) {
			return ((FloatTag) value).getFloat();
		} else if(value instanceof DoubleTag) {
			return ((DoubleTag) value).getDouble();
		} else if(value instanceof ByteTag) {
			return ((ByteTag) value).getByte();
		} else if(value instanceof ShortTag) {
			return ((ShortTag) value).getShort();
		} else if(value instanceof IntTag) {
			return ((IntTag) value).getInt();
		} else if(value instanceof LongTag) {
			return ((LongTag) value).getLong();
		} else {
			return null;
		}
	}
}

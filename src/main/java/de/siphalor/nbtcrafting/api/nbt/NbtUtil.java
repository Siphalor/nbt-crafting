package de.siphalor.nbtcrafting.api.nbt;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.util.BetterJsonOps;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.StringJoiner;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class NbtUtil {
	public static final CompoundTag EMPTY_COMPOUND = new CompoundTag();

	public static CompoundTag getTagOrEmpty(ItemStack itemStack) {
		if (itemStack.hasTag())
			return itemStack.getTag();
		else
			return EMPTY_COMPOUND;
	}

	public static CompoundTag copyOrEmpty(CompoundTag compoundTag) {
		if (compoundTag == null)
			return new CompoundTag();
		else
			return compoundTag.copy();
	}

	public static boolean tagsMatch(Tag main, Tag reference) {
		// Empty reference string is treated as wildcard
		if (isString(reference) && reference.asString().equals(""))
			return true;
		if (isString(main) && isString(reference))
			return main.asString().equals(reference.asString());
		if (isNumeric(main)) {
			if (isNumeric(reference))
				return asNumberTag(main).getDouble() == asNumberTag(reference).getDouble();
			// The reference might be a numeric range
			if (isString(reference) && reference.asString().startsWith("$"))
				return NbtNumberRange.ofString(reference.asString().substring(1)).matches(asNumberTag(main).getDouble());
			return false;
		}
		return false;
	}

	public static boolean compoundsOverlap(CompoundTag main, CompoundTag reference) {
		for (String key : main.getKeys()) {
			if (!reference.contains(key))
				continue;
			Tag mainTag = main.get(key);
			Tag refTag = reference.get(key);
			if (isCompound(mainTag) && isCompound(refTag)) {
				if (compoundsOverlap(main.getCompound(key), reference.getCompound(key)))
					return true;
			} else if (isList(mainTag) && isList(refTag)) {
				// noinspection ConstantConditions
				if (listsOverlap(asListTag(main.get(key)), asListTag(reference.get(key))))
					return true;
			} else if (tagsMatch(main.get(key), reference.get(key))) {
				return true;
			}
		}
		return false;
	}

	public static boolean listsOverlap(AbstractListTag<Tag> main, AbstractListTag<Tag> reference) {
		for (Tag mainTag : main) {
			for (Tag referenceTag : main) {
				if (isCompound(mainTag) && isCompound(referenceTag)) {
					if (compoundsOverlap(asCompoundTag(mainTag), asCompoundTag(referenceTag)))
						return true;
				} else if (isList(mainTag) && isList(referenceTag)) {
					if (listsOverlap(asListTag(mainTag), asListTag(referenceTag)))
						return true;
				} else if (tagsMatch(mainTag, referenceTag)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isCompoundContained(CompoundTag inner, CompoundTag outer) {
		for (String key : inner.getKeys()) {
			Tag innerTag = inner.get(key);
			if (!outer.contains(key))
				return false;
			Tag outerTag = outer.get(key);
			if (isCompound(innerTag) && isCompound(outerTag)) {
				if (isCompoundContained(asCompoundTag(innerTag), asCompoundTag(outerTag)))
					continue;
				return false;
			} else if (isList(innerTag) && isList(outerTag)) {
				if (isListContained(asListTag(innerTag), asListTag(outerTag)))
					continue;
				return false;
			} else if (tagsMatch(outerTag, innerTag))
				continue;
			return false;
		}
		return true;
	}

	public static boolean isListContained(AbstractListTag<Tag> inner, AbstractListTag<Tag> outer) {
		for (Tag innerTag : inner) {
			boolean success = false;
			for (Tag outerTag : outer) {
				if (isCompound(innerTag) && isCompound(outerTag) && isCompoundContained(asCompoundTag(innerTag), asCompoundTag(outerTag))) {
					success = true;
					break;
				} else if (isList(innerTag) && isList(outerTag) && isListContained(asListTag(innerTag), asListTag(outerTag))) {
					success = true;
					break;
				} else if (tagsMatch(innerTag, outerTag)) {
					success = true;
					break;
				}
			}
			if (!success)
				return false;
		}
		return true;
	}

	public static boolean sameType(Tag tag1, Tag tag2) {
		return tag1.getType() == tag2.getType();
	}

	@Contract(value = "null -> false", pure = true)
	public static boolean isString(Tag tag) {
		return tag instanceof StringTag;
	}

	@Contract(value = "null -> false", pure = true)
	public static boolean isCompound(Tag tag) {
		return tag instanceof CompoundTag;
	}

	@Contract(value = "null -> false", pure = true)
	public static boolean isList(Tag tag) {
		return tag instanceof AbstractListTag;
	}

	@Contract(value = "null -> false", pure = true)
	public static boolean isNumeric(Tag tag) {
		return tag instanceof AbstractNumberTag;
	}

	public static String asString(Tag tag) {
		if (tag instanceof AbstractNumberTag) {
			return ((AbstractNumberTag) tag).getNumber().toString();
		} else if (tag instanceof StringTag) {
			return tag.asString();
		} else if (tag instanceof ListTag) {
			StringJoiner joiner = new StringJoiner(", ");
			for (Tag entry : ((ListTag) tag)) {
				String s = asString(entry);
				joiner.add(s);
			}
			return joiner.toString();
		} else {
			return tag.toString();
		}
	}

	public static StringTag asStringTag(Tag tag) {
		return (StringTag) tag;
	}

	public static CompoundTag asCompoundTag(Tag tag) {
		return (CompoundTag) tag;
	}

	public static AbstractListTag<Tag> asListTag(Tag tag) {
		//noinspection unchecked
		return (AbstractListTag<Tag>) tag;
	}

	public static AbstractNumberTag asNumberTag(Tag tag) {
		return (AbstractNumberTag) tag;
	}

	public static Tag getTag(Tag main, String path) {
		return getTag(main, splitPath(path));
	}

	public static Tag getTag(Tag main, String[] pathKeys) {
		Tag currentTag = main;
		for (String pathKey : pathKeys) {
			if ("".equals(pathKey))
				continue;
			if (currentTag == null)
				return null;
			if (pathKey.charAt(0) == '[') {
				int index = Integer.parseUnsignedInt(pathKey.substring(1, pathKey.length() - 2), 10);
				if (isList(currentTag)) {
					AbstractListTag<Tag> list = asListTag(currentTag);
					if (index >= list.size())
						return null;
					else
						currentTag = list.get(index);
				} else {
					return null;
				}
			} else {
				if (isCompound(currentTag)) {
					CompoundTag compound = asCompoundTag(currentTag);
					if (compound.contains(pathKey)) {
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

	public static Tag getTagOrCreate(Tag main, String path) throws NbtException {
		return getTagOrCreate(main, splitPath(path));
	}

	public static Tag getTagOrCreate(Tag main, String[] pathParts) throws NbtException {
		Tag currentTag = main;
		for (String pathPart : pathParts) {
			if ("".equals(pathPart))
				continue;
			if (pathPart.charAt(0) == '[') {
				if (!isList(currentTag)) {
					throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
				}
				AbstractListTag<Tag> currentList = asListTag(currentTag);
				int index = Integer.parseUnsignedInt(pathPart.substring(1, pathPart.length() - 1));
				if (currentList.size() <= index) {
					throw new NbtException(String.join(".", pathParts) + " contains invalid list in " + main.asString());
				} else if (isCompound(currentList.get(index)) || isList(currentList.get(index))) {
					currentTag = currentList.get(index);
				} else {
					throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
				}
			} else {
				if (!isCompound(currentTag)) {
					throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
				}
				CompoundTag currentCompound = asCompoundTag(currentTag);
				if (!currentCompound.contains(pathPart)) {
					CompoundTag newCompound = new CompoundTag();
					currentCompound.put(pathPart, newCompound);
					currentTag = newCompound;
				} else if (isCompound(currentCompound.get(pathPart)) || isList(currentCompound.get(pathPart))) {
					currentTag = currentCompound.get(pathPart);
				} else {
					throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
				}
			}
		}
		return currentTag;
	}

	public static void put(Tag main, String[] pathParts, Tag tag) throws NbtException {
		Tag parent = getTagOrCreate(main, ArrayUtils.subarray(pathParts, 0, pathParts.length - 1));

		String key = pathParts[pathParts.length - 1];
		if (key.charAt(0) == '[') {
			int i = Integer.parseUnsignedInt(key.substring(1, key.length() - 1));

			if (isList(parent)) {
				if (tag == null) {
					asListTag(parent).remove(i);
				} else {
					try {
						asListTag(parent).add(i, tag);
					} catch (Exception e) {
						throw new NbtException("Can't add tag " + tag.asString() + " to list: " + parent.asString());
					}
				}
			} else {
				throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
			}
		} else {
			if (isCompound(parent)) {
				if (tag == null) {
					asCompoundTag(parent).remove(key);
				} else {
					asCompoundTag(parent).put(key, tag);
				}
			} else {
				throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
			}
		}
	}

	public static String[] splitPath(String path) {
		return path.split("\\.|(?=\\[)");
	}

	public static String getLastKey(String path) {
		return path.substring(path.lastIndexOf('.') + 1);
	}

	public static void mergeInto(CompoundTag target, CompoundTag additions, boolean replace) {
		if (additions == null) return;

		for (String key : additions.getKeys()) {
			if (!target.contains(key)) {
				//noinspection ConstantConditions
				target.put(key, additions.get(key).copy());
				continue;
			}

			Tag targetTag = target.get(key);
			Tag additionsTag = additions.get(key);
			if (isCompound(targetTag) && isCompound(additionsTag)) {
				mergeInto(asCompoundTag(targetTag), asCompoundTag(additionsTag), replace);
			} else if (isList(targetTag) && isList(additionsTag)) {
				int targetSize = asListTag(targetTag).size();
				AbstractListTag<Tag> listTag = asListTag(targetTag);
				for (Tag tag : asListTag(additionsTag)) {
					Tag copy = tag.copy();
					listTag.add(tag);
				}
			} else {
				if (replace)
					//noinspection ConstantConditions
					target.put(key, additionsTag.copy());
			}
		}
	}

	public static void mergeInto(CompoundTag target, CompoundTag additions, Collection<Pair<Pattern, MergeMode>> mergeModes, String basePath) {
		if (additions == null) return;

		if (!basePath.isEmpty()) basePath += '.';

		for (String key : additions.getKeys()) {
			String path = basePath + key;
			MergeMode mergeMode = MergeMode.MERGE;
			for (Pair<Pattern, MergeMode> entry : mergeModes) {
				if (entry.getFirst().matcher(path).matches()) {
					mergeMode = entry.getSecond();
					break;
				}
			}

			if (target.contains(key)) {
				if (mergeMode == MergeMode.UPDATE || mergeMode == MergeMode.OVERWRITE) {
					//noinspection ConstantConditions
					target.put(key, additions.get(key).copy());
				} else if (mergeMode == MergeMode.MERGE) {
					Tag targetTag = target.get(key);
					Tag additionsTag = additions.get(key);

					if (isCompound(targetTag) && isCompound(additionsTag)) {
						mergeInto(asCompoundTag(targetTag), asCompoundTag(additionsTag), mergeModes, path);
					} else if (isList(targetTag) && isList(additionsTag)) {
						mergeInto(asListTag(targetTag), asListTag(additionsTag), mergeModes, path);
					} else {
						//noinspection ConstantConditions
						target.put(key, additionsTag.copy());
					}
				}
			} else if (mergeMode != MergeMode.UPDATE) {
				//noinspection ConstantConditions
				target.put(key, additions.get(key).copy());
			}
		}
	}

	public static void mergeInto(AbstractListTag<Tag> target, AbstractListTag<Tag> additions, Collection<Pair<Pattern, MergeMode>> mergeModes, String basePath) {
		if (additions == null) return;

		int targetSize = target.size();

		for (int i = 0; i < additions.size(); i++) {
			String path = basePath + "[" + i + "]";

			MergeMode mergeMode = MergeMode.MERGE;
			for (Pair<Pattern, MergeMode> entry : mergeModes) {
				if (entry.getFirst().matcher(path).matches()) {
					mergeMode = entry.getSecond();
					break;
				}
			}

			if (mergeMode == MergeMode.OVERWRITE || (mergeMode == MergeMode.UPDATE && i < targetSize)) {
				target.set(i, additions.get(i).copy());
			} else if (mergeMode == MergeMode.MERGE) {
				Tag targetTag = target.get(i);
				Tag additionsTag = additions.get(i);

				if (isCompound(targetTag) && isCompound(additionsTag)) {
					mergeInto(asCompoundTag(targetTag), asCompoundTag(additionsTag), mergeModes, path);
				} else if (isList(targetTag) && isList(additionsTag)) {
					mergeInto(asListTag(targetTag), asListTag(additionsTag), mergeModes, path);
				} else {
					target.set(i, targetTag.copy());
				}
			} else if (mergeMode == MergeMode.APPEND) {
				try {
					target.add(additions.get(i));
				} catch (Exception e) {
					NbtCrafting.logError("Can't append tag " + additions.get(i).asString() + " to list: " + target.asString());
				}
			}
		}
	}

	public static Tag asTag(Object value) {
		if (value instanceof Tag) {
			return (Tag) value;
		} else if (value instanceof String) {
			return StringTag.of((String) value);
		} else if (value instanceof Float) {
			return FloatTag.of((Float) value);
		} else if (value instanceof Double) {
			return DoubleTag.of((Double) value);
		} else if (value instanceof Byte) {
			return ByteTag.of((Byte) value);
		} else if (value instanceof Character) {
			return StringTag.of(String.valueOf(value));
		} else if (value instanceof Short) {
			return ShortTag.of((Short) value);
		} else if (value instanceof Integer) {
			return IntTag.of((Integer) value);
		} else if (value instanceof Long) {
			return LongTag.of((Long) value);
		} else if (value instanceof Boolean) {
			return ByteTag.of((byte) ((Boolean) value ? 1 : 0));
		} else {
			return null;
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static Object toDollarValue(Tag value) {
		if (value instanceof StringTag) {
			return value.asString();
		} else if (value instanceof FloatTag) {
			return ((FloatTag) value).getFloat();
		} else if (value instanceof DoubleTag) {
			return ((DoubleTag) value).getDouble();
		} else if (value instanceof ByteTag) {
			return ((ByteTag) value).getByte();
		} else if (value instanceof ShortTag) {
			return ((ShortTag) value).getShort();
		} else if (value instanceof IntTag) {
			return ((IntTag) value).getInt();
		} else if (value instanceof LongTag) {
			return ((LongTag) value).getLong();
		} else if (value instanceof Tag) {
			return value;
		} else {
			return null;
		}
	}

	public static Tag asTag(JsonElement jsonElement) {
		return Dynamic.convert(BetterJsonOps.INSTANCE, NbtOps.INSTANCE, jsonElement);
	}

	public static JsonElement toJson(Tag tag) {
		return Dynamic.convert(NbtOps.INSTANCE, BetterJsonOps.INSTANCE, tag);
	}
}

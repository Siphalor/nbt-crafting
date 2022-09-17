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

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.nbt.NbtElement;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;

import de.siphalor.nbtcrafting3.NbtCrafting;
import de.siphalor.nbtcrafting3.dollar.DollarUtil;
import de.siphalor.nbtcrafting3.util.BetterJsonOps;

@SuppressWarnings("unused")
public class NbtUtil {
	public static final NbtCompound EMPTY_COMPOUND = new NbtCompound();

	public static NbtCompound getTagOrEmpty(ItemStack itemStack) {
		if (itemStack.hasNbt())
			return itemStack.getNbt();
		else
			return EMPTY_COMPOUND;
	}

	public static NbtCompound copyOrEmpty(NbtCompound compoundTag) {
		if (compoundTag == null)
			return new NbtCompound();
		else
			return compoundTag.copy();
	}

	public static boolean tagsMatch(NbtElement main, NbtElement reference) {
		// Empty reference string is treated as wildcard
		if (isString(reference) && reference.asString().equals(""))
			return true;
		if (isString(main) && isString(reference))
			return main.asString().equals(reference.asString());
		if (isNumeric(main)) {
			if (isNumeric(reference))
				return asNumberTag(main).doubleValue() == asNumberTag(reference).doubleValue();
			// The reference might be a numeric range
			if (isString(reference) && reference.asString().startsWith("$"))
				return NbtNumberRange.ofString(reference.asString().substring(1)).matches(asNumberTag(main).doubleValue());
			return false;
		}
		return false;
	}

	public static boolean compoundsOverlap(NbtCompound main, NbtCompound reference) {
		for (String key : main.getKeys()) {
			if (!reference.contains(key))
				continue;
			NbtElement mainTag = main.get(key);
			NbtElement refTag = reference.get(key);
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

	public static boolean listsOverlap(AbstractNbtList<NbtElement> main, AbstractNbtList<NbtElement> reference) {
		for (NbtElement mainTag : main) {
			for (NbtElement referenceTag : main) {
				if (isCompound(mainTag) && isCompound(referenceTag)) {
					if (compoundsOverlap(asNbtCompound(mainTag), asNbtCompound(referenceTag)))
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

	public static boolean isCompoundContained(NbtCompound inner, NbtCompound outer) {
		for (String key : inner.getKeys()) {
			NbtElement innerTag = inner.get(key);
			if (!outer.contains(key))
				return false;
			NbtElement outerTag = outer.get(key);
			if (isCompound(innerTag) && isCompound(outerTag)) {
				if (isCompoundContained(asNbtCompound(innerTag), asNbtCompound(outerTag)))
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

	public static boolean isListContained(AbstractNbtList<NbtElement> inner, AbstractNbtList<NbtElement> outer) {
		for (NbtElement innerTag : inner) {
			boolean success = false;
			for (NbtElement outerTag : outer) {
				if (isCompound(innerTag) && isCompound(outerTag) && isCompoundContained(asNbtCompound(innerTag), asNbtCompound(outerTag))) {
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

	public static boolean sameType(NbtElement tag1, NbtElement tag2) {
		return tag1.getType() == tag2.getType();
	}

	@Contract(value = "null -> false", pure = true)
	public static boolean isString(NbtElement tag) {
		return tag instanceof NbtString;
	}

	@Contract(value = "null -> false", pure = true)
	public static boolean isCompound(NbtElement tag) {
		return tag instanceof NbtCompound;
	}

	@Contract(value = "null -> false", pure = true)
	public static boolean isList(NbtElement tag) {
		return tag instanceof AbstractNbtList;
	}

	@Contract(value = "null -> false", pure = true)
	public static boolean isNumeric(NbtElement tag) {
		return tag instanceof AbstractNbtNumber;
	}

	public static String asString(NbtElement tag) {
		if (tag instanceof AbstractNbtNumber) {
			return ((AbstractNbtNumber) tag).numberValue().toString();
		} else if (tag instanceof NbtString) {
			return tag.asString();
		} else if (tag instanceof NbtList) {
			StringJoiner joiner = new StringJoiner(", ");
			for (NbtElement entry : ((NbtList) tag)) {
				String s = asString(entry);
				joiner.add(s);
			}
			return joiner.toString();
		} else {
			return tag.toString();
		}
	}

	public static NbtString asStringTag(NbtElement tag) {
		return (NbtString) tag;
	}

	public static NbtCompound asNbtCompound(NbtElement tag) {
		return (NbtCompound) tag;
	}

	public static AbstractNbtList<NbtElement> asListTag(NbtElement tag) {
		//noinspection unchecked
		return (AbstractNbtList<NbtElement>) tag;
	}

	public static AbstractNbtNumber asNumberTag(NbtElement tag) {
		return (AbstractNbtNumber) tag;
	}

	public static NbtElement getTag(NbtElement main, String path) {
		return getTag(main, splitPath(path));
	}

	public static NbtElement getTag(NbtElement main, String[] pathKeys) {
		NbtElement currentTag = main;
		for (String pathKey : pathKeys) {
			if ("".equals(pathKey))
				continue;
			if (currentTag == null)
				return null;
			if (pathKey.charAt(0) == '[') {
				int index = Integer.parseUnsignedInt(pathKey.substring(1, pathKey.length() - 2), 10);
				if (isList(currentTag)) {
					AbstractNbtList<NbtElement> list = asListTag(currentTag);
					if (index >= list.size())
						return null;
					else
						currentTag = list.get(index);
				} else {
					return null;
				}
			} else {
				if (isCompound(currentTag)) {
					NbtCompound compound = asNbtCompound(currentTag);
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

	public static NbtElement getTagOrCreate(NbtElement main, String path) throws NbtException {
		return getTagOrCreate(main, splitPath(path));
	}

	public static NbtElement getTagOrCreate(NbtElement main, String[] pathParts) throws NbtException {
		NbtElement currentTag = main;
		for (String pathPart : pathParts) {
			if ("".equals(pathPart))
				continue;
			if (pathPart.charAt(0) == '[') {
				if (!isList(currentTag)) {
					throw new NbtException(String.join(".", pathParts) + " doesn't match on " + main.asString());
				}
				AbstractNbtList<NbtElement> currentList = asListTag(currentTag);
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
				NbtCompound currentCompound = asNbtCompound(currentTag);
				if (!currentCompound.contains(pathPart)) {
					NbtCompound newCompound = new NbtCompound();
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

	public static void put(NbtElement main, String[] pathParts, NbtElement tag) throws NbtException {
		NbtElement parent = getTagOrCreate(main, ArrayUtils.subarray(pathParts, 0, pathParts.length - 1));

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
					asNbtCompound(parent).remove(key);
				} else {
					asNbtCompound(parent).put(key, tag);
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

	public static void mergeInto(NbtCompound target, NbtCompound additions, boolean replace) {
		if (additions == null) return;

		for (String key : additions.getKeys()) {
			if (!target.contains(key)) {
				//noinspection ConstantConditions
				target.put(key, additions.get(key).copy());
				continue;
			}

			NbtElement targetTag = target.get(key);
			NbtElement additionsTag = additions.get(key);
			if (isCompound(targetTag) && isCompound(additionsTag)) {
				mergeInto(asNbtCompound(targetTag), asNbtCompound(additionsTag), replace);
			} else if (isList(targetTag) && isList(additionsTag)) {
				int targetSize = asListTag(targetTag).size();
				AbstractNbtList<NbtElement> listTag = asListTag(targetTag);
				for (NbtElement tag : asListTag(additionsTag)) {
					NbtElement copy = tag.copy();
					listTag.add(tag);
				}
			} else {
				if (replace) {
					//noinspection ConstantConditions
					target.put(key, additionsTag.copy());
				}
			}
		}
	}


	public static void mergeInto(NbtCompound target, NbtCompound additions, MergeContext context, String basePath) {
		if (additions == null) return;

		if (!basePath.isEmpty()) basePath += '.';

		for (String key : additions.getKeys()) {
			String path = basePath + key;
			NbtElement targetTag = target.get(key);

			MergeBehavior mergeBehavior = context.getMergeMode(targetTag, path);

			NbtElement additionTag = additions.get(key);
			assert additionTag != null;
			NbtElement merged = mergeBehavior.merge(targetTag, additionTag, context, path);
			if (merged != targetTag) {
				target.put(key, merged);
			}
		}
	}

	public static void mergeInto(AbstractNbtList<NbtElement> target, AbstractNbtList<NbtElement> additions, MergeContext context, String basePath) {
		if (additions == null) return;

		int targetSize = target.size();
		int additionsSize = additions.size();

		for (int i = 0; i < additions.size() && i < targetSize; i++) { // for all elements that exist in both
			String path = basePath + "[" + i + "]";
			NbtElement targetTag = target.get(i);

			MergeBehavior mergeBehavior = context.getMergeMode(targetTag, path);

			if (mergeBehavior == MergeBehavior.APPEND) { // Legacy behavior
				try {
					target.add(additions.get(i).copy());
				} catch (Exception e) {
					NbtCrafting.logError("Can't append tag " + additions.get(i).asString() + " to list: " + target.asString());
				}
				continue;
			}

			NbtElement additionTag = additions.get(i);
			assert additionTag != null;
			NbtElement merged = mergeBehavior.merge(targetTag, additionTag, context, path);
			if (merged != targetTag) {
				target.set(i, merged);
			}
		}

		for (int i = targetSize; i < additionsSize; i++) { // for any additional elements
			String path = basePath + "[" + i + "]";
			MergeBehavior mergeBehavior = context.getMergeMode(null, path);
			NbtElement tag = mergeBehavior.merge(null, additions.get(i), context, path);
			if (tag != null) {
				target.add(tag);
			}
		}
	}

	public static NbtElement asTag(Object value) {
		if (value instanceof NbtElement) {
			return (NbtElement) value;
		} else if (value instanceof String) {
			return NbtString.of((String) value);
		} else if (value instanceof Float) {
			return NbtFloat.of((Float) value);
		} else if (value instanceof Double) {
			return NbtDouble.of((Double) value);
		} else if (value instanceof Byte) {
			return NbtByte.of((Byte) value);
		} else if (value instanceof Character) {
			return NbtString.of(String.valueOf(value));
		} else if (value instanceof Short) {
			return NbtShort.of((Short) value);
		} else if (value instanceof Integer) {
			return NbtInt.of((Integer) value);
		} else if (value instanceof Long) {
			return NbtLong.of((Long) value);
		} else if (value instanceof Boolean) {
			return NbtByte.of((byte) ((Boolean) value ? 1 : 0));
		} else if (value instanceof List) {
			NbtList listTag = new NbtList();
			for (Object element : (List<?>) value) {
				listTag.add(asTag(element));
			}
			return listTag;
		} else if (value instanceof Map) {
			NbtCompound compoundTag = new NbtCompound();
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
				compoundTag.put(DollarUtil.asString(entry.getKey()), asTag(entry.getValue()));
			}
			return compoundTag;
		} else if (value instanceof ItemStack) {
			return getTagOrEmpty(((ItemStack) value));
		} else {
			return null;
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static Object toDollarValue(NbtElement value) {
		if (value instanceof NbtString) {
			return value.asString();
		} else if (value instanceof NbtFloat) {
			return ((NbtFloat) value).floatValue();
		} else if (value instanceof NbtDouble) {
			return ((NbtDouble) value).doubleValue();
		} else if (value instanceof NbtByte) {
			return ((NbtByte) value).byteValue();
		} else if (value instanceof NbtShort) {
			return ((NbtShort) value).shortValue();
		} else if (value instanceof NbtInt) {
			return ((NbtInt) value).intValue();
		} else if (value instanceof NbtLong) {
			return ((NbtLong) value).longValue();
		} else if (value instanceof NbtElement) {
			return value;
		} else {
			return null;
		}
	}

	public static NbtElement asTag(JsonElement jsonElement) {
		return Dynamic.convert(BetterJsonOps.INSTANCE, NbtOps.INSTANCE, jsonElement);
	}

	public static JsonElement toJson(NbtElement tag) {
		return Dynamic.convert(NbtOps.INSTANCE, BetterJsonOps.INSTANCE, tag);
	}
}

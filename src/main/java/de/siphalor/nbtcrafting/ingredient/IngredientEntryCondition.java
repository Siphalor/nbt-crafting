package de.siphalor.nbtcrafting.ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ComputationException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import de.siphalor.nbtcrafting.util.NbtNumberRange;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.JsonHelper;

public class IngredientEntryCondition {
	
	private Map<String, Object> requiredElements;
	private Map<String, Object> deniedElements;
	
	public IngredientEntryCondition() {
		requiredElements = null;
		deniedElements = null;
	}
	
	public boolean matches(ItemStack stack) {
		if(!stack.hasTag()) {
			return requiredElements == null || requiredElements.isEmpty();
		}
		CompoundTag tag = stack.getTag();
		if(requiredElements != null)
			return compoundMatches(tag, "", requiredElements);
		return true;
	}
	
	private boolean compoundMatches(CompoundTag tag, String path, Map<String, Object> reference) {
		return compoundMatches(tag, path, reference, 0);
	}
	
	private boolean compoundMatches(CompoundTag tag, String path, Map<String, Object> reference, Integer successfulRequired) {
		Set<String> keys = tag.getKeys();
		for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			String keyPath = path + "." + key;
			if(tag.getType(key) != 10) {
				if(!reference.containsKey(keyPath))
					return false;
				if(!matches(tag.getTag(keyPath), reference.get(keyPath)))
					return false;
				else {
					successfulRequired++;
				}
			} else {
				compoundMatches(tag.getCompound(keyPath), keyPath, reference, successfulRequired);
			}
		}
		return successfulRequired >= reference.size();
	}
	
	private boolean matches(Tag tag, Object reference) {
		if(!typesMatch(tag.getType(), reference)) return false;
		if(reference instanceof String) {
			return tag.asString() == (String) reference;
		}
		if(reference instanceof Double) {
			return ((AbstractNumberTag) tag).getDouble() == (Double) reference;
		}
		if(reference instanceof NbtNumberRange) {
			return ((NbtNumberRange) reference).matches(((AbstractNumberTag) tag).getDouble());
		}
		if(reference instanceof ArrayList<?>) {
			@SuppressWarnings("unchecked")
			ArrayList<Map<String, Object>> listReference = (ArrayList<Map<String,Object>>) reference;
			if(tag.getType() == 9) {
				ListTag list = (ListTag) tag;
				//TODO: change that ( V )
				if(list.getType() != 10)
					return false;
				if(list.size() < listReference.size())
					return false;
				ArrayList<Integer> success = new ArrayList<Integer>();
				for(int i = 0; i < list.size(); i++) {
					for(int j = 0; j < listReference.size(); j++) {
						if(success.contains(j)) continue;
						if(compoundMatches((CompoundTag) list.get(i), "", listReference.get(j), 0)) {
							success.add(j);
							if(success.size() >= listReference.size())
								return true;
						}
					}
				}
			}
			return false;
		}
		return true;
	}
	
	private boolean typesMatch(byte tagType, Object reference) {
		switch (tagType) {
		case 6:
			return reference instanceof Double;
		case 8:
			return reference instanceof String;
		case 9:
			return reference instanceof ArrayList<?>;
		case 10:
			return true;
		default:
			return false;
		}
	}
	
	public void addToJson(JsonObject json) {
		
	}
	
	public CompoundTag getPreviewTag() {
		CompoundTag tag = new CompoundTag();
		return tag;
	}
	
	public static IngredientEntryCondition fromJson(JsonObject json) {
		IngredientEntryCondition condition = new IngredientEntryCondition();
		
		if(json.has("require")) {
			readElementsFromJson(condition.requiredElements, "", json.get("require"));
		}
		return condition;
	}
	
	protected static void readElementsFromJson(Map<String, Object> elements, String path, JsonElement json) {
		if(!json.isJsonArray() && !json.isJsonObject())
			throw new JsonSyntaxException(path + " must either be an array or an object!");
		if(json.isJsonArray()) {
			ArrayList<Map<String, Object>> array = new ArrayList<Map<String,Object>>();
			JsonArray jsonArray = json.getAsJsonArray();
			for(int i = 0; i < jsonArray.size(); i++) {
				array.add(new HashMap<String, Object>());
				readElementsFromJson(array.get(i), "", jsonArray.get(i));
			}
			elements.put(path, array);
		} else if(json.isJsonObject()) {
			JsonObject object = json.getAsJsonObject();
			for (Iterator<Entry<String, JsonElement>> iterator = object.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, JsonElement> entry = iterator.next();
				JsonElement jsonElement = entry.getValue();
				String pathTo = path + "." + entry.getKey();
				if(jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
					readElementsFromJson(elements, pathTo, jsonElement);
					continue;
				}
				if(JsonHelper.isNumber(jsonElement)) {
					elements.put(pathTo, jsonElement.getAsNumber());
					continue;
				}
				if(JsonHelper.isString(jsonElement)) {
					String string = jsonElement.getAsString();
					if(string.charAt(0) == '$') {
						elements.put(pathTo, NbtNumberRange.ofString(string.substring(1)));
						continue;
					}
					elements.put(pathTo, string);
					continue;
				}
				if(jsonElement.isJsonNull()) {
					elements.put(pathTo, null);
					continue;
				}
				throw new JsonSyntaxException(pathTo + " must either be an array, an object, a number, a string or null!");
			}
		}
	}
	
}

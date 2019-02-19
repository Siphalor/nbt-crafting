package de.siphalor.nbtcrafting.ingredient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import de.siphalor.nbtcrafting.util.NbtHelper;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public class IngredientEntryCondition {
	
	private CompoundTag requiredElements;
	private CompoundTag deniedElements;
	
	public IngredientEntryCondition() {
		requiredElements = new CompoundTag();
		deniedElements = new CompoundTag();
	}
	
	public IngredientEntryCondition(CompoundTag requiredElements, CompoundTag deniedElements) {
		this.requiredElements = requiredElements;
		this.deniedElements = deniedElements;
	}
	
	public boolean matches(ItemStack stack) {
		if(!stack.hasTag()) {
			return requiredElements.isEmpty();
		}
		CompoundTag tag = stack.getTag();
		//noinspection ConstantConditions
		if(NbtHelper.compoundsOverlap(tag, deniedElements))
			return false;
		if(!NbtHelper.isCompoundContained(requiredElements, tag))
			return false;
		return true;
	}
	
	public void addToJson(JsonObject json) {
		if(requiredElements.getSize() > 0)
			json.add("require", Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, requiredElements));
		if(deniedElements.getSize() > 0)
			json.add("deny", Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, deniedElements));
	}
	
	public CompoundTag getPreviewTag() {
		return requiredElements;
	}
	
	public static IngredientEntryCondition fromJson(JsonObject json) {
		IngredientEntryCondition condition = new IngredientEntryCondition();
		
		if(json.has("require")) {
			if(!json.get("require").isJsonObject())
				throw new JsonParseException("data.require must be an object");
			condition.requiredElements = (CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, json.getAsJsonObject("require"));
		}
		if(json.has("deny")) {
			if(!json.get("deny").isJsonObject())
				throw new JsonParseException("data.deny must be an object");
			condition.deniedElements = (CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, json.getAsJsonObject("deny"));
		}
		return condition;
	}
	
	public void write(PacketByteBuf buf) {
		buf.writeCompoundTag(requiredElements);
		buf.writeCompoundTag(deniedElements);
	}
	
	public static IngredientEntryCondition read(PacketByteBuf buf) {
		return new IngredientEntryCondition(buf.readCompoundTag(), buf.readCompoundTag());
	}
	
}

package de.siphalor.nbtcrafting.ingredient;

import java.util.Collection;
import java.util.Collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public class IngredientStackEntry extends IngredientEntry {
	
	private IngredientEntryCondition condition;
	private int id;
	
	public IngredientStackEntry(int id, IngredientEntryCondition condition) {
		this.id = id;
		this.condition = condition;
	}
	
	public IngredientStackEntry(ItemStack stack) {
		this.id = Registry.ITEM.getRawId(stack.getItem());
	}

	@Override
	public boolean matches(ItemStack stack) {
		return Registry.ITEM.getRawId(stack.getItem()) == this.id && condition.matches(stack);
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("item", Registry.ITEM.getId(Registry.ITEM.getInt(id)).toString());
		condition.addToJson(json);
		return json;
	}

	@Override
	public Collection<ItemStack> getPreviewStacks() {
		ItemStack stack = new ItemStack(Registry.ITEM.getInt(id));
		stack.setTag(condition.getPreviewTag());
		return Collections.singleton(stack);
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeVarInt(id);
	}
	
	public static IngredientStackEntry read(PacketByteBuf buf) {
		return new IngredientStackEntry(buf.readVarInt(), null);
	}

}

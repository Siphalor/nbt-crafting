package de.siphalor.nbtcrafting.ingredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public class IngredientMultiStackEntry extends IngredientEntry {
	
	private IngredientEntryCondition condition;
	private ArrayList<Integer> itemIds;
	private String tag;

	public IngredientMultiStackEntry(Collection<Integer> items, IngredientEntryCondition condition) {
		this.condition = condition;
		this.itemIds = new ArrayList<Integer>(items);
		this.tag = "";
	}
	
	@Override
	public boolean matches(ItemStack stack) {
		return itemIds.contains(Registry.ITEM.getRawId(stack.getItem()));
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("tag", tag);
		return json;
	}

	@Override
	public Collection<ItemStack> getPreviewStacks() {
		return itemIds.stream().map(id -> new ItemStack(Registry.ITEM.getInt(id))).collect(Collectors.toList());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeVarInt(itemIds.size());
		for(int i = 0; i < itemIds.size(); i++) {
			buf.writeVarInt(itemIds.get(i));
		}
	}
	
	public static IngredientMultiStackEntry read(PacketByteBuf buf) {
		int length = buf.readVarInt();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(int i = 0; i < length; i++) {
			ids.add(buf.readVarInt());
		}
		return new IngredientMultiStackEntry(ids, null);
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

}

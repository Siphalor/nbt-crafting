package de.siphalor.nbtcrafting.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.Collections;

public class IngredientStackEntry extends IngredientEntry {
	
	private IngredientEntryCondition condition;
	private int id;
	
	public IngredientStackEntry(int id, IngredientEntryCondition condition) {
		this.id = id;
		this.condition = condition;
	}
	
	public IngredientStackEntry(ItemStack stack) {
		this.id = Registry.ITEM.getRawId(stack.getItem());
		if(stack.hasTag())
			this.condition = new IngredientEntryCondition(stack.getTag(), new CompoundTag());
		else
			this.condition = new IngredientEntryCondition();
	}

	@Override
	public boolean matches(ItemStack stack) {
		return Registry.ITEM.getRawId(stack.getItem()) == this.id && condition.matches(stack);
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("item", Registry.ITEM.getId(Registry.ITEM.get(id)).toString());
		condition.addToJson(json);
		return json;
	}

	@Override
	public Collection<ItemStack> getPreviewStacks() {
		ItemStack stack = new ItemStack(Registry.ITEM.get(id));
		if(condition == null)
			System.out.println("abc");
		stack.setTag(condition.getPreviewTag());
		return Collections.singleton(stack);
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeVarInt(id);
		this.condition.write(buf);
	}
	
	public static IngredientStackEntry read(PacketByteBuf buf) {
		return new IngredientStackEntry(buf.readVarInt(), IngredientEntryCondition.read(buf));
	}

}

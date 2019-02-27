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
	private ItemStack remainder;
	
	public IngredientStackEntry(int id, IngredientEntryCondition condition) {
		this.id = id;
		this.condition = condition;
		this.remainder = null;
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
		stack.setTag(condition.getPreviewTag());
		return Collections.singleton(stack);
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeVarInt(id);
		this.condition.write(buf);
		buf.writeBoolean(remainder != null);
		if(remainder != null)
			buf.writeItemStack(remainder);
	}

	public void setRecipeRemainder(ItemStack stack) {
		remainder = stack;
	}

	@Override
	public ItemStack getRecipeRemainder(ItemStack stack) {
		return remainder;
	}

	public static IngredientStackEntry read(PacketByteBuf buf) {
		IngredientStackEntry entry = new IngredientStackEntry(buf.readVarInt(), IngredientEntryCondition.read(buf));
		if(buf.readBoolean())
            entry.setRecipeRemainder(buf.readItemStack());
		return entry;
	}

}

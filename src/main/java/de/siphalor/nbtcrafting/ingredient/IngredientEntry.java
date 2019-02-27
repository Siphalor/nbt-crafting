package de.siphalor.nbtcrafting.ingredient;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;

import java.util.Collection;

public abstract class IngredientEntry {
	
	public abstract boolean matches(ItemStack stack);

	public abstract JsonElement toJson();

	public abstract Collection<ItemStack> getPreviewStacks();
	
	public abstract void write(PacketByteBuf buf);

	public abstract ItemStack getRecipeRemainder();
}

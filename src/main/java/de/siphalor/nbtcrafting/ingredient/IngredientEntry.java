package de.siphalor.nbtcrafting.ingredient;

import java.util.Collection;

import com.google.gson.JsonElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;

public abstract class IngredientEntry {
	
	public abstract boolean matches(ItemStack stack);

	public abstract JsonElement toJson();

	public abstract Collection<ItemStack> getPreviewStacks();
	
	public abstract void write(PacketByteBuf buf);
}

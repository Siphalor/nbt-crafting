package de.siphalor.nbtcrafting.ingredient;

import com.google.gson.JsonElement;
import de.siphalor.nbtcrafting.dollars.Dollar;
import de.siphalor.nbtcrafting.dollars.DollarException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

import java.util.Collection;
import java.util.HashMap;

public abstract class IngredientEntry {
	protected ItemStack remainder;
	protected Dollar[] dollars;

	public IngredientEntry() {
		this.remainder = null;
		this.dollars = new Dollar[0];
	}
	
	public abstract boolean matches(ItemStack stack);

	public abstract JsonElement toJson();

	public abstract Collection<ItemStack> getPreviewStacks();

	public abstract void write(PacketByteBuf buf);

	public ItemStack getRecipeRemainder(ItemStack stack, HashMap<String, CompoundTag> reference) {
		if(remainder == null)
			return null;
        ItemStack result = remainder.copy();
        for(Dollar dollar : dollars) {
	        try {
		        dollar.apply(result, reference);
	        } catch (DollarException e) {
		        e.printStackTrace();
	        }
        }
        return result;
	}

	public void setRecipeRemainder(ItemStack stack) {
		this.remainder = stack;
		if(stack.hasTag())
			this.dollars = Dollar.extractDollars(stack.getTag());
	}
}

package de.siphalor.nbtcrafting.ingredient;

import com.google.gson.JsonElement;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import de.siphalor.nbtcrafting.util.RecipeUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

import java.util.Collection;
import java.util.Map;

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

	public ItemStack getRecipeRemainder(ItemStack stack, Map<String, CompoundTag> reference) {
		if(remainder == null)
			return null;
        return RecipeUtil.applyDollars(remainder.copy(), dollars, reference);
	}

	public void setRecipeRemainder(ItemStack stack) {
		this.remainder = stack;
		if(stack.hasTag())
			this.dollars = DollarParser.extractDollars(stack.getTag());
	}
}

package de.siphalor.nbtcrafting.ingredient;

import com.google.gson.JsonElement;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;
import net.minecraft.item.ItemStack;
import net.minecraft.util.PacketByteBuf;

import java.util.Collection;
import java.util.Map;

public abstract class IngredientEntry {
	protected ItemStack remainder;
	protected Dollar[] remainderDollars;

	public IngredientEntry() {
		this.remainder = null;
		this.remainderDollars = new Dollar[0];
	}
	
	public abstract boolean matches(ItemStack stack);

	public abstract JsonElement toJson();

	public Collection<ItemStack> getPreviewStacks() {
		return getPreviewStacks(true);
	}

	public abstract Collection<ItemStack> getPreviewStacks(boolean nbt);

	public abstract void write(PacketByteBuf buf);

	public ItemStack getRecipeRemainder(ItemStack stack, Map<String, Object> reference) {
		if(remainder == null)
			return ItemStack.EMPTY;
        return RecipeUtil.applyDollars(remainder.copy(), remainderDollars, reference);
	}

	public void setRecipeRemainder(ItemStack stack) {
		this.remainder = stack;
		if(stack.hasTag())
			this.remainderDollars = DollarParser.extractDollars(stack.getTag(), true);
	}
}

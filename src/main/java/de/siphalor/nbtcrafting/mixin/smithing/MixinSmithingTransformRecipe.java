package de.siphalor.nbtcrafting.mixin.smithing;

import com.google.common.collect.ImmutableMap;

import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.api.nbt.NbtUtil;
import de.siphalor.nbtcrafting.dollar.Dollar;
import de.siphalor.nbtcrafting.dollar.DollarParser;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SmithingTransformRecipe.class)
public class MixinSmithingTransformRecipe {
	@Shadow
	@Final
	ItemStack result;

	private Dollar[] outputDollars;

	private Map<String, Object> buildDollarReference(Inventory inv) {
		return ImmutableMap.of(
				"template", NbtUtil.getTagOrEmpty(inv.getStack(0)),
				"base", NbtUtil.getTagOrEmpty(inv.getStack(1)),
				"addition", NbtUtil.getTagOrEmpty(inv.getStack(2))
		);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(Identifier id, Ingredient template, Ingredient base, Ingredient addition, ItemStack result, CallbackInfo ci){
		outputDollars = DollarParser.extractDollars(result.getNbt(), false);
	}


	@Inject(method = "craft", at = @At("HEAD"), cancellable = true)
	public void craft(Inventory inventory, DynamicRegistryManager registryManager, CallbackInfoReturnable<ItemStack> cir){
		ItemStack output = RecipeUtil.applyDollars(result.copy(), outputDollars, buildDollarReference(inventory));
		if (output != null) cir.setReturnValue(output);
	}
}

package de.siphalor.nbtcrafting.mixin.cutting;

import de.siphalor.nbtcrafting.api.nbt.NbtHelper;
import net.minecraft.container.StonecutterContainer;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.StonecuttingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(StonecutterContainer.class)
public class MixinStonecutterContainer {
	@Shadow private List<StonecuttingRecipe> availableRecipes;

	@Inject(method = "updateInput", at = @At("TAIL"))
	private void onInputUpdated(Inventory inventory, ItemStack input, CallbackInfo callbackInfo) {
		availableRecipes.sort((a, b) -> {
			ItemStack s1 = a.getOutput();
			ItemStack s2 = b.getOutput();
			int comp = s1.getTranslationKey().compareTo(s2.getTranslationKey());
			if (comp != 0)
				return comp;
			return NbtHelper.getTagOrEmpty(s1).toString().compareTo(NbtHelper.getTagOrEmpty(s2).toString());
		});
	}
}

package de.siphalor.nbtcrafting.mixin.brewing;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/screen/BrewingStandScreenHandler$PotionSlot")
public class MixinBrewingSlotPotion {
	@Inject(method = "matches(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	private static void matches(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		/*RecipeManager recipeManager;
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			recipeManager = ClientCore.getRecipeManager();
		} else {
			recipeManager = ((MinecraftServer) FabricLoader.getInstance().getGameInstance()).getRecipeManager();
		}
        if(BrewingRecipe.existsMatchingBase(stack, recipeManager))
        	callbackInfoReturnable.setReturnValue(true);*/
		callbackInfoReturnable.setReturnValue(true);
	}
}

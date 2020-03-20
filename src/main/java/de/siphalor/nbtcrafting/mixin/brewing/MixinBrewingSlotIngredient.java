package de.siphalor.nbtcrafting.mixin.brewing;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/screen/BrewingStandScreenHandler$IngredientSlot")
public abstract class MixinBrewingSlotIngredient extends Slot {
	public MixinBrewingSlotIngredient(Inventory inventory_1, int int_1, int int_2, int int_3) {
		super(inventory_1, int_1, int_2, int_3);
	}

	@Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
	public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		/*RecipeManager recipeManager;
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			recipeManager = ClientCore.getRecipeManager();
		} else {
			recipeManager = ((MinecraftServer) FabricLoader.getInstance().getGameInstance()).getRecipeManager();
		}
        if(BrewingRecipe.existsMatchingIngredient(stack, recipeManager))
        	callbackInfoReturnable.setReturnValue(true);*/
		callbackInfoReturnable.setReturnValue(true);
	}
}

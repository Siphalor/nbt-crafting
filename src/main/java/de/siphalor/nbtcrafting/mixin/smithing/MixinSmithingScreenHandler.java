package de.siphalor.nbtcrafting.mixin.smithing;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;
import de.siphalor.nbtcrafting.recipe.IngredientRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SmithingScreenHandler.class)
public abstract class MixinSmithingScreenHandler extends ForgingScreenHandler {
	@Unique
	private static DefaultedList<ItemStack> remainders = null;

	public MixinSmithingScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(type, syncId, playerInventory, context);
	}

	@Inject(
			method = "updateResult",
			at = @At("HEAD"),
			cancellable = true
	)
	public void onUpdateResult(CallbackInfo callbackInfo) {
		Optional<IngredientRecipe<Inventory>> match = player.world.getRecipeManager().getFirstMatch(NbtCrafting.SMITHING_RECIPE_TYPE, input, player.world);

		if (match.isPresent()) {
			output.setStack(0, match.get().craft(input));
			callbackInfo.cancel();
		}
	}

	@Inject(
			method = "canTakeOutput",
			at = @At("HEAD"),
			cancellable = true
	)
	protected void canTakeOutput(PlayerEntity playerEntity, boolean stackPresent, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
		if (stackPresent) {
			callbackInfoReturnable.setReturnValue(true);
		}
	}

	@Inject(
			method = "onTakeOutput",
			at = @At("HEAD")
	)
	protected void onTakeOutput(PlayerEntity playerEntity, ItemStack output, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
		Optional<IngredientRecipe<Inventory>> match = player.world.getRecipeManager().getFirstMatch(NbtCrafting.SMITHING_RECIPE_TYPE, input, player.world);
		remainders = match.map(inventoryIngredientRecipe -> inventoryIngredientRecipe.getRemainingStacks(input)).orElse(null);
	}

	@Inject(
			method = "onTakeOutput",
			at = @At("TAIL")
	)
	protected void onOutputTaken(PlayerEntity playerEntity, ItemStack output, CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
		if (remainders != null) {
			context.run((world, blockPos) -> {
				RecipeUtil.putRemainders(remainders, input, world, blockPos);
			});
		}
	}
}

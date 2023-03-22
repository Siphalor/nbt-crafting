package de.siphalor.nbtcrafting.mixin.smithing;


import java.util.List;
import java.util.Optional;

import de.siphalor.nbtcrafting.recipe.Smithing.SmithingRecipe;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.ForgingSlotsManager;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.api.RecipeUtil;

@Mixin(SmithingScreenHandler.class)
public abstract class MixinSmithingScreenHandler extends ForgingScreenHandler {

	@Shadow
	@Final
	private World world;

	@Shadow
	@Final
	private List<net.minecraft.recipe.SmithingRecipe> recipes;

	private static Optional<Integer> getQuickMoveSlot(SmithingRecipe SmithingRecipe, ItemStack itemStack) {
		if (SmithingRecipe.template.test(itemStack)) {
			return Optional.of(0);
		}
		if (SmithingRecipe.base.test(itemStack)) {
			return Optional.of(1);
		}
		if (SmithingRecipe.addition.test(itemStack)) {
			return Optional.of(2);
		}
		return Optional.empty();
	}

	@Unique
	private static DefaultedList<ItemStack> remainders = null;

	public MixinSmithingScreenHandler(@Nullable ScreenHandlerType<?> screenHandlerType, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(screenHandlerType, syncId, playerInventory, context);
	}

	@Inject(method = "getForgingSlotsManager", at = @At("HEAD"), cancellable = true)
	private void getForgingSlotsManager(CallbackInfoReturnable<ForgingSlotsManager> cir) {
		cir.setReturnValue(ForgingSlotsManager.create()
				.input(0, 8, 48, itemStack -> this.world.getRecipeManager().listAllOfType(NbtCrafting.SMITHING_TRANSFORM_RECIPE_TYPE).stream().anyMatch(smithingRecipe -> smithingRecipe.template.test(itemStack)) || this.recipes.stream().anyMatch(smithingRecipe -> smithingRecipe.testTemplate(itemStack)))
				.input(1, 26, 48, itemStack -> this.world.getRecipeManager().listAllOfType(NbtCrafting.SMITHING_TRANSFORM_RECIPE_TYPE).stream().anyMatch(smithingRecipe -> smithingRecipe.base.test(itemStack) && smithingRecipe.template.test(this.slots.get(0).getStack())) || this.recipes.stream().anyMatch(smithingRecipe -> smithingRecipe.testBase(itemStack) && smithingRecipe.testTemplate(this.slots.get(0).getStack())))
				.input(2, 44, 48, itemStack -> this.world.getRecipeManager().listAllOfType(NbtCrafting.SMITHING_TRANSFORM_RECIPE_TYPE).stream().anyMatch(smithingRecipe -> smithingRecipe.addition.test(itemStack) && smithingRecipe.template.test(this.slots.get(0).getStack())) || this.recipes.stream().anyMatch(smithingRecipe -> smithingRecipe.testAddition(itemStack) && smithingRecipe.testTemplate(this.slots.get(0).getStack())))
				.output(3, 98, 48).build());
	}

	@Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
	protected void canTakeOutput(PlayerEntity playerEntity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
		if (bl) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getSlotFor", at = @At("HEAD"), cancellable = true)
	private void getSlotFor(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
		int x = this.world.getRecipeManager().listAllOfType(NbtCrafting.SMITHING_TRANSFORM_RECIPE_TYPE).stream().map(SmithingRecipe -> getQuickMoveSlot(SmithingRecipe, itemStack)).filter(Optional::isPresent).findFirst().orElse(Optional.of(0)).get();
		if (x != 0) {
			cir.setReturnValue(x);
		}
	}

	@Inject(method = "isValidIngredient", at = @At("HEAD"), cancellable = true)
	protected void isValidIngredient(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
		if (this.world.getRecipeManager().listAllOfType(NbtCrafting.SMITHING_TRANSFORM_RECIPE_TYPE).stream().map(SmithingRecipe -> getQuickMoveSlot(SmithingRecipe, itemStack)).anyMatch(Optional::isPresent))
			cir.setReturnValue(true);
	}


	@Inject(method = "onTakeOutput", at = @At("HEAD"))
	protected void onTakeOutput(PlayerEntity playerEntity, ItemStack itemStack, CallbackInfo ci) {
		Optional<SmithingRecipe> match = player.world.getRecipeManager().getFirstMatch(NbtCrafting.SMITHING_TRANSFORM_RECIPE_TYPE, input, player.world);
		remainders = match.map(inventoryIngredientRecipe -> inventoryIngredientRecipe.getRemainder(input)).orElse(null);
	}

	@Inject(method = "onTakeOutput", at = @At("TAIL"))
	protected void onOutputTaken(PlayerEntity playerEntity, ItemStack itemStack, CallbackInfo ci) {
		if (!remainders.isEmpty()) {
			context.run((world, blockPos) -> {
				RecipeUtil.putRemainders(remainders, input, world, blockPos);
			});
		}
	}

	@Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
	public void updateResult(CallbackInfo ci) {
		Optional<SmithingRecipe> match = player.world.getRecipeManager().getFirstMatch(NbtCrafting.SMITHING_TRANSFORM_RECIPE_TYPE, input, player.world);

		if (match.isPresent()) {
			output.setStack(0, match.get().craft(input, player.world.getRegistryManager()));
			ci.cancel();
		}
	}

}

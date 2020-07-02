package de.siphalor.nbtcrafting.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(RecipeBookGhostSlots.class)
public abstract class MixinRecipeBookGhostSlots {

	@Shadow
	@Final
	private List<RecipeBookGhostSlots.GhostInputSlot> slots;

	@Inject(
			method = "draw",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;depthFunc(I)V", remap = false, shift = Shift.BEFORE),
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	public void draw(MatrixStack matrices, MinecraftClient minecraftClient, int xOffset, int yOffset, boolean bool, float float_1, CallbackInfo ci, int i) {
		if (i != 0) {
			RecipeBookGhostSlots.GhostInputSlot slot = slots.get(i);
			minecraftClient.getItemRenderer().renderGuiItemOverlay(minecraftClient.textRenderer, slot.getCurrentItemStack(), slot.getX() + xOffset, slot.getY() + yOffset);
		}
	}
}

package de.siphalor.nbtcrafting.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.RecipeBookGhostSlots;
import net.minecraft.client.gui.widget.RecipeBookGhostSlots.GhostInputSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(RecipeBookGhostSlots.class)
public abstract class RecipeBookGhostSlotsMixin {
	
	@Shadow
	@Final
	private List<RecipeBookGhostSlots.GhostInputSlot> slots;

	@Inject(
		method = "draw",
		at = @At(value = "INVOKE", target = "com/mojang/blaze3d/platform/GlStateManager.enableLighting()V", remap = false, shift = Shift.BEFORE),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	public void draw(MinecraftClient minecraftClient, int xOffset, int yOffset, boolean bool, float float_1, CallbackInfo ci, int i) {
		if(i != 0) {
			GhostInputSlot slot = slots.get(i);
			minecraftClient.getItemRenderer().renderGuiItemOverlay(minecraftClient.textRenderer, slot.getCurrentItemStack(), slot.getX() + xOffset, slot.getY() + yOffset);
		}
	}
}

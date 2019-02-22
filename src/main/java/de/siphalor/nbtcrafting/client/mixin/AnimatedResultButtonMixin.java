package de.siphalor.nbtcrafting.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(AnimatedResultButton.class)
public abstract class AnimatedResultButtonMixin extends ButtonWidget {

	public AnimatedResultButtonMixin(int int_1, int int_2, String string_1) {
		super(int_1, int_2, string_1);
	}

	@Inject(
		method = "drawButton",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItem(Lnet/minecraft/item/ItemStack;II)V", shift = Shift.AFTER),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void drawButton(int mouseX, int mouseY, float delta, CallbackInfo ci, MinecraftClient minecraftClient, int int_3, int int_4, boolean boolean_1, @SuppressWarnings("rawtypes") List list_1, ItemStack stack, int int_5) {
		minecraftClient.getItemRenderer().renderGuiItemOverlay(minecraftClient.textRenderer, stack, this.x + int_5, this.y + int_5);
	}
}

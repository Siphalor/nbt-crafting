package de.siphalor.nbtcrafting.client.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;

@Mixin(AnimatedResultButton.class)
public abstract class AnimatedResultButtonMixin extends ButtonWidget {

	public AnimatedResultButtonMixin(int int_1, int int_2, int int_3, int int_4, int int_5, String string_1) {
		super(int_1, int_2, int_3, int_4, int_5, string_1);
	}

	@Inject(
		method = "draw",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItem(Lnet/minecraft/item/ItemStack;II)V", shift = Shift.AFTER),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void draw(int mouseX, int mouseY, float delta, CallbackInfo ci, MinecraftClient minecraftClient, int int_3, int int_4, boolean boolean_1, @SuppressWarnings("rawtypes") List list_1, ItemStack stack, int int_5) {
		minecraftClient.getItemRenderer().renderGuiItemOverlay(minecraftClient.fontRenderer, stack, this.x + int_5, this.y + int_5);
	}
}

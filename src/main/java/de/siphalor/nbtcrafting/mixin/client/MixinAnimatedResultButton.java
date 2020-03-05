package de.siphalor.nbtcrafting.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(AnimatedResultButton.class)
public abstract class MixinAnimatedResultButton extends AbstractButtonWidget {

	public MixinAnimatedResultButton(int x, int y, int width, int height, String message) {
		super(x, y, width, height, message);
	}

	@Inject(
		method = "renderButton",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderGuiItem(Lnet/minecraft/item/ItemStack;II)V", shift = Shift.AFTER),
		locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void drawButton(int mouseX, int mouseY, float delta, CallbackInfo ci, MinecraftClient minecraftClient, int int_3, int int_4, boolean boolean_1, @SuppressWarnings("rawtypes") List list_1, ItemStack stack, int int_5) {
		minecraftClient.getItemRenderer().renderGuiItemOverlay(minecraftClient.textRenderer, stack, this.x + int_5, this.y + int_5);
	}
}

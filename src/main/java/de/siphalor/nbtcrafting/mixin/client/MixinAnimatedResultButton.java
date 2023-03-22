/*
 * Copyright 2020-2022 Siphalor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package de.siphalor.nbtcrafting.mixin.client;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(AnimatedResultButton.class)
public abstract class MixinAnimatedResultButton extends ClickableWidget {
	@Unique
	private static int itemDrawOffset;

	public MixinAnimatedResultButton(int x, int y, int width, int height, Text message) {
		super(x, y, width, height, message);
	}

	@Inject(
			method = "renderButton",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderInGui(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V"),
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void beforeItemDrawn(MatrixStack matrixStack, int i, int j, float f, CallbackInfo ci, MinecraftClient minecraftClient, int k, int l, boolean bl, List list, ItemStack itemStack, int m) {
		itemDrawOffset = m;
	}

	@Inject(
			method = "renderButton",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;renderInGui(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/item/ItemStack;II)V", shift = Shift.AFTER),
			locals = LocalCapture.CAPTURE_FAILSOFT
	)
	private void drawButton(MatrixStack matrixStack, int i, int j, float f, CallbackInfo ci, MinecraftClient minecraftClient, int k, int l, boolean bl, List list, ItemStack itemStack, int m) {
		minecraftClient.getItemRenderer().renderGuiItemOverlay(matrixStack, minecraftClient.textRenderer, itemStack, this.getX() + itemDrawOffset, this.getY() + itemDrawOffset);
	}
}

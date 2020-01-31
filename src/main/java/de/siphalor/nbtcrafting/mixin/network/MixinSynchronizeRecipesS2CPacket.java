package de.siphalor.nbtcrafting.mixin.network;

import de.siphalor.nbtcrafting.NbtCrafting;
import de.siphalor.nbtcrafting.util.ServerRecipe;
import de.siphalor.nbtcrafting.util.duck.IServerPlayerEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.packet.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(SynchronizeRecipesS2CPacket.class)
public abstract class MixinSynchronizeRecipesS2CPacket {
	@Shadow private List<Recipe<?>> recipes;

	@Shadow
	public static <T extends Recipe<?>> void writeRecipe(T recipe, PacketByteBuf buf) {}

	@Inject(method = "write", at = @At("HEAD"), cancellable = true)
	public void onWrite(PacketByteBuf buf, CallbackInfo callbackInfo) {
		if(!((IServerPlayerEntity) NbtCrafting.lastServerPlayerEntity).hasClientMod()) {
			List<Recipe<?>> syncRecipes = recipes.stream().filter(recipe -> !(recipe instanceof ServerRecipe)).collect(Collectors.toList());
			buf.writeVarInt(syncRecipes.size());
			syncRecipes.forEach(recipe -> writeRecipe(recipe, buf));
			callbackInfo.cancel();
		}
	}

	@Inject(method = "read", at = @At("HEAD"))
	public void onRead(PacketByteBuf buf, CallbackInfo callbackInfo) {
		File dir = FabricLoader.getInstance().getGameDirectory().toPath().resolve("nbtc-debug").toFile();
		dir.mkdirs();
		File file = dir.toPath().resolve(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-S").format(LocalDateTime.now()) + ".dump").toFile();
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			NbtCrafting.tempBB.forEachByte(value -> {fileOutputStream.write(value); return true;});
			fileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

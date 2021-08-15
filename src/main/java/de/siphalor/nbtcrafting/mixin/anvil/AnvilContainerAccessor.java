package de.siphalor.nbtcrafting.mixin.anvil;

import net.minecraft.container.AnvilContainer;
import net.minecraft.container.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnvilContainer.class)
public interface AnvilContainerAccessor {
	@Accessor
	Property getLevelCost();
}

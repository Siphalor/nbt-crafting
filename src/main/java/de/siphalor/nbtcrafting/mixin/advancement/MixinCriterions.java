package de.siphalor.nbtcrafting.mixin.advancement;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Criteria.class)
public interface MixinCriterions {
	@Invoker("register")
	static <T extends Criterion<?>> T registerCriterion(T criterion) {
		return null;
	}
}

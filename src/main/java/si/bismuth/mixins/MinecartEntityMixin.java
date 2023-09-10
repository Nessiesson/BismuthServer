package si.bismuth.mixins;

import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MinecartEntity.class)
public class MinecartEntityMixin {
	@ModifyConstant(method = "moveOnRail", constant = @Constant(classValue = LivingEntity.class, ordinal = 0))
	private Class<? extends LivingEntity> mc64836(Object value, Class<LivingEntity> clazz) {
		return PlayerEntity.class;
	}
}

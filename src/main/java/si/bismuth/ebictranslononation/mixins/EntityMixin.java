package si.bismuth.ebictranslononation.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
	@Redirect(method = "move", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/MoverType;PISTON:Lnet/minecraft/entity/MoverType;", opcode = Opcodes.GETSTATIC))
	private MoverType mc124718(MoverType type) {
		return null;
	}
}

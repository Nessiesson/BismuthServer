package si.bismuth.mixins;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Mixin(WorldChunk.class)
public class WorldChunkMixin {
	@Redirect(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", opcode = Opcodes.INVOKESTATIC, remap = false))
	private HashMap<BlockPos, BlockEntity> reloadUpdateOrderFix() {
		return new LinkedHashMap<>();
	}

	@Redirect(method = "addEntity", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
	private void silenceWrongLocationShouldBe(Logger logger, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
		// noop
	}
}

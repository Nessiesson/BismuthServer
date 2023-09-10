package si.bismuth.mixins;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerWorld.class)
public interface IServerWorld {
	@Invoker
	boolean callIsChunkLoadedAt(int x, int z, boolean allowEmpty);
}

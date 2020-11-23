package si.bismuth.mixins;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import si.bismuth.utils.SpawnReporter;

@Mixin(WorldEntitySpawner.class)
public abstract class MixinWorldEntitySpawner {
	private static final EnumCreatureType[] OVERWORLD = new EnumCreatureType[]{EnumCreatureType.MONSTER, EnumCreatureType.CREATURE, EnumCreatureType.WATER_CREATURE, EnumCreatureType.AMBIENT};
	private static final EnumCreatureType[] OTHERWORLD = new EnumCreatureType[]{EnumCreatureType.MONSTER};
	@Shadow
	@Final
	private static int MOB_COUNT_DIV;

	@Inject(method = "findChunksForSpawning", at = @At(value = "FIELD", target = "Lnet/minecraft/world/WorldEntitySpawner;MOB_COUNT_DIV:I", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void getMobcaps(WorldServer server, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate, CallbackInfoReturnable<Integer> cir, int chunkAddsToMobcap, int mobTypeSpawned, BlockPos spawnPoint, EnumCreatureType[] mobCategories, int idk, int wtf, EnumCreatureType mobCategory, int loadedOfMobCategory) {
		final int mobCap = mobCategory.getMaxNumberOfCreature() * chunkAddsToMobcap / MOB_COUNT_DIV;
		SpawnReporter.mobcaps.get(server.provider.getDimensionType().getId()).put(mobCategory, new Tuple<>(loadedOfMobCategory, mobCap));
	}

	@Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EnumCreatureType;values()[Lnet/minecraft/entity/EnumCreatureType;"))
	private EnumCreatureType[] preventUselessMobSpawningAttemptsInIncorrectDimensions(WorldServer world) {
		return world.provider.isSurfaceWorld() ? OVERWORLD : OTHERWORLD;
	}
}

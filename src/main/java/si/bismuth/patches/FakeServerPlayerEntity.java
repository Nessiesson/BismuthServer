package si.bismuth.patches;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityHeadAnglesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTeleportS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.criterion.ScoreboardCriterion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@SuppressWarnings("EntityConstructor")
public class FakeServerPlayerEntity extends ServerPlayerEntity {
	private double lastReportedPosX;
	private double lastReportedPosY;
	private double lastReportedPosZ;

	private double setX;
	private double setY;
	private double setZ;
	private float setYaw;
	private float setPitch;

	private final Scoreboard scoreboard = new FakeScoreboard();

	private FakeServerPlayerEntity(MinecraftServer server, ServerWorld world, GameProfile profile, ServerPlayerInteractionManager interactionManager) {
		super(server, world, profile, interactionManager);
	}

	public static void createFake(String username, MinecraftServer server, double x, double y, double z, double yaw, double pitch, int dimension, int gamemode) {
		ServerWorld worldIn = server.getWorld(dimension);
		ServerPlayerInteractionManager interactionManagerIn = new ServerPlayerInteractionManager(worldIn);
		GameProfile gameprofile = server.getGameProfileCache().get(username);
		if (gameprofile == null) {
			UUID uuid = PlayerEntity.getUuid(new GameProfile(null, username));
			gameprofile = new GameProfile(uuid, username);
		}

		gameprofile = fixSkin(gameprofile);
		FakeServerPlayerEntity instance = new FakeServerPlayerEntity(server, worldIn, gameprofile, interactionManagerIn);
		instance.setSetPosition(x, y, z, (float) yaw, (float) pitch);
		server.getPlayerManager().onLogin(new FakeConnection(), instance);
		if (instance.dimensionId != dimension) //player was logged in in a different dimension
		{
			ServerWorld old_world = server.getWorld(instance.dimensionId);
			instance.dimensionId = dimension;
			old_world.removeEntityNow(instance);
			instance.removed = false;
			worldIn.addEntity(instance);
			instance.setWorld(worldIn);
			server.getPlayerManager().onChangedDimension(instance, old_world);
			instance.networkHandler.teleport(x, y, z, (float) yaw, (float) pitch);
			instance.interactionManager.setWorld(worldIn);
		}
		instance.setHealth(20.0F);
		instance.removed = false;
		instance.stepHeight = 0.6F;
		interactionManagerIn.setGameMode(GameMode.byId(gamemode));
		server.getPlayerManager().sendPacket(new EntityHeadAnglesS2CPacket(instance, (byte) (instance.headYaw * 256 / 360)), instance.dimensionId);
		server.getPlayerManager().sendPacket(new EntityTeleportS2CPacket(instance), instance.dimensionId);
		server.getPlayerManager().move(instance);
		instance.dataTracker.set(MODEL_PARTS, (byte) 0x7f); // show all model layers (incl. capes)
	}

	private static GameProfile fixSkin(GameProfile profile) {
		if (!profile.getProperties().containsKey("texture"))
			return SkullBlockEntity.updateProfile(profile);
		else
			return profile;
	}

	@Override
	public void discard() {
		logout();
	}

	@Override
	public void tick() {
		super.tick();
		this.tickPlayer();
		this.playerMoved();
	}

	@Override
	public void onKilled(DamageSource cause) {
		super.onKilled(cause);
		logout();
	}

	private void logout() {
		this.stopRiding();
		getServer().getPlayerManager().remove(this);
	}

	private void playerMoved() {
		if (x != lastReportedPosX || y != lastReportedPosY || z != lastReportedPosZ) {
			server.getPlayerManager().move(this);
			lastReportedPosX = x;
			lastReportedPosY = y;
			lastReportedPosZ = z;
		}
	}

	private void setSetPosition(double x, double y, double z, float yaw, float pitch) {
		this.setX = x;
		this.setY = y;
		this.setZ = z;
		this.setYaw = yaw;
		this.setPitch = pitch;
	}

	public void resetToSetPosition() {
		refreshPositionAndAngles(setX, setY, setZ, setYaw, setPitch);
	}

	@Override
	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	private class FakeScoreboard extends Scoreboard {
		@Override
		public Collection<ScoreboardObjective> getObjectives(ScoreboardCriterion criteria) {
			return new ArrayList<>();
		}
	}
}

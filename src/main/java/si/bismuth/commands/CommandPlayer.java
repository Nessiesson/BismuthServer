package si.bismuth.commands;

import si.bismuth.patches.EntityPlayerMPFake;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandPlayer extends CommandBismuthBase {
	public String getName() {
		return "player";
	}

	public String getUsage(CommandSource sender) {
		return "player <player_name> <spawn|kill> ";
	}

	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (args.length < 2) {
			throw new IncorrectUsageException("player <x> <kill|spawn>");
		}
		String playerName = args[0];
		String action = args[1];
		ServerPlayerEntity player = server.getPlayerManager().get(playerName);
		if (sender instanceof PlayerEntity) {
			PlayerEntity sendingPlayer = asPlayer(sender);
			if (!(server.getPlayerManager().isOp(sendingPlayer.getGameProfile()))) {
				if (!(sendingPlayer == player || player == null || player instanceof EntityPlayerMPFake)) {
					throw new IncorrectUsageException("Non OP players can't control other players");
				}
			}
		}
		if (player == null && !action.equalsIgnoreCase("spawn")) {
			throw new IncorrectUsageException("player doesn't exist");
		}

		if ("spawn".equalsIgnoreCase(action)) {
			if (player != null) {
				throw new IncorrectUsageException("player " + playerName + " already exists");
			} else if (Arrays.asList(server.getPlayerManager().getWhitelistNames()).contains(playerName)) {
				throw new IncorrectUsageException("You can't spawn whitelisted players");
			}
			if (playerName.length() < 3 || playerName.length() > 16) {
				throw new IncorrectUsageException("player names can only be 3 to 16 chars long");
			}
			Vec3d vec3d = sender.getSourcePos();
			double d0 = vec3d.x;
			double d1 = vec3d.y;
			double d2 = vec3d.z;
			double yaw = 0.0D;
			double pitch = 0.0D;
			World world = sender.getSourceWorld();
			int dimension = world.dimension.getType().getId();

			if (sender instanceof ServerPlayerEntity) {
				ServerPlayerEntity entity = asPlayer(sender);
				yaw = entity.yaw;
				pitch = entity.pitch;
			}
			if (args.length >= 5) {
				d0 = parseTeleportCoordinate(d0, args[2], true).getCoordinate();
				d1 = parseTeleportCoordinate(d1, args[3], -4096, 4096, false).getCoordinate();
				d2 = parseTeleportCoordinate(d2, args[4], true).getCoordinate();
				yaw = parseTeleportCoordinate(yaw, args.length > 5 ? args[5] : "~", false).getCoordinate();
				pitch = parseTeleportCoordinate(pitch, args.length > 6 ? args[6] : "~", false).getCoordinate();
			}
			if (args.length >= 8) {
				String dimension_string = args[7];
				dimension = 0;
				if ("nether".equalsIgnoreCase(dimension_string)) {
					dimension = -1;
				}
				if ("end".equalsIgnoreCase(dimension_string)) {
					dimension = 1;
				}
			}
			EntityPlayerMPFake.createFake(playerName, server, d0, d1, d2, yaw, pitch, dimension, 3);
			return;
		}
		if ("kill".equalsIgnoreCase(action)) {
			if (!(player instanceof EntityPlayerMPFake)) {
				throw new IncorrectUsageException("use /kill or /kick on regular players");
			}
			player.discard();
			return;
		}
		throw new IncorrectUsageException("unknown action: " + action);
	}

	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
		if (args.length == 1) {
			Set<String> players = new HashSet<>(Arrays.asList(server.getPlayerNames()));
			players.add("Steve");
			players.add("Alex");
			return suggestMatching(args, players.toArray(new String[0]));
		}
		if (args.length == 2) {
			//currently for all, needs to be restricted for Fake plaeyrs
			return suggestMatching(args,
					"spawn", "kill");
		}
		if (args.length > 2 && (args[1].equalsIgnoreCase("spawn"))) {
			if (args.length <= 5) {
				return suggestCoordinate(args, 2, targetPos);
			} else if (args.length <= 7) {
				return suggestMatching(args, "0.0");
			} else if (args.length == 8) {
				return suggestMatching(args, "overworld", "end", "nether");
			}
		}
		return Collections.emptyList();
	}
}

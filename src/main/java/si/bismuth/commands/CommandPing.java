package si.bismuth.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandPing extends CommandBismuthBase {
	@Override
	public String getName() {
		return "ping";
	}

	@Override
	public String getUsage(CommandSource sender) {
		return "ping [player]";
	}

	@Override
	public void run(MinecraftServer server, CommandSource sender, String[] args) throws CommandException {
		if (!(sender instanceof ServerPlayerEntity)) {
			return;
		}

		ServerPlayerEntity player = asPlayer(sender);
		if (args.length > 0) {
			final ServerPlayerEntity name = server.getPlayerManager().get(args[0]);
			if (name != null) {
				player = name;
			}
		}

		sender.sendMessage(new LiteralText("Ping of " + player.getName() + " is " + player.ping + "ms."));
	}

	@Override
	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? suggestMatching(args, server.getPlayerNames()) : Collections.emptyList();
	}
}

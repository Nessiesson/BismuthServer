package si.bismuth.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.exception.CommandException;
import net.minecraft.server.command.exception.IncorrectUsageException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.util.math.BlockPos;
import si.bismuth.utils.Profiler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class TickCommand extends CommandBismuthBase {
	/**
	 * Gets the name of the command
	 */
	public String getName() {
		return "tick";
	}

	/**
	 * Gets the usage string for the command.
	 */
	public String getUsage(CommandSource sender) {
		return "Usage: tick <health|entities> [duration]";
	}

	/**
	 * Callback for when the command is executed
	 */
	public void run(final MinecraftServer server, final CommandSource sender, String[] args) throws CommandException {
		if (args.length == 0) {
			throw new IncorrectUsageException(getUsage(sender));
		}

		if ("health".equalsIgnoreCase(args[0])) {
			int step = 100;
			if (args.length > 1) {
				step = parseInt(args[1], 20, 72000);
			}

			Profiler.prepare_tick_report(step);
			return;
		} else if ("entity".equalsIgnoreCase(args[0]) || "entities".equalsIgnoreCase(args[0])) {
			int step = 100;
			if (args.length > 1) {
				step = parseInt(args[1], 20, 72000);
			}

			Profiler.prepare_entity_report(step);
			return;
		}

		throw new IncorrectUsageException(getUsage(sender));
	}

	public List<String> getSuggestions(MinecraftServer server, CommandSource sender, String[] args, @Nullable BlockPos pos) {
		if (args.length == 1) {
			return suggestMatching(args, "health", "entities");
		}

		if (args.length == 2) {
			if ("health".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "100", "200", "1000");
			}

			if ("entities".equalsIgnoreCase(args[0])) {
				return suggestMatching(args, "100", "200", "1000");
			}
		}
		return Collections.emptyList();
	}
}

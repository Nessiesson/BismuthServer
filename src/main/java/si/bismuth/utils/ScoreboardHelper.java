package si.bismuth.utils;

import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import org.apache.commons.lang3.StringUtils;
import si.bismuth.MCServer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ScoreboardHelper {
	private static final ServerScoreboard board = (ServerScoreboard) MCServer.server.getWorld(0).getScoreboard();

	public static void setScoreboard(List<String> args, int displaySlot) {
		final ScoreObjective objective = ScoreboardHelper.getObjective(args, board);
		board.setObjectiveInDisplaySlot(displaySlot, objective);
	}

	public static ScoreObjective getObjective(List<String> args, Scoreboard board) {
		final Collection<String> objectives = board.getScoreObjectives().stream().map(ScoreObjective::getName).collect(Collectors.toList());
		return args.size() > 2 ? board.getObjective(getClosestMatch(objectives, args.get(2))) : null;
	}

	private static String getClosestMatch(Collection<String> collection, String target) {
		int distance = Integer.MAX_VALUE;
		String closest = "";
		for (final String s : collection) {
			if (target.equalsIgnoreCase(s)) {
				return s;
			}

			int currentDistance = StringUtils.getLevenshteinDistance(s, target);
			if (currentDistance < distance) {
				distance = currentDistance;
				closest = s;
			}
		}

		return closest;
	}
}

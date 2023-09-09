package si.bismuth.scoreboard;

import net.minecraft.scoreboard.ScoreObjective;

import java.util.Collection;
import java.util.Map;

public interface IScoreboard {
    LongScore getOrCreateScore(String playerName, ScoreObjective objective);

    Collection<LongScore> getSortedScores(ScoreObjective p_getSortedScores_1_);

    Collection<LongScore> getScores();

    Map<ScoreObjective, LongScore> getObjectivesForEntity(String p_getObjectivesForEntity_1_);
}

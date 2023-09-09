package si.bismuth.scoreboard;

import net.minecraft.scoreboard.ScoreObjective;

import java.util.Comparator;

public class LongScore {
    public static final Comparator<LongScore> SCORE_COMPARATOR = new Comparator<LongScore>() {
        public int compare(LongScore p_compare_1_, LongScore p_compare_2_) {
            if (p_compare_1_.getScorePoints() > p_compare_2_.getScorePoints()) {
                return 1;
            } else {
                return p_compare_1_.getScorePoints() < p_compare_2_.getScorePoints() ? -1 : p_compare_2_.getPlayerName().compareToIgnoreCase(p_compare_1_.getPlayerName());
            }
        }
    };
    private final IScoreboard scoreboard;
    private final ScoreObjective objective;
    private final String scorePlayerName;
    private long scorePoints;
    private boolean locked;
    private boolean forceUpdate;

    public LongScore(IScoreboard p_i2309_1_, ScoreObjective p_i2309_2_, String p_i2309_3_) {
        this.scoreboard = p_i2309_1_;
        this.objective = p_i2309_2_;
        this.scorePlayerName = p_i2309_3_;
        this.forceUpdate = true;
    }

    public void increaseScore(long p_increaseScore_1_) {
        if (this.objective.getCriteria().isReadOnly()) {
            throw new IllegalStateException("Cannot modify read-only score");
        } else {
            this.setScorePoints(this.getScorePoints() + p_increaseScore_1_);
            if (!"Total".equals(scorePlayerName)){
                final IScoreboard scoreboard = this.scoreboard;
                final LongScore totalScore = scoreboard.getOrCreateScore("Total", objective);
                if (totalScore.getScorePoints() > -1){
                    totalScore.increaseScore(p_increaseScore_1_);
                }
            }
        }
    }

    public void decreaseScore(long p_decreaseScore_1_) {
        if (this.objective.getCriteria().isReadOnly()) {
            throw new IllegalStateException("Cannot modify read-only score");
        } else {
            this.setScorePoints(this.getScorePoints() - p_decreaseScore_1_);
        }
    }

    public void incrementScore() {
        if (this.objective.getCriteria().isReadOnly()) {
            throw new IllegalStateException("Cannot modify read-only score");
        } else {
            this.increaseScore(1);
        }
    }

    public long getScorePoints() {
        return this.scorePoints;
    }

    public void setScorePoints(long p_setScorePoints_1_) {
        long lvt_2_1_ = this.scorePoints;
        this.scorePoints = p_setScorePoints_1_;
        if (lvt_2_1_ != p_setScorePoints_1_ || this.forceUpdate) {
            //TODO Calculate the change and apply that to the total score
            this.forceUpdate = false;
            IServerScoreboard scoreboard = (IServerScoreboard) this.getScoreScoreboard();
            scoreboard.onScoreUpdated(this);
        }
    }

    public ScoreObjective getObjective() {
        return this.objective;
    }

    public String getPlayerName() {
        return this.scorePlayerName;
    }

    public IScoreboard getScoreScoreboard() {
        return this.scoreboard;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean p_setLocked_1_) {
        this.locked = p_setLocked_1_;
    }
}

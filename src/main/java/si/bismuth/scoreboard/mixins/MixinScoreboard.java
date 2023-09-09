package si.bismuth.scoreboard.mixins;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import si.bismuth.scoreboard.IScoreboard;
import si.bismuth.scoreboard.LongScore;

import java.util.*;

@Mixin(Scoreboard.class)
public abstract class MixinScoreboard implements IScoreboard {
    private final Map<String, Map<ScoreObjective, LongScore>> entitiesScoreObjectives = Maps.newHashMap();

    @Shadow @Final
    private Map<String, ScoreObjective> scoreObjectives;

    @Shadow @Final
    private Map<IScoreCriteria, List<ScoreObjective>> scoreObjectiveCriterias;

    @Shadow
    public abstract void broadcastScoreUpdate(String scoreName);

    @Shadow
    public abstract void broadcastScoreUpdate(String scoreName, ScoreObjective objective);

    @Shadow
    public abstract void setObjectiveInDisplaySlot(int objectiveSlot, ScoreObjective objective);

    @Shadow
    public abstract ScoreObjective getObjectiveInDisplaySlot(int slotIn);

    @Shadow
    public abstract void onScoreObjectiveRemoved(ScoreObjective objective);


    @Override
    public LongScore getOrCreateScore(String playerName, ScoreObjective objective) {
        if (playerName.length() > 40) {
            throw new IllegalArgumentException("The player name '" + playerName + "' is too long!");
        } else {
            Map<ScoreObjective, LongScore> playerScoreboards = this.entitiesScoreObjectives.computeIfAbsent(playerName, k -> Maps.newHashMap());

            LongScore longScore = (LongScore) ((Map<?, ?>)playerScoreboards).get(objective);
            if (longScore == null) {
                longScore = new LongScore(this, objective, playerName);
                playerScoreboards.put(objective, longScore);
            }

            return longScore;
        }
    }

    @Override
    public Collection<LongScore> getSortedScores(ScoreObjective p_getSortedScores_1_) {
        List<LongScore> lvt_2_1_ = Lists.newArrayList();

        for (Map<ScoreObjective, LongScore> scoreObjectiveLongScoreMap : this.entitiesScoreObjectives.values()) {
            LongScore lvt_5_1_ = scoreObjectiveLongScoreMap.get(p_getSortedScores_1_);
            if (lvt_5_1_ != null) {
                lvt_2_1_.add(lvt_5_1_);
            }
        }

        lvt_2_1_.sort(LongScore.SCORE_COMPARATOR);
        return lvt_2_1_;
    }

    @Override
    public Collection<LongScore> getScores() {
        Collection<Map<ScoreObjective, LongScore>> lvt_1_1_ = this.entitiesScoreObjectives.values();
        List<LongScore> lvt_2_1_ = Lists.newArrayList();

        for (Map<ScoreObjective, LongScore> scoreObjectiveLongScoreMap : lvt_1_1_) {
            lvt_2_1_.addAll(scoreObjectiveLongScoreMap.values());
        }

        return lvt_2_1_;
    }

    @Override
    public Map<ScoreObjective, LongScore> getObjectivesForEntity(String p_getObjectivesForEntity_1_) {
        Map<ScoreObjective, LongScore> lvt_2_1_ = this.entitiesScoreObjectives.get(p_getObjectivesForEntity_1_);
        if (lvt_2_1_ == null) {
            lvt_2_1_ = Maps.newHashMap();
        }

        return lvt_2_1_;
    }

    @Inject(method = "entityHasObjective", at = @At(value = "HEAD"), cancellable = true)
    void entityHasObjective(String name, ScoreObjective objective, CallbackInfoReturnable<Boolean> cir) {
        Map<ScoreObjective, LongScore> map = this.entitiesScoreObjectives.get(name);

        if (map == null)
        {
            cir.setReturnValue(Boolean.FALSE);
        }
        else
        {
            LongScore score = map.get(objective);
            cir.setReturnValue(score != null);
        }
    }

    @Inject(method = "removeObjectiveFromEntity", at = @At(value = "HEAD"), cancellable = true)
    void removeObjectiveFromEntity(String name, ScoreObjective objective, CallbackInfo ci) {
        if (objective == null)
        {
            Map<ScoreObjective, LongScore> map = this.entitiesScoreObjectives.remove(name);

            if (map != null)
            {
                this.broadcastScoreUpdate(name);
            }
        }
        else
        {
            Map<ScoreObjective, LongScore> map2 = this.entitiesScoreObjectives.get(name);

            if (map2 != null)
            {
                LongScore score = map2.remove(objective);

                if (map2.size() < 1)
                {
                    Map<ScoreObjective, LongScore> map1 = this.entitiesScoreObjectives.remove(name);

                    if (map1 != null)
                    {
                        this.broadcastScoreUpdate(name);
                    }
                }
                else if (score != null)
                {
                    this.broadcastScoreUpdate(name, objective);
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "removeObjective", at = @At(value = "HEAD"), cancellable = true)
    void removeObjective(ScoreObjective objective, CallbackInfo ci) {
        this.scoreObjectives.remove(objective.getName());

        for (int i = 0; i < 19; ++i)
        {
            if (this.getObjectiveInDisplaySlot(i) == objective)
            {
                this.setObjectiveInDisplaySlot(i, null);
            }
        }

        List<ScoreObjective> list = this.scoreObjectiveCriterias.get(objective.getCriteria());

        if (list != null)
        {
            list.remove(objective);
        }

        for (Map<ScoreObjective, LongScore> map : this.entitiesScoreObjectives.values())
        {
            map.remove(objective);
        }

        this.onScoreObjectiveRemoved(objective);
        ci.cancel();
    }
}

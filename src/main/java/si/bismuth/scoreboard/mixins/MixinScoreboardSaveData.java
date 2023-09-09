package si.bismuth.scoreboard.mixins;

import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardSaveData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import si.bismuth.scoreboard.IScoreboard;
import si.bismuth.scoreboard.IScoreboardSaveData;
import si.bismuth.scoreboard.LongScore;

import java.util.Collection;
import java.util.Map;

@Mixin(ScoreboardSaveData.class)
public class MixinScoreboardSaveData implements IScoreboardSaveData {
    @Shadow
    private Scoreboard scoreboard;

    @Inject(method="setScoreboard", at=@At(value="TAIL", target="Lnet/minecraft/scoreboard/ScoreboardSaveData;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V"))
    private void updateTotals(CallbackInfo ci){
        final Map<ScoreObjective, Long> totalsMap = Maps.<ScoreObjective, Long>newHashMap();
        IScoreboard scoreboard = (IScoreboard) this.scoreboard;

        for (LongScore longScore : scoreboard.getScores()){
            if (!"Total".equals(longScore.getPlayerName())){
                totalsMap.put(longScore.getObjective(), totalsMap.getOrDefault(longScore.getObjective(), 0L) + longScore.getScorePoints());
            }
        }

        for (ScoreObjective objective : totalsMap.keySet()){
            long total = totalsMap.get(objective);
            scoreboard.getOrCreateScore("Total", objective).setScorePoints(total);
        }
    }

    @Redirect(method = "readFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardSaveData;readScores(Lnet/minecraft/nbt/NBTTagList;)V"))
    private void readFromNBT(ScoreboardSaveData instance, NBTTagList p_readScores_1_) {
        IScoreboardSaveData scoreboardSaveData = (IScoreboardSaveData) instance;
        scoreboardSaveData.readScores(p_readScores_1_);
    }

    @Redirect(method = "writeToNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardSaveData;scoresToNbt()Lnet/minecraft/nbt/NBTTagList;"))
    private NBTTagList writeToNBT(ScoreboardSaveData instance) {
        IScoreboardSaveData scoreboardSaveData = (IScoreboardSaveData) instance;
        return scoreboardSaveData.scoresToNbt();
    }

    @Override
    public void readScores(NBTTagList p_readScores_1_) {
        for(int lvt_2_1_ = 0; lvt_2_1_ < p_readScores_1_.tagCount(); ++lvt_2_1_) {
            NBTTagCompound lvt_3_1_ = p_readScores_1_.getCompoundTagAt(lvt_2_1_);
            ScoreObjective lvt_4_1_ = this.scoreboard.getObjective(lvt_3_1_.getString("Objective"));
            String lvt_5_1_ = lvt_3_1_.getString("Name");
            if (lvt_5_1_.length() > 40) {
                lvt_5_1_ = lvt_5_1_.substring(0, 40);
            }

            IScoreboard scoreboard = (IScoreboard) this.scoreboard;
            LongScore lvt_6_1_ = scoreboard.getOrCreateScore(lvt_5_1_, lvt_4_1_);
            lvt_6_1_.setScorePoints(lvt_3_1_.getLong("Score"));
            if (lvt_3_1_.hasKey("Locked")) {
                lvt_6_1_.setLocked(lvt_3_1_.getBoolean("Locked"));
            }
        }
    }

    @Override
    public NBTTagList scoresToNbt() {
        NBTTagList lvt_1_1_ = new NBTTagList();
        IScoreboard scoreboard = (IScoreboard) this.scoreboard;
        Collection<LongScore> lvt_2_1_ = scoreboard.getScores();

        for (LongScore lvt_4_1_ : lvt_2_1_) {
            if (lvt_4_1_.getObjective() != null) {
                NBTTagCompound lvt_5_1_ = new NBTTagCompound();
                lvt_5_1_.setString("Name", lvt_4_1_.getPlayerName());
                lvt_5_1_.setString("Objective", lvt_4_1_.getObjective().getName());
                lvt_5_1_.setLong("Score", lvt_4_1_.getScorePoints());
                lvt_5_1_.setBoolean("Locked", lvt_4_1_.isLocked());
                lvt_1_1_.appendTag(lvt_5_1_);
            }
        }
        return lvt_1_1_;
    }
}

package si.bismuth.scoreboard.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.*;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import si.bismuth.scoreboard.IScoreboard;
import si.bismuth.scoreboard.LongScore;

import java.util.Collection;
import java.util.Iterator;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer {
    public MixinEntityPlayerMP(World world, GameProfile profile) {
        super(world, profile);
    }

    @Shadow @Final private StatisticsManagerServer statsFile;

    @Shadow
    public abstract boolean isSpectator();

    /**
     * @author brrr
     */
    @Overwrite
    private void updateScorePoints(IScoreCriteria p_updateScorePoints_1_, int p_updateScorePoints_2_) {
        Collection<ScoreObjective> lvt_3_1_ = this.getWorldScoreboard().getObjectivesFromCriteria(p_updateScorePoints_1_);

        for (ScoreObjective lvt_5_1_ : lvt_3_1_) {
            LongScore lvt_6_1_ = ((IScoreboard) this.getWorldScoreboard()).getOrCreateScore(this.getName(), lvt_5_1_);
            lvt_6_1_.setScorePoints(p_updateScorePoints_2_);
        }
    }

    //TODO For some reason it can't capture the collection like wtf currently using above overwrite
//    @Inject(method = "updateScorePoints", at = @At(value = "INVOKE", target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
//    private void updateScorePoints(IScoreCriteria p_updateScorePoints_1_, int p_updateScorePoints_2_, CallbackInfo ci, Collection<ScoreObjective> lvt_3_1_) {
//        for (ScoreObjective lvt_5_1_ : lvt_3_1_) {
//            LongScore lvt_6_1_ = ((IScoreboard) this.getWorldScoreboard()).getOrCreateScore(this.getName(), lvt_5_1_);
//            lvt_6_1_.setScorePoints(p_updateScorePoints_2_);
//        }
//        ci.cancel();
//    }

    //TODO conflicts with collection when using CAPTURE_FAILHARD
    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;getWorldScoreboard()Lnet/minecraft/scoreboard/Scoreboard;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onDeath(DamageSource p_onDeath_1_, CallbackInfo ci, boolean lvt_2_1_, Collection<ScoreObjective> lvt_3_2_, Iterator<ScoreObjective> var4, ScoreObjective lvt_5_1_) {
        LongScore lvt_6_1_ = ((IScoreboard) this.getWorldScoreboard()).getOrCreateScore(this.getName(), lvt_5_1_);
        lvt_6_1_.incrementScore();
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getOrCreateScore(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreObjective;)Lnet/minecraft/scoreboard/Score;"))
    private Score onDeath2(Scoreboard instance, String p_getOrCreateScore_1_, ScoreObjective p_getOrCreateScore_2_) {
        // Return dummy score that will be garbage collected later on to prevent filling up the old scoreboard datastructure
        return new Score(instance, p_getOrCreateScore_2_, p_getOrCreateScore_1_);
    }

    @Redirect(method = "awardKillScore", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getOrCreateScore(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreObjective;)Lnet/minecraft/scoreboard/Score;"))
    private Score awardKillScore(Scoreboard instance, String p_getOrCreateScore_1_, ScoreObjective p_getOrCreateScore_2_) {
        ((IScoreboard) instance).getOrCreateScore(p_getOrCreateScore_1_, p_getOrCreateScore_2_).incrementScore();
        // Return dummy score that will be garbage collected later on to prevent filling up the old scoreboard datastructure
        return new Score(instance, p_getOrCreateScore_2_, p_getOrCreateScore_1_);
    }

    @Inject(method = "awardTeamKillScores", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayerMP;getWorldScoreboard()Lnet/minecraft/scoreboard/Scoreboard;", ordinal = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    private void awardTeamKillScores(Entity p_awardTeamKillScores_1_, CallbackInfoReturnable<Collection<ScoreObjective>> cir, String lvt_2_1_, ScorePlayerTeam lvt_3_1_, int lvt_4_1_, Iterator<ScoreObjective> var5, ScoreObjective lvt_6_1_) {
        LongScore longScore = ((IScoreboard) this.getWorldScoreboard()).getOrCreateScore(this.getName(), lvt_6_1_);
        longScore.incrementScore();
    }

    @Redirect(method = "awardTeamKillScores", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getOrCreateScore(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreObjective;)Lnet/minecraft/scoreboard/Score;"))
    private Score awardTeamKillScores2(Scoreboard instance, String p_getOrCreateScore_1_, ScoreObjective p_getOrCreateScore_2_) {
        // Return dummy score that will be garbage collected later on to prevent filling up the old scoreboard datastructure
        return new Score(instance, p_getOrCreateScore_2_, p_getOrCreateScore_1_);
    }

    @Redirect(method = "addStat", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Scoreboard;getOrCreateScore(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreObjective;)Lnet/minecraft/scoreboard/Score;"))
    private Score addStat(Scoreboard instance, String p_getOrCreateScore_1_, ScoreObjective p_getOrCreateScore_2_) {
        ((IScoreboard) instance).getOrCreateScore(p_getOrCreateScore_1_, p_getOrCreateScore_2_).incrementScore();
        // Return dummy score that will be garbage collected later on to prevent filling up the old scoreboard datastructure
        return new Score(instance, p_getOrCreateScore_2_, p_getOrCreateScore_1_);
    }

    @Inject(method = "updateScorePoints", at = @At(value = "HEAD"), cancellable = true)
    void updateScorePoints(IScoreCriteria p_updateScorePoints_1_, int p_updateScorePoints_2_, CallbackInfo ci) {
        for(ScoreObjective lvt_5_1_ : this.getWorldScoreboard().getObjectivesFromCriteria(p_updateScorePoints_1_)) {
            LongScore lvt_6_1_ = ((IScoreboard)this.getWorldScoreboard()).getOrCreateScore(this.getName(), lvt_5_1_);
            lvt_6_1_.setScorePoints(p_updateScorePoints_2_);
        }
        ci.cancel();
    }

    @Inject(method = "takeStat", at = @At(value = "HEAD"), cancellable = true)
    void takeStat(StatBase p_takeStat_1_, CallbackInfo ci) {
        if (p_takeStat_1_ != null) {
            this.statsFile.unlockAchievement(this, p_takeStat_1_, 0);

            for(ScoreObjective lvt_3_1_ : this.getWorldScoreboard().getObjectivesFromCriteria(p_takeStat_1_.getCriteria())) {
                ((IScoreboard)this.getWorldScoreboard()).getOrCreateScore(this.getName(), lvt_3_1_).setScorePoints(0);
            }

        }
        ci.cancel();
    }
}

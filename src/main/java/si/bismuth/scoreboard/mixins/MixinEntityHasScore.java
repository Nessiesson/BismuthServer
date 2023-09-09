package si.bismuth.scoreboard.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.EntityHasScore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import si.bismuth.scoreboard.IScoreboard;

@Mixin(EntityHasScore.class)
public class MixinEntityHasScore {
    // TODO insert in randomValueRange or some shit to make this more clean
    public boolean isInRange(long p_isInRange_1_, RandomValueRange p_entityScoreMatch_4_) {
        return (float)p_isInRange_1_ <= p_entityScoreMatch_4_.getMax() && (float)p_isInRange_1_ >= p_entityScoreMatch_4_.getMin();
    }


    @Inject(method = "entityScoreMatch", at = @At(value = "HEAD"), cancellable = true)
    void entityScoreMatch(Entity p_entityScoreMatch_1_, Scoreboard p_entityScoreMatch_2_, String p_entityScoreMatch_3_, RandomValueRange p_entityScoreMatch_4_, CallbackInfoReturnable<Boolean> cir) {
        ScoreObjective lvt_5_1_ = p_entityScoreMatch_2_.getObjective(p_entityScoreMatch_3_);
        if (lvt_5_1_ == null) {
            cir.setReturnValue(Boolean.FALSE);
        } else {
            String lvt_6_1_ = p_entityScoreMatch_1_ instanceof EntityPlayerMP
                    ? p_entityScoreMatch_1_.getName()
                    : p_entityScoreMatch_1_.getCachedUniqueIdString();
            if (!p_entityScoreMatch_2_.entityHasObjective(lvt_6_1_, lvt_5_1_)) {
                cir.setReturnValue(Boolean.FALSE);
            } else {
                isInRange(((IScoreboard)p_entityScoreMatch_2_).getOrCreateScore(lvt_6_1_, lvt_5_1_).getScorePoints(), p_entityScoreMatch_4_);
            }
        }
    }
}

package si.bismuth.scoreboard.mixins;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import si.bismuth.scoreboard.IScoreboard;
import si.bismuth.scoreboard.LongScore;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(EntitySelector.class)
public class MixinEntitySelector {
    private static long getLong(String p_getInt_0_, long p_getInt_1_) {
        try {
            return Long.parseLong(p_getInt_0_);
        } catch (Throwable var3) {
            return p_getInt_1_;
        }
    }

    private static Map<String, Long> getLongScoreMap(Map<String, String> p_getScoreMap_0_) {
        Map<String, Long> lvt_1_1_ = Maps.newHashMap();

        for(String lvt_3_1_ : p_getScoreMap_0_.keySet()) {
            if (lvt_3_1_.startsWith("score_") && lvt_3_1_.length() > "score_".length()) {
                lvt_1_1_.put(lvt_3_1_.substring("score_".length()), getLong((String)p_getScoreMap_0_.get(lvt_3_1_), 1L));
            }
        }

        return lvt_1_1_;
    }

    @Inject(method = "getScorePredicates", at = @At(value = "HEAD"), cancellable = true)
    private static void getScorePredicates(ICommandSender p_getScorePredicates_0_, Map<String, String> p_getScorePredicates_1_, CallbackInfoReturnable<List<Predicate<Entity>>> cir) {
        final Map<String, Long> lvt_2_1_ = getLongScoreMap(p_getScorePredicates_1_);
        cir.setReturnValue ((List<Predicate<Entity>>)(lvt_2_1_.isEmpty() ? Collections.emptyList() : Lists.newArrayList(new Predicate[]{new Predicate<Entity>() {
            public boolean apply(@Nullable Entity p_apply_1_) {
                if (p_apply_1_ == null) {
                    return false;
                } else {
                    Scoreboard scoreboard = p_getScorePredicates_0_.getServer().getWorld(0).getScoreboard();

                    for(Map.Entry<String, Long> lvt_4_1_ : lvt_2_1_.entrySet()) {
                        String lvt_5_1_ = (String)lvt_4_1_.getKey();
                        boolean lvt_6_1_ = false;
                        if (lvt_5_1_.endsWith("_min") && lvt_5_1_.length() > 4) {
                            lvt_6_1_ = true;
                            lvt_5_1_ = lvt_5_1_.substring(0, lvt_5_1_.length() - 4);
                        }

                        ScoreObjective lvt_7_1_ = scoreboard.getObjective(lvt_5_1_);
                        if (lvt_7_1_ == null) {
                            return false;
                        }

                        String lvt_8_1_ = p_apply_1_ instanceof EntityPlayerMP ? p_apply_1_.getName() : p_apply_1_.getCachedUniqueIdString();
                        if (!scoreboard.entityHasObjective(lvt_8_1_, lvt_7_1_)) {
                            return false;
                        }

                        LongScore lvt_9_1_ = ((IScoreboard)scoreboard).getOrCreateScore(lvt_8_1_, lvt_7_1_);
                        long lvt_10_1_ = lvt_9_1_.getScorePoints();
                        if (lvt_10_1_ < lvt_4_1_.getValue() && lvt_6_1_) {
                            return false;
                        }

                        if (lvt_10_1_ > lvt_4_1_.getValue() && !lvt_6_1_) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }})));
    }
}

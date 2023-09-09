package si.bismuth.scoreboard.mixins;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketDisplayObjective;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import si.bismuth.MCServer;
import si.bismuth.network.BisPacketUpdateScore;
import si.bismuth.scoreboard.IScoreboard;
import si.bismuth.scoreboard.IServerScoreboard;
import si.bismuth.scoreboard.LongScore;

import java.util.List;
import java.util.Set;

@Mixin(ServerScoreboard.class)
public abstract class MixinServerScoreboard extends Scoreboard implements IServerScoreboard {
    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private Set<ScoreObjective> addedObjectives;

    @Shadow protected abstract void markSaveDataDirty();

    @Override
    public void onScoreUpdated(LongScore longScore) {
        if (this.addedObjectives.contains(longScore.getObjective())) {
            // TODO send the BisPacketUpdateScore to clients that support 64bit scoreboard
            Score score = new Score((Scoreboard) longScore.getScoreScoreboard(), longScore.getObjective(), longScore.getPlayerName());
            score.setScorePoints((int)longScore.getScorePoints());
            this.server.getPlayerList().sendPacketToAllPlayers(new SPacketUpdateScore(score));

            // TODO Change this to only send to subscribed players in a better way
            for (EntityPlayerMP player : this.server.getPlayerList().getPlayers()) {
                MCServer.pcm.sendPacketToPlayer(player, new BisPacketUpdateScore(longScore));
            }
        }
        this.markSaveDataDirty();
    }

    /**
     * @author agressive in making 64 bit scoreboards work, greets dragonbabyfly
     */
    @Overwrite
    public List<Packet<?>> getCreatePackets(ScoreObjective p_getCreatePackets_1_) {
        List<Packet<?>> lvt_2_1_ = Lists.newArrayList();
        lvt_2_1_.add(new SPacketScoreboardObjective(p_getCreatePackets_1_, 0));

        for(int lvt_3_1_ = 0; lvt_3_1_ < 19; ++lvt_3_1_) {
            if (this.getObjectiveInDisplaySlot(lvt_3_1_) == p_getCreatePackets_1_) {
                lvt_2_1_.add(new SPacketDisplayObjective(lvt_3_1_, p_getCreatePackets_1_));
            }
        }

        IScoreboard scoreboard = (IScoreboard) this;
        for (LongScore longScore : scoreboard.getSortedScores(p_getCreatePackets_1_)) {
            // TODO Change this to only send to subscribed players in a better way
            for (EntityPlayerMP player : this.server.getPlayerList().getPlayers()) {
                System.out.println("test");
                MCServer.pcm.sendPacketToPlayer(player, new BisPacketUpdateScore(longScore));
            }
            Score score = new Score((Scoreboard) longScore.getScoreScoreboard(), longScore.getObjective(), longScore.getPlayerName());
            score.setScorePoints((int)longScore.getScorePoints());
            lvt_2_1_.add(new SPacketUpdateScore(score));
        }

        return lvt_2_1_;
    }
}

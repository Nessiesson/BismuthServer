package si.bismuth.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import si.bismuth.scoreboard.LongScore;

import java.io.IOException;

@PacketChannelName("updateScore")
public class BisPacketUpdateScore extends BisPacket {
    private String name = "";
    private String objective = "";
    private long value;

    public BisPacketUpdateScore() {
    }

    public BisPacketUpdateScore(LongScore longScore) {
        this.name = longScore.getPlayerName();
        this.objective = longScore.getObjective().getName();
        this.value = longScore.getScorePoints();
    }

    @Override
    public void writePacketData() {
        final PacketBuffer buf = this.getPacketBuffer();
        buf.writeString(this.name);
        buf.writeString(this.objective);
        buf.writeVarLong(this.value);

    }

    @Override
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.name = buf.readString(40);
        this.objective = buf.readString(16);
        this.value = buf.readVarLong();
    }

    @Override
    public void processPacket(EntityPlayerMP player) {

    }
}

package net.glowstone.net.codec.play.player;

import com.flowpowered.network.Codec;
import com.flowpowered.network.CodecContext;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import net.glowstone.net.message.play.player.ServerDifficultyMessage;
import org.bukkit.Difficulty;

public final class ServerDifficultyCodec implements Codec<ServerDifficultyMessage> {

    @Override
    public ServerDifficultyMessage decode(CodecContext codecContext, ByteBuf buffer) throws IOException {
        int difficulty = buffer.readUnsignedByte();
        return new ServerDifficultyMessage(Difficulty.values()[difficulty]);
    }

    @Override
    public ByteBuf encode(CodecContext codecContext, ByteBuf buf, ServerDifficultyMessage message) throws IOException {
        return buf.writeByte(message.getDifficulty().ordinal());
    }
}

package io.github.gaming32.worldhost.client.ws;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;

import javax.websocket.EndpointConfig;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.UUID;

// Mirrors https://github.com/Gaming32/world-host-server/blob/main/src/c2s_message.rs
public sealed interface WorldHostC2SMessage {
    record ListOnline(Collection<UUID> friends) implements WorldHostC2SMessage {
        @Override
        public void encode(DataOutputStream dos) throws IOException {
            dos.writeByte(0);
            dos.writeInt(friends.size());
            for (final UUID friend : friends) {
                writeUuid(dos, friend);
            }
        }
    }

    record FriendRequest(UUID toUser) implements WorldHostC2SMessage {
        @Override
        public void encode(DataOutputStream dos) throws IOException {
            dos.writeByte(1);
            writeUuid(dos, toUser);
        }
    }

    record PublishedWorld(Collection<UUID> friends) implements WorldHostC2SMessage {
        @Override
        public void encode(DataOutputStream dos) throws IOException {
            dos.writeByte(2);
            dos.writeInt(friends.size());
            for (final UUID friend : friends) {
                writeUuid(dos, friend);
            }
        }
    }

    record ClosedWorld(Collection<UUID> friends) implements WorldHostC2SMessage {
        @Override
        public void encode(DataOutputStream dos) throws IOException {
            dos.writeByte(3);
            dos.writeInt(friends.size());
            for (final UUID friend : friends) {
                writeUuid(dos, friend);
            }
        }
    }

    record RequestJoin(UUID friend) implements WorldHostC2SMessage {
        @Override
        public void encode(DataOutputStream dos) throws IOException {
            dos.writeByte(4);
            writeUuid(dos, friend);
        }
    }

    record JoinGranted(UUID connectionId, JoinType joinType) implements WorldHostC2SMessage {
        @Override
        public void encode(DataOutputStream dos) throws IOException {
            dos.writeByte(5);
            writeUuid(dos, connectionId);
            joinType.encode(dos);
        }
    }

    record QueryRequest(UUID friend) implements WorldHostC2SMessage {
        @Override
        public void encode(DataOutputStream dos) throws IOException {
            dos.writeByte(6);
            writeUuid(dos, friend);
        }
    }

    record QueryResponse(UUID connectionId, ServerMetadata metadata) implements WorldHostC2SMessage {
        @Override
        public void encode(DataOutputStream dos) throws IOException {
            dos.writeByte(7);
            writeUuid(dos, connectionId);
            final PacketByteBuf buf = PacketByteBufs.create();
            new QueryResponseS2CPacket(metadata).write(buf);
            dos.writeInt(buf.readableBytes());
            buf.readBytes(dos, buf.readableBytes());
        }
    }

    void encode(DataOutputStream dos) throws IOException;

    static void writeUuid(DataOutputStream dos, UUID uuid) throws IOException {
        dos.writeLong(uuid.getMostSignificantBits());
        dos.writeLong(uuid.getLeastSignificantBits());
    }

    class Encoder implements javax.websocket.Encoder.BinaryStream<WorldHostC2SMessage> {
        @Override
        public void encode(WorldHostC2SMessage object, OutputStream os) throws IOException {
            object.encode(new DataOutputStream(os));
        }

        @Override
        public void init(EndpointConfig config) {
        }

        @Override
        public void destroy() {
        }
    }
}

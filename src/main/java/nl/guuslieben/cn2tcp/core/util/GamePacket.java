package nl.guuslieben.cn2tcp.core.util;

import java.util.UUID;

import nl.guuslieben.cn2tcp.core.game.Lobby;

public class GamePacket {

    private Lobby lobby;
    private String message;
    private UUID uuid;

    public GamePacket(Lobby lobby, String message, UUID uuid) {
        this.lobby = lobby;
        this.message = message;
        this.uuid = uuid;
    }

    public Lobby getLobby() {
        return lobby;
    }

    public String getMessage() {
        return message;
    }

    public UUID getUuid() {
        return uuid;
    }
}

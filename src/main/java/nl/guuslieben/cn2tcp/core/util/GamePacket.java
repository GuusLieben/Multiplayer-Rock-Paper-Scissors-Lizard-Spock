package nl.guuslieben.cn2tcp.core.util;

import java.util.UUID;

import nl.guuslieben.cn2tcp.core.game.Lobby;

public class GamePacket {

    private Lobby lobby;
    private String message;
    private UUID uuid;
    private String name;

    public GamePacket(Lobby lobby, String message, UUID uuid, String name) {
        this.lobby = lobby;
        this.message = message;
        this.uuid = uuid;
        this.name = name;
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

    public String getName() {
        return name;
    }
}

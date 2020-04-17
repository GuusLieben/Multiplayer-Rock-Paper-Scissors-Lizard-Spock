package nl.guuslieben.cn2tcp.core.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import nl.guuslieben.cn2tcp.core.util.Player;

public class LobbyManager {

    private static final Map<UUID, Lobby> playersIn = new HashMap<>();
    private static final Map<UUID, Player> players = new HashMap<>();
    private static final List<Lobby> lobbies = new ArrayList<>();

    static void addPlayerToLobby(Player player, Lobby lobby) {
        playersIn.remove(player.getUuid());
        players.put(player.getUuid(), player);
        playersIn.put(player.getUuid(), lobby);
    }

    public static Lobby getLobby(String alias) {
        return lobbies.parallelStream().filter(lobby -> lobby.getAlias().equals(alias))
                .findAny().orElseGet(() -> {
                    Lobby newLobby = new Lobby(alias);
                    lobbies.add(newLobby);
                    return newLobby;
                });
    }

    public static Lobby getLobbyForPlayer(Player player) {
        return playersIn.getOrDefault(player.getUuid(), null);
    }

    public static List<Lobby> getLobbies() {
        return lobbies;
    }

    public static int totalPlayerCount() {
        return playersIn.size();
    }

    public static Optional<Player> getPlayer(UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    public static Collection<Player> getPlayersInLobby(Lobby lobby) {
        List<Player> players = new ArrayList<>();
        for (Entry<UUID, Lobby> entry : playersIn.entrySet()) {
            UUID uuid = entry.getKey();
            Lobby lob = entry.getValue();
            if (lob.getAlias().equals(lobby.getAlias())) getPlayer(uuid).ifPresent(players::add);
        }
        return players;
    }
}

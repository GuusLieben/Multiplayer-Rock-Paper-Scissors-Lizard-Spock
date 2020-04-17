package nl.guuslieben.cn2tcp.core.game;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import nl.guuslieben.cn2tcp.core.enums.GameState;
import nl.guuslieben.cn2tcp.core.enums.Move;
import nl.guuslieben.cn2tcp.core.enums.Result;
import nl.guuslieben.cn2tcp.core.util.Player;
import nl.guuslieben.cn2tcp.core.util.Tuple;

public class Lobby {

    protected final Map<UUID, Move> moves = new HashMap<>();
    private GameState gameState = GameState.WAITING;
    private final String alias;
    private String[] winners = null;

    Lobby(String alias) {
        this.alias = alias;
    }

    public void joinPlayer(Player player) {
        LobbyManager.addPlayerToLobby(player, this);
    }

    public GameState getGameState() {
        return this.gameState;
    }

    public Collection<Player> getPlayers() {
        return LobbyManager.getPlayersInLobby(this);
    }

    public String getAlias() {
        return alias;
    }

    public String[] getWinners() {
        return winners;
    }

    public int getMoves() {
        return this.moves.size();
    }

    @SuppressWarnings("unchecked")
    public Result move(Player player, Move move) {
        // Play the move and directly check if we have a winner/tie
        moves.put(player.getUuid(), move);
        var result = this.getResult();

        // Indicates there are still players who have not submitted a move, do not tell the client anything but their own move and how many players are left
        if (result.getK().equals(Result.TIE)) {
            var players = (Collection<Player>) result.getV();
            this.winners = players.stream().map(Player::getName).collect(Collectors.toList()).toArray(new String[]{});

            // Indicates we have a single winner
        } else if (result.getK().equals(Result.WINNER)) {
            this.winners = new String[]{((Player) result.getV()).getName()};
        }
        return result.getK();
    }

    // TODO : Make this actually be useful
    public Tuple<Result, Object> getResult() {
        int playersLeft = LobbyManager.getPlayersInLobby(this).size() - moves.size();
        if (playersLeft > 0) {
            this.gameState = GameState.WAITING;
            return new Tuple<>(Result.WAITING, playersLeft);
        }

        this.gameState = GameState.ENDED;
        return new Tuple<>(Result.WINNER, getPlayers().toArray()[0]);

    }

}

package nl.guuslieben.cn2tcp.core.game;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import nl.guuslieben.cn2tcp.CN2_Client;
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
    private Tuple<Move, Player[]> winningCandidates = null;

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

    public Result move(Player player, Move move) {
        // Play the move and directly check if we have a winner/tie
        var result = this.checkResult(move, player);

        // Indicates there are still players who have not submitted a move, do not tell the client anything but their own move and how many players are left
        if (result.getK().equals(Result.TIE)) {
            var players = (Player[]) result.getV();
            this.winners = Arrays.stream(players).map(Player::getName).collect(Collectors.toList()).toArray(new String[]{});

            // Indicates we have a single winner
        } else if (result.getK().equals(Result.WINNER)) {
            this.winners = new String[]{((Player) result.getV()).getName()};
        }
        return result.getK();
    }

    public Tuple<Result, Object> checkResult(Move move, Player player) {
        if (winningCandidates == null || winningCandidates.getK().losesTo(move)) winningCandidates = new Tuple<>(move, new Player[]{player});
        else if (winningCandidates.getK() == move) {
            Player[] candidates = new Player[winningCandidates.getV().length+1];
            candidates[winningCandidates.getV().length] = player;
            winningCandidates = new Tuple<>(move, candidates);
        }

        int playersLeft = LobbyManager.getPlayersInLobby(this).size() - moves.size();
        if (playersLeft > 0) {
            this.gameState = GameState.WAITING;
            return new Tuple<>(Result.WAITING, playersLeft);
        }

        if (winningCandidates.getV().length > 1) return new Tuple<>(Result.TIE, winningCandidates.getV());
        else return new Tuple<>(Result.WINNER, winningCandidates.getV()[0]);
    }

    public void broadcast(String message) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        for (Player player : LobbyManager.getPlayersInLobby(this)) {
            var buf = message.getBytes();
            var packet = new DatagramPacket(buf, buf.length, player.getAddress(), CN2_Client.CLIENT_PORT);
            socket.send(packet);
        }
    }

}

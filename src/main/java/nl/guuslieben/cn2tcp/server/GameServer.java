package nl.guuslieben.cn2tcp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.UUID;

import nl.guuslieben.cn2tcp.CN2;
import nl.guuslieben.cn2tcp.core.enums.GameState;
import nl.guuslieben.cn2tcp.core.enums.Move;
import nl.guuslieben.cn2tcp.core.enums.Result;
import nl.guuslieben.cn2tcp.core.game.Lobby;
import nl.guuslieben.cn2tcp.core.game.LobbyManager;
import nl.guuslieben.cn2tcp.core.util.GamePacket;
import nl.guuslieben.cn2tcp.core.util.Player;
import nl.guuslieben.cn2tcp.core.util.net.NetworkUtil;

public class GameServer extends Thread {

    public static boolean running = false;
    private final DatagramSocket socket;
    private final Logger logger;

    public GameServer() throws SocketException {
        socket = new DatagramSocket(CN2.SERVER_PORT);
        logger = LoggerFactory.getLogger("Server");
        running = true;
    }

    @Override
    public void run() {
        logger.warn("Server opening socket.");

        while (true) {
            try {
                // Receive packet buffer
                var buf = new byte[8192];
                var packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // Collect return address
                var address = packet.getAddress();
                int port = packet.getPort();

                // Fill packet with newly received buffer and convert to String
                packet = new DatagramPacket(buf, buf.length, address, port);
                var gamePacket = decodePacket(packet.getData(), address.getAddress());
                var received = gamePacket.getMessage();

                // Construct a player from the return address
                UUID uuid = gamePacket.getUuid();
                var playerCandidate = LobbyManager.getPlayer(uuid);
                var player = playerCandidate.orElseGet(() -> new Player(uuid, LobbyManager.totalPlayerCount() + 1));

                // Make sure the player is registered to their active lobby according to both the client and server
                var lobby = gamePacket.getLobby();
                var checkedLobby = LobbyManager.getLobbyForPlayer(player);
                if (lobby == null && checkedLobby != null) lobby = checkedLobby;
                if (checkedLobby != null && !checkedLobby.equals(lobby)) lobby.joinPlayer(player);

                if (lobby != null) {
                    if (received.startsWith("!reg")) handleRegistration(received, player, address, port, lobby);
                    else if (received.equals("!forcequit")) break;
                    else if (received.equals("!debug")) handleDebug(player, address, port);
                    else if (received.equals("!status")) handleStatus(address, port, lobby);
                    else handleMove(received, player, address, port, lobby);
                } else send("Unknown lobby", address, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        logger.warn("Server closing socket.");
        socket.close();
    }

    private void handleStatus(InetAddress address, int port, Lobby lobby) {
        String message = "Lobby '" + lobby.getAlias() + "' is currently " + lobby.getGameState();
        if (lobby.getGameState() == GameState.ENDED) {
            if (lobby.getWinners().length > 1)
                message += String.format(". The winners are %s", String.join(", ", lobby.getWinners()));
            else message += String.format(". The winner is %s", lobby.getWinners()[0]);
        }
        send(message, address, port);
    }

    private void handleMove(String received, Player player, InetAddress address, int port, Lobby lobby) {
        try {
            // Can throw an exception, either null or illegal argument. If thrown will return unknown command status
            var move = Enum.valueOf(Move.class, received.toUpperCase());
            Result result = lobby.move(player, move);
            String response;
            switch (result) {
                case WINNER:
                    response = String.format("Received move '%s'. We have a winner! %s", move, lobby.getWinners()[0]);
                    break;
                case WAITING:
                    response = String.format("Received move '%s'. Waiting for %d players to play.", move, (lobby.getPlayers().size() - lobby.getMoves()));
                    break;
                case TIE:
                    response = String.format("Received move '%s'. Tied! %s", move, String.join(", ", lobby.getWinners()));
                    break;
                default:
                    response = "Failed to get a response";
                    break;
            }
            ;
            send(response, address, port);
        } catch (Exception e) {
            send(String.format("Unknown command '%s'", received), address, port);
        }
    }

    private void handleDebug(Player player, InetAddress address, int port) {
        var user = player.getName();
        var lobbies = LobbyManager.getLobbies();
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("You are playing as : %s%n", user));
        builder.append(String.format("There are a total of %d users logged in across %d lobbies%n", LobbyManager.totalPlayerCount(), lobbies.size()));
        lobbies.forEach(lobby -> {
            builder.append(String.format("Lobby '%s' has %d players logged in. Its state is : %s%n", lobby.getAlias(), lobby.getPlayers().size(), lobby.getGameState().toString()));
            if (lobby.getPlayers().size() > 0) {
                builder.append("Logged in players : \n");
                lobby.getPlayers().forEach(p -> builder.append(String.format(" - %s (%s)%n", p.getName(), p.getUuid())));
            }
        });

        send(builder.toString(), address, port);
    }

    private void handleRegistration(String received, Player player, InetAddress address, int port, Lobby lobby) {
        var parts = received.split(" ");
        if (parts.length > 1)
            player = new Player(UUID.nameUUIDFromBytes(parts[1].getBytes()), lobby.getPlayers().size() + 1);
        lobby.joinPlayer(player);
        send(String.format("You are now playing as %s in lobby %s", player.getName(), lobby.getAlias()), address, port);
    }

    private void send(String msg, InetAddress address, int port) {
        send(new DatagramPacket(msg.getBytes(), msg.getBytes().length, address, port));
    }

    private void send(DatagramPacket packet) {
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private GamePacket decodePacket(byte[] data, byte[] alternativeId) {
        var dataString = NetworkUtil.convertBytes(data);
        var dataParts = dataString.split("\\|");
        var uuid = UUID.nameUUIDFromBytes(alternativeId);
        if (dataParts.length > 1) {
            var lobby = LobbyManager.getLobby(dataParts[0]);
            var message = dataParts[1];
            if (dataParts.length > 2) uuid = UUID.nameUUIDFromBytes(dataParts[2].getBytes());
            return new GamePacket(lobby, message, uuid);
        } else {
            return new GamePacket(null, dataString, uuid);
        }
    }
}

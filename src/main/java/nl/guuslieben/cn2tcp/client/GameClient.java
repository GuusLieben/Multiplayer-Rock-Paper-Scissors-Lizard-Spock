package nl.guuslieben.cn2tcp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

import nl.guuslieben.cn2tcp.CN2_Client;
import nl.guuslieben.cn2tcp.CN2_Server;
import nl.guuslieben.cn2tcp.core.util.net.NetworkUtil;

public class GameClient {

    private final DatagramSocket socket;
    private final InetAddress address;
    private final Logger logger;
    private static boolean isActive = true;

    public GameClient() throws IOException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        logger = LoggerFactory.getLogger("Client");
        logger.info("Registering to server\n:: -> " + sendEcho("default|!reg"));
        new GameClientBroadcastListener(logger).start();
        this.play();
    }

    public void play() {
        var scanner = new Scanner(System.in);
        var running = true;
        while (running) {
            try {
                logger.debug(">> Waiting for user\r");
                var move = scanner.nextLine();
                var response = sendEcho(move);
                logger.info(response);
                if (move.equals("!forcequit")) {
                    running = false;
                    close();
                    break;
                }

            } catch (IOException | IllegalArgumentException | NullPointerException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public String sendEcho(String msg) throws IOException {
        var buf = msg.getBytes();
        var packet = new DatagramPacket(buf, buf.length, address, CN2_Server.SERVER_PORT);
        socket.send(packet);

        var receiveBuf = new byte[4096];
        packet = new DatagramPacket(receiveBuf, receiveBuf.length);

        socket.receive(packet);
        return NetworkUtil.convertBytes(packet.getData());
    }

    public void close() throws IOException {
        sendEcho("!forcequit");
        socket.close();
        isActive = false;
    }

    private static class GameClientBroadcastListener extends Thread {

        private final DatagramSocket socket;
        private final Logger logger;

        public GameClientBroadcastListener(Logger logger) throws SocketException {
            this.socket = new DatagramSocket(CN2_Client.CLIENT_PORT);
            this.logger = logger;
        }

        @Override
        public void run() {
            while (GameClient.isActive) {
                try {
                    var buf = new byte[4096];
                    var packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);

                    var address = packet.getAddress();
                    int port = packet.getPort();

                    // Fill packet with newly received buffer and convert to String
                    packet = new DatagramPacket(buf, buf.length, address, port);
                    String message = NetworkUtil.convertBytes(packet.getData());

                    logger.info(String.format("Received broadcast : %s", message));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

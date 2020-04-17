package nl.guuslieben.cn2tcp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import nl.guuslieben.cn2tcp.CN2;
import nl.guuslieben.cn2tcp.core.util.net.NetworkUtil;

public class GameClient {

    private DatagramSocket socket;
    private final InetAddress address;
    private final Logger logger;

    public GameClient() throws IOException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        logger = LoggerFactory.getLogger("Client");
        logger.info("Registering to server\n:: -> " + sendEcho("default|!reg"));

        this.play();
    }

    public void play() {
        var scanner = new Scanner(System.in);
        var running = true;
        while (running) {
            try {
                logger.info(">> Waiting for user\r");
                var move = scanner.nextLine();
                var response = sendEcho(move);
                logger.info(response);
                if (move.equals("!forcequit")) {
                    running = false;
                    close();
                }

            } catch (IOException | IllegalArgumentException | NullPointerException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public String sendEcho(String msg) throws IOException {
        var buf = msg.getBytes();
        var packet = new DatagramPacket(buf, buf.length, address, CN2.SERVER_PORT);
        socket.send(packet);

        var receiveBuf = new byte[4096];
        packet = new DatagramPacket(receiveBuf, receiveBuf.length);

        socket.receive(packet);
        return NetworkUtil.convertBytes(packet.getData());
    }

    public String receive() throws IOException {
        String result;
        do {
            result = sendEcho("!tick");
        } while (result == null || result.equals(""));

        logger.info("Got result : " + result);
        return result;
    }

    public void close() throws IOException {
        sendEcho("!forcequit");
        socket.close();
    }

}

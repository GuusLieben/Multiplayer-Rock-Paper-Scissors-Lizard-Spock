package nl.guuslieben.cn2tcp;

import java.io.IOException;

import nl.guuslieben.cn2tcp.server.GameServer;

public class CN2_Server {

    public static final Integer SERVER_PORT = 4445;

    public static void main(String[] args) throws IOException {
        if (!GameServer.running) new GameServer().start();
    }

}

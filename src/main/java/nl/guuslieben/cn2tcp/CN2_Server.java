package nl.guuslieben.cn2tcp;

import java.io.IOException;

import nl.guuslieben.cn2tcp.server.GameServer;

public class CN2_Server {

    public static final Integer SERVER_PORT = 8111;

    public static void start() throws IOException {
        if (!GameServer.running) new GameServer().start();
    }

}

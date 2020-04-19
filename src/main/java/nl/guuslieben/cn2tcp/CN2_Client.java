package nl.guuslieben.cn2tcp;

import java.io.IOException;

import nl.guuslieben.cn2tcp.client.GameClient;

public class CN2_Client {

    public static final int CLIENT_PORT = 4449;

    public static void start() throws IOException {
        new GameClient();
    }

}

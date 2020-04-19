package nl.guuslieben.cn2tcp;

import java.io.IOException;

public class CN2_Main {

    public static void main(String[] args) throws IOException {
        switch (args[0]) {
            case "client" -> CN2_Client.start();
            case "server" -> CN2_Server.start();
        }
    }

}

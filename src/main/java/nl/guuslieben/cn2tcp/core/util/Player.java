package nl.guuslieben.cn2tcp.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.UUID;

import nl.guuslieben.cn2tcp.core.util.net.NetworkUtil;

public class Player {

    private UUID uuid;
    private String name;
    Logger logger = LoggerFactory.getLogger("Player");
    private InetAddress address;

    public Player(UUID uuid, int number, InetAddress address) {
        this.uuid = uuid;
        this.name = NetworkUtil.getRandomName(number);
        this.address = address;
        logger.info("New player created with name " + name + " and uuid " + uuid.toString());
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Player setName(String alias) {
        this.name = alias;
        return this;
    }

    public InetAddress getAddress() {
        return address;
    }
}

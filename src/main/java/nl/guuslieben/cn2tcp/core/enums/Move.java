package nl.guuslieben.cn2tcp.core.enums;

import java.util.Arrays;
import java.util.List;

public enum Move {
    ROCK, PAPER, SCISSORS, LIZARD, SPOCK;

    public List<Move> losesTo;

    public boolean losesTo(Move other) {
        return losesTo.contains(other);
    }

    static {
        SCISSORS.losesTo = Arrays.asList(ROCK, SPOCK);
        ROCK.losesTo = Arrays.asList(PAPER, SPOCK);
        PAPER.losesTo = Arrays.asList(SCISSORS, LIZARD);
        SPOCK.losesTo = Arrays.asList(PAPER, LIZARD);
        LIZARD.losesTo = Arrays.asList(SCISSORS, ROCK);
    }

}

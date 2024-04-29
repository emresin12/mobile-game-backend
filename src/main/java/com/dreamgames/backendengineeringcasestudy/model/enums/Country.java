package com.dreamgames.backendengineeringcasestudy.model.enums;

import java.util.concurrent.ThreadLocalRandom;

public enum Country {
    FRANCE,
    GERMANY,
    TURKEY,
    UK,
    USA;

    public static Country getRandomCountry() {
        return values()[ThreadLocalRandom.current().nextInt(values().length)];
    }

}

package com.inno.innochat.common.constant;


import java.util.HashMap;
import java.util.Map;


/**
 * 支援賽事
 */
public enum SportIdEnum {
    FOOTBALL(1),
    BASKETBALL(2),
    TENNIS(3),
    BASEBALL(4),
    ESPORTS(23);
    private final int value;

    SportIdEnum(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }

    private static Map<Integer, SportIdEnum> valueMapper = new HashMap<>();

    static {
        for (SportIdEnum sportsId : SportIdEnum.values()) {
            valueMapper.put(sportsId.value, sportsId);
        }
    }

    public static SportIdEnum getSportIdByValue(int value) {
        SportIdEnum result = valueMapper.get(value);
        return result;
    }

}

package com.keecker.embedded.stm.interfaces;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016 KEECKER SAS (www.keecker.com)
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * Contributors: Cyril Lugan
 */

public class MovementSafeties {
    public static final int NB_OF_PROXIMITY_SENSORS = 12;

    public static final int PROXIMITY_LEFT_0_ID = 0;
    public static final int PROXIMITY_LEFT_1_ID = 1;
    public static final int PROXIMITY_LEFT_2_ID = 2;
    public static final int PROXIMITY_REAR_ID   = 3;
    public static final int CLIFF_REAR_RIGHT_ID = 4;
    public static final int CLIFF_REAR_LEFT_ID = 5;
    public static final int PROXIMITY_RIGHT_0_ID = 6;
    public static final int PROXIMITY_RIGHT_1_ID = 7;
    public static final int PROXIMITY_RIGHT_2_ID = 8;
    public static final int CLIFF_FRONT_MIDDLE_ID = 9;
    public static final int CLIFF_FRONT_LEFT_ID = 10;
    public static final int CLIFF_FRONT_RIGHT_ID = 11;
    public static final int MOTOR_CURRENT_LEFT_ID = 12;
    public static final int MOTOR_CURRENT_RIGHT_ID = 13;
    public static final int SHOCK_ID = 14;
    public static final int ANGULAR_INCONSISTENCY_ID = 15;

    public static final int PROXIMITY_LEFT_0 = 1<<PROXIMITY_LEFT_0_ID;
    public static final int PROXIMITY_LEFT_1 = 1 << PROXIMITY_LEFT_1_ID;
    public static final int PROXIMITY_LEFT_2 = 1 << PROXIMITY_LEFT_2_ID;
    public static final int PROXIMITY_REAR = 1 << PROXIMITY_REAR_ID;
    public static final int CLIFF_REAR_RIGHT = 1 << CLIFF_REAR_RIGHT_ID;
    public static final int CLIFF_REAR_LEFT = 1 << CLIFF_REAR_LEFT_ID;
    public static final int PROXIMITY_RIGHT_0 = 1 << PROXIMITY_RIGHT_0_ID;
    public static final int PROXIMITY_RIGHT_1 = 1 << PROXIMITY_RIGHT_1_ID;
    public static final int PROXIMITY_RIGHT_2 = 1 << PROXIMITY_RIGHT_2_ID;
    public static final int CLIFF_FRONT_MIDDLE = 1 << CLIFF_FRONT_MIDDLE_ID;
    public static final int CLIFF_FRONT_LEFT = 1 << CLIFF_FRONT_LEFT_ID;
    public static final int CLIFF_FRONT_RIGHT = 1 << CLIFF_FRONT_RIGHT_ID;
    public static final int MOTOR_CURRENT_LEFT = 1 << MOTOR_CURRENT_LEFT_ID;
    public static final int MOTOR_CURRENT_RIGHT = 1 << MOTOR_CURRENT_RIGHT_ID;
    public static final int SHOCK = 1 << SHOCK_ID;
    public static final int ANGULAR_INCONSISTENCY = 1 << ANGULAR_INCONSISTENCY_ID;
    public static final int ALL = 0xffff;

    public static List<String> toStrings(int mask) {
        List<String> list = new ArrayList<>();
        if ((mask & PROXIMITY_LEFT_0) != 0) { list.add("PROXIMITY_LEFT_0"); }
        if ((mask & PROXIMITY_LEFT_1) != 0) { list.add("PROXIMITY_LEFT_1"); }
        if ((mask & PROXIMITY_LEFT_2) != 0) { list.add("PROXIMITY_LEFT_2"); }
        if ((mask & PROXIMITY_RIGHT_0) != 0) { list.add("PROXIMITY_RIGHT_0"); }
        if ((mask & PROXIMITY_RIGHT_1) != 0) { list.add("PROXIMITY_RIGHT_1"); }
        if ((mask & PROXIMITY_RIGHT_2) != 0) { list.add("PROXIMITY_RIGHT_2"); }
        if ((mask & PROXIMITY_REAR) != 0) { list.add("PROXIMITY_REAR"); }
        if ((mask & CLIFF_FRONT_MIDDLE) != 0) { list.add("CLIFF_FRONT_MIDDLE"); }
        if ((mask & CLIFF_FRONT_LEFT) != 0) { list.add("CLIFF_FRONT_LEFT"); }
        if ((mask & CLIFF_FRONT_RIGHT) != 0) { list.add("CLIFF_FRONT_RIGHT"); }
        if ((mask & CLIFF_REAR_LEFT) != 0) { list.add("CLIFF_REAR_LEFT"); }
        if ((mask & CLIFF_REAR_RIGHT) != 0) { list.add("CLIFF_REAR_RIGHT"); }
        if ((mask & MOTOR_CURRENT_LEFT) != 0) { list.add("MOTOR_CURRENT_LEFT"); }
        if ((mask & MOTOR_CURRENT_RIGHT) != 0) { list.add("MOTOR_CURRENT_RIGHT"); }
        if ((mask & SHOCK) != 0) { list.add("SHOCK"); }
        if ((mask & ANGULAR_INCONSISTENCY) != 0) { list.add("ANGULAR_INCONSISTENCY"); }
        return list;
    }
}

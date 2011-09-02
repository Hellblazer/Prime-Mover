package com.hellblazer.primeMover.soot;

import com.hellblazer.primeMover.Kronos;

public class ApiUserImpl implements ApiUser {

    @Override
    public void blockingSleep() {
        Kronos.blockingSleep(1000L);
    }
}

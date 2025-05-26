package testClasses;

import com.hellblazer.primeMover.api.Kronos;

public class ApiUserImpl implements ApiUser {

    @Override
    public void blockingSleep() {
        Kronos.blockingSleep(1000L);
    }
}

package testClasses;

import java.util.logging.Logger;

import com.hellblazer.primeMover.runtime.EntityReference;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;

class MockController extends Devi {

    @Override
    public void advance(long duration) {
    }

    @Override
    public long getCurrentTime() {
        return -1;
    }

    @Override
    public boolean isDebugEvents() {
        return false;
    }

    @Override
    public boolean isTrackEventSources() {
        return false;
    }

    @Override
    public void post(EventImpl event) {
    }

    @Override
    public Object postContinuingEvent(EntityReference entity, int event, Object... arguments) throws Throwable {
        return null;
    }

    @Override
    public void postEvent(EntityReference entity, int event, Object... arguments) {
    }

    @Override
    public void postEvent(long time, EntityReference entity, int event, Object... arguments) {
    }

    @Override
    public void setCurrentTime(long time) {
    }

    @Override
    public void setDebugEvents(boolean debug) {
    }

    @Override
    public void setEventLogger(Logger eventLog) {
    }

    @Override
    public void setTrackEventSources(boolean track) {
    }

    @Override
    public EventImpl swapCaller(EventImpl caller) {
        return null;
    }

}

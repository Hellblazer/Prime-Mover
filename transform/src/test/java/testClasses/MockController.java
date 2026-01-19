package testClasses;

import com.hellblazer.primeMover.api.EntityReference;
import com.hellblazer.primeMover.runtime.Devi;
import com.hellblazer.primeMover.runtime.EventImpl;

import org.slf4j.Logger;

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

    @Override
    public int getTotalEvents() {
        return 0;
    }

    @Override
    public java.util.Map<String, Integer> getSpectrum() {
        return java.util.Map.of();
    }

    @Override
    public String getName() {
        return "MockController";
    }

    @Override
    public long getSimulationStart() {
        return 0;
    }

    @Override
    public long getSimulationEnd() {
        return 0;
    }

}

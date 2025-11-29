package com.hellblazer.primeMover.desmoj.report;

import com.hellblazer.primeMover.desmoj.QueueStatistics;
import java.util.LinkedHashMap;
import java.util.Map;

public class QueueReporter implements Reporter {
    private final String name;
    private final QueueStatistics stats;
    private final long currentTime;
    
    public QueueReporter(String name, QueueStatistics stats, long currentTime) {
        this.name = name;
        this.stats = stats;
        this.currentTime = currentTime;
    }
    
    @Override
    public String getName() { return name; }
    
    @Override
    public String getType() { return "Queue"; }
    
    @Override
    public Map<String, Object> getStatistics() {
        var map = new LinkedHashMap<String, Object>();
        map.put("currentLength", stats.getCurrentLength());
        map.put("maxLength", stats.getMaxLength());
        map.put("totalEntries", stats.getTotalEntries());
        map.put("totalExits", stats.getTotalExits());
        map.put("avgWaitTime", stats.getAvgWaitTime());
        map.put("maxWaitTime", stats.getMaxWaitTime());
        map.put("avgLength", stats.getAvgLength(currentTime));
        return map;
    }
}

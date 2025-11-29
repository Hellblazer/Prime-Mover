package com.hellblazer.primeMover.desmoj.report;

import com.hellblazer.primeMover.desmoj.ResourceStatistics;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceReporter implements Reporter {
    private final String name;
    private final ResourceStatistics stats;
    private final long currentTime;
    
    public ResourceReporter(String name, ResourceStatistics stats, long currentTime) {
        this.name = name;
        this.stats = stats;
        this.currentTime = currentTime;
    }
    
    @Override
    public String getName() { return name; }
    
    @Override
    public String getType() { return "Resource"; }
    
    @Override
    public Map<String, Object> getStatistics() {
        var map = new LinkedHashMap<String, Object>();
        map.put("capacity", stats.getCapacity());
        map.put("currentInUse", stats.getCurrentInUse());
        map.put("totalAcquisitions", stats.getTotalAcquisitions());
        map.put("avgWaitTime", stats.getAvgWaitTime());
        map.put("maxWaitTime", stats.getMaxWaitTime());
        map.put("utilization", stats.getUtilization(currentTime));
        return map;
    }
}

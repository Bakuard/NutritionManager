package com.bakuard.nutritionManager.config.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class HierarchyLoggerFilter extends Filter<ILoggingEvent> {

    private String parentLoggerName;

    public HierarchyLoggerFilter() {

    }

    @Override
    public FilterReply decide(ILoggingEvent logEvent) {
        if(logEvent.getLoggerName().startsWith(parentLoggerName)) {
            return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
    }

    public String getParentLoggerName() {
        return parentLoggerName;
    }

    public void setParentLoggerName(String parentLoggerName) {
        this.parentLoggerName = parentLoggerName;
    }

}

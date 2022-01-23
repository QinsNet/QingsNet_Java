package com.ethereal.net.core.base;

import com.ethereal.net.core.base.event.ExceptionEvent;
import com.ethereal.net.core.base.event.LogEvent;
import com.ethereal.net.core.entity.TrackException;
import com.ethereal.net.core.entity.TrackLog;
import lombok.Getter;

public class BaseCore {
    @Getter
    private final ExceptionEvent exceptionEvent = new ExceptionEvent();
    @Getter
    private final LogEvent logEvent = new LogEvent();

    public void onException(TrackException.ErrorCode code, String message) {
        onException(new TrackException(code,message,this));
    }

    public void onException(Exception exception)  {
        exceptionEvent.onEvent(exception);
    }

    public void onLog(TrackLog.LogCode code, String message){
        onLog(new TrackLog(code,message,this));
    }

    public void onLog(TrackLog log){
        log.setSender(this);
        logEvent.onEvent(log);
    }

}

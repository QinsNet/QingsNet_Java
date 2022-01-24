package com.ethereal.meta.meta.event.delegate;

import com.ethereal.meta.core.entity.TrackLog;

public interface LogEventDelegate {
    void onLog(TrackLog log);
}

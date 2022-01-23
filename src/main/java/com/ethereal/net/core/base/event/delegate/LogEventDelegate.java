package com.ethereal.net.core.base.event.delegate;

import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.core.entity.TrackLog;

public interface LogEventDelegate {
    void onLog(TrackLog log);
}

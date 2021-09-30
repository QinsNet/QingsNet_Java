package com.ethereal.server.Core.Event.Delegate;

import com.ethereal.server.Core.Model.TrackLog;

public interface LogEventDelegate {
    void onLog(TrackLog log);
}

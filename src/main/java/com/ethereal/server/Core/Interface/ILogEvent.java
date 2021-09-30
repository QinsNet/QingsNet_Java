package com.ethereal.server.Core.Interface;

import com.ethereal.server.Core.Model.TrackLog;

public interface ILogEvent {
    void onLog(TrackLog log);
    void onLog(TrackLog.LogCode code, String message);
}

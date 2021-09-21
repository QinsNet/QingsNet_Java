package Core.Interface;

import Core.Model.TrackLog;

public interface ILogEvent {
    public void OnLog(TrackLog.LogCode code, String message);
    public void OnLog(TrackLog log);
}

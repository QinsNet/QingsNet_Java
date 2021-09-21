package Core.Interface;

import Core.Model.TrackException;

public interface IExceptionEvent {
    public void OnException(TrackException.ErrorCode code,String message);
    public void OnException(TrackException e);
}

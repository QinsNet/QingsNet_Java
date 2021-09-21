package Core.Event.Delegate;

import Core.Model.TrackException;

public interface ExceptionEventDelegate {
    void OnException(TrackException exception);

}

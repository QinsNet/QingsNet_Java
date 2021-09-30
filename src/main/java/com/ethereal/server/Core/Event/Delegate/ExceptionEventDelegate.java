package com.ethereal.server.Core.Event.Delegate;

import com.ethereal.server.Core.Model.TrackException;

public interface ExceptionEventDelegate {
    void onException(TrackException exception);
}

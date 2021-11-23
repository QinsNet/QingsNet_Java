package com.ethereal.server.Core.EventRegister.Delegate;

import com.ethereal.server.Core.Model.TrackException;

public interface ExceptionEventDelegate {
    void onException(TrackException exception);
}

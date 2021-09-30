package com.ethereal.server.Core.Interface;

import com.ethereal.server.Core.Model.TrackException;

public interface IExceptionEvent {
    void onException(TrackException exception);
    void onException(TrackException.ErrorCode code, String message);
}

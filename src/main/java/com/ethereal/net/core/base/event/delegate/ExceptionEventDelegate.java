package com.ethereal.net.core.base.event.delegate;

import com.ethereal.net.core.base.BaseCore;
import com.ethereal.net.core.base.event.ExceptionEvent;
import com.ethereal.net.core.entity.TrackException;

public interface ExceptionEventDelegate {
    void onException(Exception exception);
}

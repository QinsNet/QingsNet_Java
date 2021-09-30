package com.ethereal.server.Net.Interface;

import com.ethereal.server.Core.Interface.IExceptionEvent;
import com.ethereal.server.Core.Interface.ILogEvent;
import com.ethereal.server.Core.Model.ClientRequestModel;
import com.ethereal.server.Core.Model.ClientResponseModel;
import com.ethereal.server.Core.Model.ServerRequestModel;
import com.ethereal.server.Core.Model.TrackException;
import com.ethereal.server.Server.Abstract.BaseToken;

import java.lang.reflect.InvocationTargetException;

public interface INet extends IExceptionEvent, ILogEvent {
    boolean publish() throws Exception;
    ClientResponseModel clientRequestReceiveProcess(BaseToken token, ClientRequestModel request) throws Exception;
}

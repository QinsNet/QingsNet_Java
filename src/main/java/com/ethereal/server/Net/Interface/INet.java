package com.ethereal.server.Net.Interface;

import com.ethereal.server.Core.Interface.IExceptionEvent;
import com.ethereal.server.Core.Interface.ILogEvent;
import com.ethereal.server.Core.Model.ClientRequestModel;
import com.ethereal.server.Core.Model.ClientResponseModel;
import com.ethereal.server.Server.Abstract.Token;

public interface INet extends IExceptionEvent, ILogEvent {
    boolean publish() throws Exception;
    ClientResponseModel clientRequestReceiveProcess(Token token, ClientRequestModel request) throws Exception;
}

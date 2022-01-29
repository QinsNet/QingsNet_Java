package com.ethereal.meta.meta;

import com.ethereal.meta.net.core.Net;
import com.ethereal.meta.request.core.Request;
import com.ethereal.meta.service.core.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Component {
    Class<? extends Service> service = Service.class;
    Class<? extends Request> request = Request.class;
    Class<? extends Net> net = Net.class;
}

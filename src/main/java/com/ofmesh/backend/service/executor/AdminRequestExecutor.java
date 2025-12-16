package com.ofmesh.backend.service.executor;

import com.ofmesh.backend.entity.AdminRequest;
import com.ofmesh.backend.entity.AdminRequestType;
import com.ofmesh.backend.entity.User;

public interface AdminRequestExecutor {
    AdminRequestType supports();
    String execute(AdminRequest request, User executor);
}

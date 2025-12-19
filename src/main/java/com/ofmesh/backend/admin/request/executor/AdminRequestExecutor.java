package com.ofmesh.backend.admin.request.executor;

import com.ofmesh.backend.admin.request.entity.AdminRequest;
import com.ofmesh.backend.admin.request.entity.AdminRequestType;
import com.ofmesh.backend.user.profile.entity.User;

public interface AdminRequestExecutor {
    AdminRequestType supports();
    String execute(AdminRequest request, User executor);
}

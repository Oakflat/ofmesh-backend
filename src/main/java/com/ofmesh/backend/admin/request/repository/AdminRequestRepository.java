package com.ofmesh.backend.admin.request.repository;

import com.ofmesh.backend.admin.request.entity.AdminRequest;
import com.ofmesh.backend.admin.request.entity.AdminRequestStatus;
import com.ofmesh.backend.admin.request.entity.AdminRequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminRequestRepository extends JpaRepository<AdminRequest, Long>, JpaSpecificationExecutor<AdminRequest> {

    boolean existsByTypeAndTargetUserIdAndStatus(
            AdminRequestType type,
            Long targetUserId,
            AdminRequestStatus status
    );
}

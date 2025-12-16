package com.ofmesh.backend.repository;

import com.ofmesh.backend.entity.AdminRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminRequestRepository extends JpaRepository<AdminRequest, Long>, JpaSpecificationExecutor<AdminRequest> {
}

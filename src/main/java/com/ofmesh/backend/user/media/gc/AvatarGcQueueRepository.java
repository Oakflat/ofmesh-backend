package com.ofmesh.backend.user.media.gc;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface AvatarGcQueueRepository extends JpaRepository<AvatarGcQueueItem, Long> {

    @Query("""
      select g from AvatarGcQueueItem g
      where g.status = 'PENDING' and g.deleteAfter <= :now
      order by g.id asc
    """)
    List<AvatarGcQueueItem> findDue(@Param("now") OffsetDateTime now, Pageable pageable);
}

package com.ofmesh.backend.user.profile.repository;

import com.ofmesh.backend.user.profile.entity.AccountStatus;
import com.ofmesh.backend.user.profile.entity.Role;
import com.ofmesh.backend.user.profile.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, Long> {
    @Modifying
    @Query("""
        update User u
        set u.accountStatus = :active,
            u.banUntil = null,
            u.banReason = null
        where u.id = :userId
          and u.accountStatus = :banned
          and u.banUntil is not null
          and u.banUntil <= :now
    """)
    int normalizeExpiredBan(
            @Param("userId") Long userId,
            @Param("now") OffsetDateTime now,
            @Param("banned") AccountStatus banned,
            @Param("active") AccountStatus active
    );
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    long countByRole(Role role);

    // ✅ 与 User.createdAt: OffsetDateTime 对齐（DB: timestamptz）
    long countByCreatedAtAfter(OffsetDateTime time);

    // ✅ 管理后台只读检索：用户名/邮箱模糊匹配（分页）
    @Query("""
        select u from User u
        where lower(u.username) like lower(concat('%', :q, '%'))
           or lower(u.email) like lower(concat('%', :q, '%'))
    """)
    Page<User> searchAdminUsers(@Param("q") String q, Pageable pageable);

    List<User> findByAccountStatusAndBanUntilIsNotNullAndBanUntilLessThanEqual(AccountStatus accountStatus, OffsetDateTime now, PageRequest of);
    @Query("""
        select u from User u
        where u.username = :key or u.email = :key
    """)
    Optional<User> findByLoginKey(@Param("key") String key);
    @Query("select u.avatarKey from User u where u.id = :userId")
    String getAvatarKey(@Param("userId") Long userId);

    @Query("select u.avatarPrevKey from User u where u.id = :userId")
    String getAvatarPrevKey(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update User u
       set u.avatarPrevKey = u.avatarKey,
           u.avatarKey = :newKey,
           u.avatar = :newUrl,
           u.avatarUpdatedAt = :now
     where u.id = :userId
""")
    int updateAvatarAndShiftPrev(
            @Param("userId") Long userId,
            @Param("newKey") String newKey,
            @Param("newUrl") String newUrl,
            @Param("now") OffsetDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update User u
       set u.avatarKey = u.avatarPrevKey,
           u.avatarPrevKey = u.avatarKey,
           u.avatar = :newUrl,
           u.avatarUpdatedAt = :now
     where u.id = :userId
       and u.avatarPrevKey is not null
       and u.avatarPrevKey <> ''
""")
    int rollbackAvatarToPrev(
            @Param("userId") Long userId,
            @Param("newUrl") String newUrl,
            @Param("now") OffsetDateTime now
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :userId")
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);

    @Query("""
  select count(u) from User u
  where u.avatarKey = :key or u.avatarPrevKey = :key
""")
    long countAvatarKeyReferences(@Param("key") String key);
    @Query("select u.bannerKey from User u where u.id = :userId")
    String getBannerKey(@Param("userId") Long userId);

    @Query("select u.bannerPrevKey from User u where u.id = :userId")
    String getBannerPrevKey(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
  update User u
     set u.bannerPrevKey = u.bannerKey,
         u.bannerKey = :newKey,
         u.bannerUpdatedAt = :now
   where u.id = :userId
""")
    int updateBannerAndShiftPrev(
            @Param("userId") Long userId,
            @Param("newKey") String newKey,
            @Param("now") OffsetDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
  update User u
     set u.bannerKey = u.bannerPrevKey,
         u.bannerPrevKey = u.bannerKey,
         u.bannerUpdatedAt = :now
   where u.id = :userId
     and u.bannerPrevKey is not null
     and u.bannerPrevKey <> ''
""")
    int rollbackBannerToPrev(
            @Param("userId") Long userId,
            @Param("now") OffsetDateTime now
    );

    @Query("""
  select count(u) from User u
  where u.bannerKey = :key or u.bannerPrevKey = :key
""")
    long countBannerKeyReferences(@Param("key") String key);

}

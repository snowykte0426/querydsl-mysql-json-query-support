package com.github.snowykte0426.querydsl.mysql.json.jpa.repository;

import com.github.snowykte0426.querydsl.mysql.json.jpa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for User entity.
 *
 * <p>Demonstrates integration of JSON functions with Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {

    /**
     * Find users by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Find users by name containing a substring.
     */
    List<User> findByNameContaining(String namePart);

    /**
     * Find users where metadata contains a specific JSON path value.
     * Uses native query with JSON_EXTRACT.
     */
    @Query(value = "SELECT * FROM users WHERE JSON_EXTRACT(metadata, :path) = :value", nativeQuery = true)
    List<User> findByMetadataPath(@Param("path") String path, @Param("value") String value);

    /**
     * Find users where metadata contains a specific value.
     * Uses native query with JSON_CONTAINS.
     */
    @Query(value = "SELECT * FROM users WHERE JSON_CONTAINS(metadata, :value)", nativeQuery = true)
    List<User> findByMetadataContaining(@Param("value") String value);

    /**
     * Find users where a JSON key exists in metadata.
     * Uses native query with JSON_CONTAINS_PATH.
     */
    @Query(value = "SELECT * FROM users WHERE JSON_CONTAINS_PATH(metadata, 'one', :path)", nativeQuery = true)
    List<User> findByMetadataKeyExists(@Param("path") String path);

    /**
     * Find users with specific role in metadata.
     */
    @Query(value = "SELECT * FROM users WHERE JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')) = :role", nativeQuery = true)
    List<User> findByRole(@Param("role") String role);

    /**
     * Find users by settings theme.
     */
    @Query(value = "SELECT * FROM users WHERE JSON_UNQUOTE(JSON_EXTRACT(settings, '$.theme')) = :theme", nativeQuery = true)
    List<User> findByTheme(@Param("theme") String theme);

    /**
     * Count users by metadata depth.
     */
    @Query(value = "SELECT COUNT(*) FROM users WHERE JSON_DEPTH(metadata) >= :minDepth", nativeQuery = true)
    long countByMetadataDepth(@Param("minDepth") int minDepth);
}

package com.voxever.teammies.auth.repository;

import com.voxever.teammies.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    List<RefreshToken> findAllByPrefix(String tokenPrefix);
}
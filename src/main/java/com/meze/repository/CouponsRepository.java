package com.meze.repository;

import com.meze.domains.Coupons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponsRepository extends JpaRepository<Coupons, Long> {
    @Query("SELECT c FROM Coupons c WHERE c.code=:couponCode")
    Optional<Coupons> findByCouponCode(@Param("couponCode") String couponCode);
}

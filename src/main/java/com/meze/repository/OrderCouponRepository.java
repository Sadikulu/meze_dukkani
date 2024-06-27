package com.meze.repository;

import com.meze.domains.OrderCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderCouponRepository extends JpaRepository<OrderCoupon,Long> {

    Page<OrderCoupon> findByUserId(Long id, Pageable pageable);

    OrderCoupon findByOrderId(Long orderId);

    boolean existsByCouponsIdAndUserId(Long couponId,Long userId);

    OrderCoupon findByOrderIdAndUserId(Long orderId,Long userId);

    Optional<OrderCoupon> findByCouponsCode(String couponCode);
}

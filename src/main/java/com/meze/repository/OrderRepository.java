package com.meze.repository;

import com.meze.domains.Order;
import com.meze.domains.User;
import com.meze.domains.UserAddress;
import com.meze.domains.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    //**********************GETALLORDERS****************************
   @Query("SELECT o from Order o where o.code=:query and o.status=:status and" +
           " o.createAt >:date1 and o.createAt<:date2 " )
    Page<Order> findAll(@Param("query") String query,
                        @Param("status") String status,
                        @Param("date1") LocalDateTime date1,
                        @Param("date2") LocalDateTime date2,
                        Pageable pageable);
//****************************************************************

    Optional<Order> findById(Long id);

    //@Query("Select o from Order o join o.user u where u.id=:user_id")
  // Optional<Order> findByUserAndId(User user, Long id);

    boolean existsByUser(User user);

    //************************
    @EntityGraph(attributePaths = "id")
    List<Order> getAllBy();
//*************************************
//*********
@Query("SELECT o from Order o where o.user.id=:userId" )
 Page<Order> findAll(@Param("userId") Long userId, Pageable pageable);
//*****************************************

    @Query("SELECT o from Order o where o.user=:user_id and o.status=:status and" +
            " o.createAt >:date1 and o.createAt<:date2 ")
    Page<Order> findByUserAndStatus(Pageable pageable,
                                      @Param("date1") LocalDateTime date1,
                                      @Param("date2") LocalDateTime date2,
                                      @Param("status") OrderStatus status,
                                      @Param("user") User user  );
    boolean existsByInvoiceAddress(UserAddress userAddress);
    boolean existsByShippingAddress(UserAddress userAddress);

    @Query("select case when count(o) > 0 then true else false end " +
            "from Order o " +
            "join o.orderItems oi " +
            "where o.user.id = :userId " +
            "and oi.product.id = :productId")
    Boolean existsByUserIdAndProductId(@Param("userId")Long userId,@Param("productId") Long productId);

    Optional<Order> findByCode(String orderCode);
    @EntityGraph(attributePaths = "id")
    Optional<Order> findByIdAndUser(Long id, User user);

    @Modifying
    @Query("DELETE FROM Order o WHERE o.user.id IS NULL")
    void deleteAllWithoutUser();
}
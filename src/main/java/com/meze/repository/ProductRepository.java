package com.meze.repository;

import com.meze.domains.Product;
import com.meze.domains.enums.BrandStatus;
import com.meze.domains.enums.CategoryStatus;
import com.meze.domains.enums.ProductStatus;
import com.meze.dto.MostPopularProduct;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {

    //Boolean existsByBrandId(Long id);

    Boolean existsByCategoryId(Long id);

    @Query( "SELECT p FROM Product p JOIN p.image img WHERE img.id=:id")
    Product findProductByImageId(@Param("id") String id );
    
    @EntityGraph(attributePaths = {"image", "id"})
    Optional<Product> findProductById(Long id);

    @Query("SELECT p FROM OrderItem oi INNER JOIN oi.product p WHERE p.id=:productId")
    List<Product> checkOrderItemsByID(@Param("productId") Long id);

    @Query("SELECT toi.product.id as productId, SUM(toi.quantity) as salesCount \n" +
            "FROM OrderItem toi \n" +
            "WHERE toi.createAt > :startDate \n" +
            "AND toi.product.category.status = :cStatus \n" +
            //"AND toi.product.brand.status = :bStatus\n" +
            "AND toi.product.status = :pStatus\n" +
            "GROUP BY productId \n" +
            "ORDER BY salesCount DESC")
    List<MostPopularProduct> findMostPopularProductsOfLastMonth(@Param("startDate") LocalDateTime startDate,
                                                                @Param("cStatus") CategoryStatus categoryStatus,
                                                                //@Param("bStatus") BrandStatus brandStatus,
                                                                @Param("pStatus") ProductStatus productStatus,
                                                                Pageable pageable);

    @Query("SELECT oi.product.id as productId, SUM(oi.quantity) as salesCount \n" +
            "FROM OrderItem oi \n" +
            "WHERE oi.createAt > :startDate \n" +
            "group by productId \n" +
            "order by salesCount desc")
    List<MostPopularProduct> findMostPopularProductsOfLastMonthAdmin(@Param("startDate")LocalDateTime startDate, Pageable pageable);

    @Query("SELECT oi.product.id as productId, oi.product.title as title, oi.product.price as price, oi.product.category.title as category, SUM(oi.quantity) AS salesCount \n" +
            "FROM OrderItem oi \n" +
            "GROUP BY productId,title,price,category \n" +
            "ORDER BY salesCount DESC")
    List<MostPopularProduct> findMostPopularProductsOfLastMonthWithoutPage(@Param("startDate") LocalDateTime startDate);

    Page<Product> findProductsByIdIn(List<Long> productIdList,Pageable pageable);

    @Query("SELECT p\n" +
            "FROM Product p\n" +
            "WHERE p.featured = true\n" +
            "  AND p.category.status = :cStatus\n" +
            //"  AND p.brand.status = :bStatus\n" +
            "  AND p.status = :pStatus")
    Page<Product> findFeaturedProducts(@Param("cStatus") CategoryStatus categoryStatus,
                                       //@Param("bStatus") BrandStatus brandStatus,
                                       @Param("pStatus") ProductStatus productStatus,
                                       Pageable pageable);

    @Query("SELECT p\n" +
            "FROM Product p\n" +
            "WHERE p.featured = true")
    Page<Product> findFeaturedProductsForAdmin(Pageable pageable);

    @Query("SELECT p\n" +
            "FROM Product p\n" +
            "WHERE p.newProduct = true\n" +
            "  AND p.category.status = :cStatus\n" +
            //"  AND p.brand.status = :bStatus\n" +
            "  AND p.status = :pStatus")
    Page<Product> findNewProducts(@Param("cStatus") CategoryStatus categoryStatus,
                                  //@Param("bStatus") BrandStatus brandStatus,
                                  @Param("pStatus") ProductStatus productStatus,
                                  Pageable pageable);

    @Query("SELECT p\n" +
            "FROM Product p\n" +
            "WHERE p.newProduct = true\n")
    Page<Product> findNewProductsForAdmin(Pageable pageable);

    @EntityGraph(attributePaths = "id")
    @NotNull
    List<Product> findAll();

    void deleteAllByBuiltInFalse();
}

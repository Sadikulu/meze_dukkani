package com.meze.repository;

import com.meze.domains.Brand;
import com.meze.domains.enums.BrandStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand,Long> {


    boolean existsByName(String name);

    @Query("SELECT count(b) from Brand b join b.image img where img.id=:id")
    Integer findBrandByImage_Id(@Param("id")String id);


    Optional<Page<Brand>>  findAllByStatus(Pageable pageable,BrandStatus status);

    @Query("SELECT b from Brand b WHERE b.status=:status and b.id=:id")
   Optional<Brand>  getBrandByStatus_PublishedAndId(@Param("status")BrandStatus status,@Param("id") Long id);


    @Query("SELECT b FROM Brand b WHERE b.status =:status  AND b.id=:id")
    Optional<Brand> findByStatusAndId(@Param("status") BrandStatus status, Long id );

    @EntityGraph(attributePaths = "image")
    Optional<Brand> findById(Long id);

    boolean existsByImageId(String id);

    void deleteAllByBuiltInFalse();
}

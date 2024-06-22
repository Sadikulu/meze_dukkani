package com.meze.repository;

import com.meze.domains.User;
import com.meze.domains.UserAddress;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress,Long> {

    @EntityGraph(attributePaths = {"user"})
    List<UserAddress> findAllByUser(User user);

    @EntityGraph(attributePaths = {"user"})
    Optional<UserAddress> findByIdAndUser(User user, Long id);

    Boolean existsByTitle(String title);
}

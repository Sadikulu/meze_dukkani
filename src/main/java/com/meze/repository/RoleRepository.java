package com.meze.repository;

import com.meze.domains.Role;
import com.meze.domains.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepository extends JpaRepository<Role, Byte> {

    Optional<Role> findByRoleName(RoleType roleName);

}

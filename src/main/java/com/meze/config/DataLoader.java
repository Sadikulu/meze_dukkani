package com.meze.config;

import com.meze.domains.Role;
import com.meze.domains.enums.RoleType;
import com.meze.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {

    roleRepository.save(new Role( 1, RoleType.ROLE_CUSTOMER));
    roleRepository.save(new Role(2,RoleType.ROLE_MANAGER));
    roleRepository.save(new Role(3,RoleType.ROLE_ADMIN));
    }
}


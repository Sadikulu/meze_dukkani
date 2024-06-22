package com.meze.service;

import com.meze.domains.Role;
import com.meze.domains.enums.RoleType;
import com.meze.exception.ResourceNotFoundException;
import com.meze.exception.message.ErrorMessage;
import com.meze.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    @Autowired
    private final RoleRepository roleRepository;

    public Role findByRoleName(RoleType roleType) {

        return roleRepository.findByRoleName(roleType).orElseThrow(()->
                new ResourceNotFoundException(String.format(
                        ErrorMessage.ROLE_NOT_FOUND_MESSAGE, roleType.name())));

    }
}

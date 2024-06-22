package com.meze.mapper;

import com.meze.domains.User;
import com.meze.dto.UserDTO;
import com.meze.dto.UserDeleteDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring",uses = ProductMapper.class)
public interface UserMapper {

    UserDTO userToUserDTO(User user);

    List<UserDTO> map(List<User> userList);

    UserDeleteDTO userToUserDeleteDTO(User user);

}

package com.meze.mapper;

import com.meze.domains.User;
import com.meze.domains.UserAddress;
import com.meze.dto.UserAddressDTO;
import com.meze.dto.request.UserAddressRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserAddressMapper {


    @Mapping(target = "userId",source = "user",qualifiedByName = "getUserIdForAddress")
    UserAddressDTO userAddressToUserAddressDTO(UserAddress userAddress);

    List<UserAddressDTO> map(List<UserAddress> userAddressList);

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "createAt",ignore = true)
    @Mapping(target = "updateAt",ignore = true)
    @Mapping(target = "user",ignore = true)
    UserAddress userAddressRequestToUserAddress(UserAddressRequest userAddressRequest);

    @Named("getUserIdForAddress")
    public static Long getUserIdForAddress(User user){
        return user.getId();
    }
}

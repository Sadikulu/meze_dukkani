package com.meze.mapper;

import com.meze.domains.ShoppingCart;
import com.meze.dto.ShoppingCartDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = ShoppingCartItemMapper.class)
public interface ShoppingCartMapper {

  @Mapping(source = "shoppingCartItem", target = "shoppingCartItemDTO")
  ShoppingCartDTO shoppingCartToShoppingCartDTO(ShoppingCart shoppingCart);

  List <ShoppingCartDTO> map(List<ShoppingCart> shoppingCarts);

}



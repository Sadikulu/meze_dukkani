package com.meze.mapper;

import com.meze.domains.ImageFile;
import com.meze.domains.Product;
import com.meze.domains.ShoppingCartItem;
import com.meze.dto.ShoppingCartItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShoppingCartItemMapper {

    @Mapping(source = "product", target = "productId", qualifiedByName = "getProductId")
    @Mapping(source = "product", target = "title", qualifiedByName = "getProductTitle")
    @Mapping(source = "product", target = "imageId", qualifiedByName = "getProductImageId")
    @Mapping(source = "product", target = "unitPrice", qualifiedByName = "getProductPrice")
    //@Mapping(source = "product", target = "discount", qualifiedByName = "getProductDiscount")
    //@Mapping(source = "product",target = "discountedPrice", qualifiedByName = "getDiscountedPrice")
    @Mapping(source = "product",target = "tax",qualifiedByName = "getTaxRate")
    @Mapping(source = "product",target = "stockAmount",qualifiedByName = "getStockAmount")
    ShoppingCartItemDTO shoppingCartItemToShoppingCartItemDTO(ShoppingCartItem shoppingCartItem);

    List<ShoppingCartItemDTO> shoppingCartItemToShoppingCartItemDTOAsList(List<ShoppingCartItem> shoppingCartItemList);

    @Named("getProductId")
    public static Long getProductId(Product product){
        return product.getId();
    }

    @Named("getProductTitle")
    public static String getProductTitle(Product product){
        return product.getTitle();
    }

    @Named("getProductImageId")
    public static String getProductImageId(Product product){
        return product.getImage().stream().filter(ImageFile::isShowcase).map(ImageFile::getId).findFirst().orElse(null);
    }

    @Named("getProductPrice")
    public static String getProductPrice(Product product){
        return product.getPrice();
    }

//    @Named("getProductDiscount")
//    public static Integer getProductDiscount(Product product){
//        return product.getDiscount();
//    }

//    @Named("getDiscountedPrice")
//    public static Double getDiscountedPrice(Product product){
//        return product.getDiscountedPrice();
//    }

    @Named("getTaxRate")
    public static Double getTaxRate(Product product){
        return product.getTax();
    }
    @Named("getStockAmount")
    public static Integer getStockAmount(Product product){
        return product.getStockAmount();
    }

}
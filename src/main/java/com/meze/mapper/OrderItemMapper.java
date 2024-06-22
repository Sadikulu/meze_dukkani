package com.meze.mapper;

import com.meze.domains.ImageFile;
import com.meze.domains.OrderItem;
import com.meze.domains.Product;
import com.meze.dto.OrderItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.text.DecimalFormat;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    DecimalFormat df = new DecimalFormat("#.##");

    @Mapping(source = "product",target = "productId",qualifiedByName = "getProductId")
    @Mapping(source = "product",target = "imageId",qualifiedByName = "getImageId")
    @Mapping(source = "product",target = "title",qualifiedByName = "getTitleOfProduct")
    @Mapping(target = "tax",expression = "java(orderItemToTax(orderItem))")
    @Mapping(target = "discount",expression = "java(orderItemToDiscount(orderItem))")
    OrderItemDTO orderItemToOrderItemDTO(OrderItem orderItem);

    List<OrderItemDTO> orderItemsToOrderItemsDTO(List<OrderItem> orderItems);

    @Named("getProductId")
    public static Long getProductId(Product product){
        return product.getId();
    }

    @Named("getImageId")
    public static String getImageId(Product product){
       return product.getImage().stream().filter(ImageFile::isShowcase).map(ImageFile::getId).findFirst().orElse(null);
    }

    @Named("getTitleOfProduct")
    public static String getTitleOfProduct(Product product){
        return product.getTitle();
    }

    public default double orderItemToTax(OrderItem orderItem) {
        return Double.parseDouble(df.format(((((orderItem.getUnitPrice() * orderItem.getQuantity()) * (100-orderItem.getDiscount())) / 100) * orderItem.getTax()) / 100).replaceAll(",","."));
    }

    public default double orderItemToDiscount(OrderItem orderItem) {
        return Double.parseDouble(df.format(((orderItem.getUnitPrice() * orderItem.getQuantity()) * orderItem.getDiscount()) / 100).replaceAll(",","."));
    }
}

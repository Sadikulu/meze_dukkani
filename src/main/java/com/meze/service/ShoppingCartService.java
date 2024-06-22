package com.meze.service;

import com.meze.domains.Product;
import com.meze.domains.ShoppingCart;
import com.meze.domains.ShoppingCartItem;
import com.meze.dto.ShoppingCartDTO;
import com.meze.dto.ShoppingCartItemDTO;
import com.meze.dto.request.ShoppingCartRequest;
import com.meze.dto.request.ShoppingCartUpdateRequest;
import com.meze.exception.BadRequestException;
import com.meze.exception.ResourceNotFoundException;
import com.meze.exception.message.ErrorMessage;
import com.meze.mapper.ShoppingCartItemMapper;
import com.meze.mapper.ShoppingCartMapper;
import com.meze.repository.ShoppingCartItemRepository;
import com.meze.repository.ShoppingCartRepository;
import com.meze.reusableMethods.DiscountCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;

    private final ShoppingCartMapper shoppingCartMapper;

    private final ProductService productService;

    private final ShoppingCartItemRepository shoppingCartItemRepository;

    private final ShoppingCartItemMapper shoppingCartItemMapper;

    private final DiscountCalculator discountCalculator;


    public ShoppingCart findShoppingCartByUUID(String cartUUID) {
        return shoppingCartRepository.findByCartUUID(cartUUID).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE,cartUUID)));
    }

    public ShoppingCartItemDTO createCartItem(String cartUUID,ShoppingCartRequest shoppingCartRequest) {

        ShoppingCart shoppingCart = findShoppingCartByUUID(cartUUID);

        Double totalPrice=0.0;
        Product product = productService.findProductById(shoppingCartRequest.getProductId());
        ShoppingCartItem foundItem = shoppingCartItemRepository.findByProductIdAndShoppingCartCartUUID(product.getId(), shoppingCart.getCartUUID());
        ShoppingCartItem shoppingCartItem = null;
        if (shoppingCartRequest.getQuantity() > product.getStockAmount()){
            throw new BadRequestException(String.format(ErrorMessage.PRODUCT_OUT_OF_STOCK_MESSAGE,product.getId()));
        }
        if (!shoppingCart.getShoppingCartItem().isEmpty() && shoppingCart.getShoppingCartItem().contains(foundItem)) {
            if (shoppingCartRequest.getQuantity() > foundItem.getProduct().getStockAmount()){
                throw new BadRequestException(String.format(ErrorMessage.PRODUCT_OUT_OF_STOCK_MESSAGE,product.getId()));
            }
            int quantity = foundItem.getQuantity() + shoppingCartRequest.getQuantity();
            foundItem.setQuantity(quantity);
            if (!Character.isLetter(product.getPrice().charAt(0))){
                totalPrice = quantity*Double.parseDouble(product.getPrice());
                foundItem.setTotalPrice(totalPrice);
            }
            shoppingCartItemRepository.save(foundItem);
            if (!Character.isLetter(product.getPrice().charAt(0))){
                shoppingCart.setGrandTotal(shoppingCart.getGrandTotal()+(shoppingCartRequest.getQuantity()*Double.parseDouble(foundItem.getProduct().getPrice())));
            }
            shoppingCartItem = foundItem;
            shoppingCartItem.setUpdateAt(LocalDateTime.now());
        } else{
            shoppingCartItem = new ShoppingCartItem();
            shoppingCartItem.setProduct(product);
            shoppingCartItem.setQuantity(shoppingCartRequest.getQuantity());
            shoppingCartItem.setShoppingCart(shoppingCart);
            if (!Character.isLetter(product.getPrice().charAt(0))){
                totalPrice = shoppingCartRequest.getQuantity()*Double.parseDouble(product.getPrice());
                shoppingCartItem.setTotalPrice(totalPrice);
            }
            shoppingCartItemRepository.save(shoppingCartItem);
            shoppingCart.getShoppingCartItem().add(shoppingCartItem);
            shoppingCart.setGrandTotal(shoppingCart.getGrandTotal()+totalPrice);
        }

        shoppingCartRepository.save(shoppingCart);
        return shoppingCartItemMapper.shoppingCartItemToShoppingCartItemDTO(shoppingCartItem);
    }

    public ShoppingCartItemDTO removeItemById(String cartUUID,Long productId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByCartUUID(cartUUID).orElseThrow(()->
                new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE));
        ShoppingCartItem foundItem = shoppingCartItemRepository.findByProductIdAndShoppingCartCartUUID(productId,
               cartUUID);
        shoppingCart.setGrandTotal(shoppingCart.getGrandTotal()-foundItem.getTotalPrice());
        shoppingCartItemRepository.delete(foundItem);
        shoppingCartRepository.save(shoppingCart);
        return shoppingCartItemMapper.shoppingCartItemToShoppingCartItemDTO(foundItem);
    }

    public void cleanShoppingCart(String cartUUID){
        ShoppingCart shoppingCart = findShoppingCartByUUID(cartUUID);
        shoppingCartItemRepository.deleteAll(shoppingCart.getShoppingCartItem());
        shoppingCart.setGrandTotal(0.0);
    }

    public ShoppingCartItemDTO changeQuantity(String cartUUID,ShoppingCartUpdateRequest shoppingCartUpdateRequest, String op) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByCartUUID(cartUUID).orElseThrow(()->
                new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE));
        Product product = productService.findProductById(shoppingCartUpdateRequest.getProductId());
        ShoppingCartItem foundItem = shoppingCartItemRepository.findByProductIdAndShoppingCartCartUUID(product.getId(),shoppingCart.getCartUUID());
        if (!Character.isLetter(foundItem.getProduct().getPrice().charAt(0))){
            switch (op){
                case "increase":
                    foundItem.setQuantity(foundItem.getQuantity()+1);
                    shoppingCart.setGrandTotal(shoppingCart.getGrandTotal()+Double.parseDouble(foundItem.getProduct().getPrice()));
                    break;
                case "decrease":
                    foundItem.setQuantity(foundItem.getQuantity()-1);
                    shoppingCart.setGrandTotal(shoppingCart.getGrandTotal()-Double.parseDouble(foundItem.getProduct().getPrice()));
                    break;
            }
            Double totalPrice = discountCalculator.totalPriceWithDiscountCalculate(foundItem.getQuantity(), Double.parseDouble(product.getPrice()));
            foundItem.setTotalPrice(totalPrice);
        }
        foundItem.setUpdateAt(LocalDateTime.now());
        shoppingCartItemRepository.save(foundItem);
        save(shoppingCart);

        return shoppingCartItemMapper.shoppingCartItemToShoppingCartItemDTO(foundItem);
    }


    public ShoppingCartDTO getShoppingCart(String cartUUID) {
        ShoppingCart shoppingCart;
        if (!cartUUID.isEmpty()){
            shoppingCart = findShoppingCartByUUID(cartUUID);
        } else{
            shoppingCart = new ShoppingCart();
            shoppingCart.setCartUUID(UUID.randomUUID().toString());
            shoppingCartRepository.save(shoppingCart);

        }
       return shoppingCartMapper.shoppingCartToShoppingCartDTO(shoppingCart);
    }

    public void save(ShoppingCart shoppingCart) {
        shoppingCartRepository.save(shoppingCart);
    }

    public void removeAllNotOwnedByUsers() {
        shoppingCartRepository.deleteAllShoppingCartsWithoutUser();
    }
}


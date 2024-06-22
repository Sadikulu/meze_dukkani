package com.meze.service;

import com.meze.dto.DashboardCountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class DatabaseService {
    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
//    private final BrandService brandService;
//    private final CouponsService couponsService;
    private final ReviewService reviewService;
//    private final OrderService orderService;
    private final ContactMessageService contactMessageService;
    private final ShoppingCartService shoppingCartService;

    @Transactional
    public void resetAll(){
    userService.removeAllBuiltInFalseUsers();
    productService.removeAllBuiltInFalseProducts();
    categoryService.removeAllBuiltInFalseCategories();
//    brandService.removeAllBuiltInFalseBrands();
//    couponsService.removeAll();
    contactMessageService.removeAll();
    shoppingCartService.removeAllNotOwnedByUsers();
    //orderService.removeAllNotOwnedByUser();
    }

    public DashboardCountDTO getCountOfAllRecords() {
        long userCount = userService.countUserRecords();
        //long brandCount = brandService.countBrandRecords();
        long categoryCount = categoryService.countCategoryRecords();
        long productCount = productService.countProductRecords();
//        long orderCount = orderService.countOrderRecords();
//        long couponCount = couponsService.countCouponRecords();
        long reviewCount = reviewService.countReviewRecords();
        long contactMessageCount = contactMessageService.countContactMessageRecords();
        DashboardCountDTO count = new DashboardCountDTO();
        count.setCustomerCount(userCount);
//        count.setBrandCount(brandCount);
        count.setCategoryCount(categoryCount);
        count.setProductCount(productCount);
//        count.setOrderCount(orderCount);
//        count.setCouponCount(couponCount);
        count.setReviewCount(reviewCount);
        count.setContactMessageCount(contactMessageCount);
        return count;
    }
}

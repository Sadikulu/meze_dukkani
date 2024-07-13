package com.meze.service;


import com.meze.domains.*;
import com.meze.dto.MostPopularProduct;
import com.meze.exception.message.ErrorMessage;
import com.meze.report.ExcelReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private CategoryService categoryService;

//    @Autowired
    //private BrandService brandService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;
//    @Autowired
//    private OrderService orderService;
//    @Autowired
//    private CouponsService couponsService;
// **************************** REPORT *********************

    public ByteArrayInputStream getReport() {
        List<Category> categories=categoryService.getAllCategories();
        //List<Brand>  brands=brandService.getAllBrands();
        List<Product> products=productService.getAllProducts();
        //List<Order> orders=orderService.getAllOrdersWithPage();
        List<User> users =  userService.getAllUsers();
        //List<Coupons> coupons =couponsService.getAllCoupons();
        try {
            return ExcelReporter.getExcelReport(categories,products,users);
        } catch (IOException e) {
            throw new RuntimeException(ErrorMessage.EXCEL_REPORT_ERROR_MESSAGE);
        }


    }

 //   *********Report Orders*********
//    public ByteArrayInputStream getOrderReport(LocalDate date1, LocalDate date2, String type) {
//
//        List<Order> orders =orderService.getAllOrdersWithPage();
//
//        try {
//            return ExcelReporter.getOrderExcelReport(orders,date1,date2,type);
//        } catch (IOException e) {
//            throw new RuntimeException(ErrorMessage.EXCEL_REPORT_ERROR_MESSAGE);
//        }
//    }
    //*********Most Popular Products Report*********
    public ByteArrayInputStream getReportMostPopularProduct(int amount) {

        List<MostPopularProduct> products = productService.findMostPopularProductsOfLastMonthWithoutPage();
        try {
            return ExcelReporter.getReportMostPopularProduct (products,amount);
        } catch (IOException e) {
            throw new RuntimeException(ErrorMessage.EXCEL_REPORT_ERROR_MESSAGE);
        }
    }

    //*********Stock Alarm Report*********
    public ByteArrayInputStream getReportStockAlarm() {

        List<Product> products =productService.getAllProducts();

        try {
            return ExcelReporter.getReportStockAlarm(products);
        } catch (IOException e) {
            throw new RuntimeException(ErrorMessage.EXCEL_REPORT_ERROR_MESSAGE);
        }
    }
    //*********Unordered Product Report*********
//    public ByteArrayInputStream getReportUnorderedProducts() {
//
//        List<Product> products =productService.getAllProducts();
//        List<Order> orders =orderService.getAllOrdersWithPage();
//
//
//        try {
//            return ExcelReporter.getReportUnorderedProducts(products,orders);
//        } catch (IOException e) {
//            throw new RuntimeException(ErrorMessage.EXCEL_REPORT_ERROR_MESSAGE);
//        }
//    }


}

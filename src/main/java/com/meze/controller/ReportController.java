package com.meze.controller;

import com.meze.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {
    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Resource> getReport()  {
        String fileName="reports.xlsx";
        ByteArrayInputStream bais=reportService.getReport();

        InputStreamResource file=new InputStreamResource(bais);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName).
                contentType(MediaType.parseMediaType("application/vmd.ms-excel")).body(file);

    }
  //  *********Report Orders*********
//    @GetMapping("/orders")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<Resource> getOrderReport(
//            @RequestParam("date1") String startDate,
//            @RequestParam("date2") String endDate,
//            @RequestParam("type") String type)
//    {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDate date1 = LocalDate.parse(startDate, formatter);
//        LocalDate date2 = LocalDate.parse(endDate, formatter);
//        String fileName="orderReport.xlsx";
//        //ByteArrayInputStream bais=reportService.getOrderReport(date1,date2,type);
//
//        //InputStreamResource file=new InputStreamResource(bais);
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName).
//                contentType(MediaType.parseMediaType("application/vmd.ms-excel")).body(file);
//
//    }

   // *********Most Popular Products Report*********
    @GetMapping("/most-popular-products")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Resource> getReportMostPopularProducts(
            @RequestParam("amount")  int amount)
    {
        String fileName="most-popular-products-report.xlsx";
        ByteArrayInputStream bais=reportService.getReportMostPopularProduct(amount);
        InputStreamResource file=new InputStreamResource(bais);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName).
                contentType(MediaType.parseMediaType("application/vmd.ms-excel")).body(file);

    }

    //*********Stock Alarm Report*********
    @GetMapping("/stock-alarm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Resource> getReportStockAlarm()
    {
        String fileName="stockAlarmReport.xlsx";
        ByteArrayInputStream bais=reportService.getReportStockAlarm();
        InputStreamResource file=new InputStreamResource(bais);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName).
                contentType(MediaType.parseMediaType("application/vmd.ms-excel")).body(file);
    }


    //*********Unordered Product Report*********
//    @GetMapping("/unordered-products")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//    public ResponseEntity<Resource> getReportUnorderedProducts()
//    {
//        String fileName="unordered-products.xlsx";
//        ByteArrayInputStream bais=reportService.getReportUnorderedProducts();
//
//        InputStreamResource file=new InputStreamResource(bais);
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName).
//                contentType(MediaType.parseMediaType("application/vmd.ms-excel")).body(file);
//
//    }

}



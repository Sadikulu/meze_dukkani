package com.meze.report;
import com.meze.domains.*;
import com.meze.dto.MostPopularProduct;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class ExcelReporter {
    //***********GeneralReports**************
    static String SHEET_REPORT = "Reports";
    static String[] REPORT_HEADERS = {"Categories",
             "Products", "Customers",
            "DiscountCodes"};

    //********** ORDERS ***************************

    static String SHEET_ORDERS = "Orders";
    static String[] ORDERS_HEADERS = {"Period", "Total Product", "Total Amount"};

    static String SHEET_POPULAR_PRODUCTS = "PopularProducts";
    static String[] POPULAR_PRODUCTS_HEADERS = {"Id", "Title", "Price", "Category", "Sales Count"};

    static String SHEET_STOCK_ALARM_PRODUCTS = "StockAlarmProducts";
    static String[] STOCK_ALARM_HEADERS = {"Id", "Title", "Price", "Category", "Stock Amount", "Stock Limit"};
    static String SHEET_UNORDERED_PRODUCTS = "StockAlarmProducts";
    static String[] UNORDERED_PRODUCTS_HEADERS = {"Id", "Title", "Price", "Category"};

    public static ByteArrayInputStream getExcelReport(List<Category> categories, List<Product> products, List<User> users) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Sheet sheet = workbook.createSheet(SHEET_REPORT);
        Row headerRow = sheet.createRow(0);

        // header alanını doldurduk
        for (int i = 0; i < REPORT_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(REPORT_HEADERS[i]);
        }

        // datalarını koyacağız

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue(categories.stream().map(t -> t.getTitle()).distinct().count());
        //row.createCell(1).setCellValue(brands.stream().map(t -> t.getName()).distinct().count());
        row.createCell(2).setCellValue(products.stream().map(t -> t.getId()).count());
        //row.createCell(3).setCellValue(orders.stream().map(t -> t.getCreateAt()).filter(t -> t.isEqual(LocalDateTime.now())).count());
        row.createCell(4).setCellValue(users.stream().map(t -> t.getRoles()).filter(t -> t.equals("Customer")).count());
        //row.createCell(5).setCellValue(coupons.stream().map(t -> t.getCode()).count());

        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());

    }

    //*******************ORDERS_REPORT***********************

    public static ByteArrayInputStream getOrderExcelReport(List<Order> orders, LocalDate date1, LocalDate date2, String type) throws IOException {
            Workbook workbook = new XSSFWorkbook();
        if (type.equals("day") || type.equals("week") || type.equals("month") || type.equals("year")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet(SHEET_ORDERS);
            Row headerRow = sheet.createRow(0);

            // header alanını doldurduk
            for (int i = 0; i < ORDERS_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(ORDERS_HEADERS[i]);
            }

            // datalarını koyacağız

            int rowId = 1;
            if (type.equals("day")) {
                for (LocalDate date = date1; date.isBefore(date2); date = date.plusDays(1)) {
                    LocalDateTime day=date.atStartOfDay();
                    Row row = sheet.createRow(rowId++);

                    row.createCell(0).setCellValue(date.getDayOfMonth()+" "+date.getMonth()+" "+date.getYear());
                    row.createCell(1).setCellValue(orders.stream().filter(d->d.getCreateAt().isAfter(day)&& d.getCreateAt().isBefore(day.plusDays(1))).map(o -> o.getOrderItems().stream().map(p -> p.getProduct().getId())).count());
                    row.createCell(2).setCellValue(orders.stream().filter(d->d.getCreateAt().isAfter(day) && d.getCreateAt().isBefore(day.plusDays(1))).map(t -> t.getOrderItems().stream().map(a -> a.getQuantity())).flatMap(x -> x).reduce(0, (Integer::sum)));

                }
            } else if (type.equals("week")) {
                for (LocalDate date = date1; date.isBefore(date2); date = date.plusWeeks(1)) {
                    LocalDateTime day=date.atStartOfDay();
                    Row row = sheet.createRow(rowId++);

                    row.createCell(0).setCellValue((rowId-1)+".week");
                    row.createCell(1).setCellValue(orders.stream().filter(d->d.getCreateAt().isAfter(day)&& d.getCreateAt().isBefore(day.plusWeeks(1))).map(t -> t.getOrderItems().stream().map(p -> p.getProduct().getId())).count());
                    row.createCell(2).setCellValue(orders.stream().filter(d->d.getCreateAt().isAfter(day) && d.getCreateAt().isBefore(day.plusWeeks(1))).map(t -> t.getOrderItems().stream().map(a -> a.getQuantity())).flatMap(x -> x).reduce(0, (Integer::sum)));
                }
            } else if (type.equals("month")) {
                for (LocalDate date = date1; date.isBefore(date2); date = date.plusMonths(1)) {
                    LocalDateTime day=date.atStartOfDay();
                    Row row = sheet.createRow(rowId++);

                    row.createCell(0).setCellValue(date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())+" "+date.getYear());
                    row.createCell(1).setCellValue(orders.stream().filter(d->d.getCreateAt().isAfter(day)&& d.getCreateAt().isBefore(day.plusMonths(1))).map(t -> t.getOrderItems().stream().map(p -> p.getProduct().getId())).count());
                    row.createCell(2).setCellValue(orders.stream().filter(d->d.getCreateAt().isAfter(day) && d.getCreateAt().isBefore(day.plusMonths(1))).map(t -> t.getOrderItems().stream().map(a -> a.getQuantity())).flatMap(x -> x).reduce(0, (Integer::sum)));

                }
            } else if (type.equals("year")) {
                for (LocalDate date = date1; date.isBefore(date2); date = date.plusYears(1)) {
                    LocalDateTime day=date.atStartOfDay();
                    Row row = sheet.createRow(rowId++);

                    row.createCell(0).setCellValue(date.getYear());
                    row.createCell(1).setCellValue(orders.stream().filter(d->d.getCreateAt().isAfter(day)&& d.getCreateAt().isBefore(day.plusYears(1))).map(t -> t.getOrderItems().stream().map(p -> p.getProduct().getId())).count());
                    row.createCell(2).setCellValue(orders.stream().filter(d->d.getCreateAt().isAfter(day) && d.getCreateAt().isBefore(day.plusYears(1))).map(t -> t.getOrderItems().stream().map(a -> a.getQuantity())).flatMap(x -> x).reduce(0, (Integer::sum)));
                }
            }

            workbook.write(out);
            workbook.close();
            return new ByteArrayInputStream(out.toByteArray());
        } else {
            System.out.println("Error very type");
        }

        return null;

    }


    //*********Most Popular Products Report*********
    public static ByteArrayInputStream getReportMostPopularProduct(List<MostPopularProduct> products, int amount) throws IOException {
    List<MostPopularProduct> mostPopularProducts=products.stream().limit(amount).collect(Collectors.toList());
        Workbook workbook = new XSSFWorkbook();

        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        Sheet sheet =  workbook.createSheet(SHEET_POPULAR_PRODUCTS);
        Row headerRow =  sheet.createRow(0);

        // header alanını doldurduk
        for(int i=0; i<POPULAR_PRODUCTS_HEADERS.length;i++ ) {
            Cell cell = headerRow.createCell(i) ;
            cell.setCellValue(POPULAR_PRODUCTS_HEADERS[i]);
        }

        // datalarını koyacağız
        int rowId=1;


        for(MostPopularProduct each: mostPopularProducts) {
            Row row = sheet.createRow(rowId++);


            row.createCell(0).setCellValue(each.getProductId());
            row.createCell(1).setCellValue(each.getTitle());
            row.createCell(2).setCellValue(each.getPrice());
            row.createCell(3).setCellValue(each.getCategory());
            row.createCell(4).setCellValue(each.getSalesCount());

        }

        workbook.write(out);
        workbook.close();



        return new ByteArrayInputStream(out.toByteArray());


    }

    //*********Stock Alarm Report*********
    public static ByteArrayInputStream getReportStockAlarm(List<Product> products) throws IOException {

        Workbook workbook = new XSSFWorkbook();

        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        Sheet sheet =  workbook.createSheet(SHEET_STOCK_ALARM_PRODUCTS);
        Row headerRow =  sheet.createRow(0);

        // header alanını doldurduk
        for(int i=0; i<STOCK_ALARM_HEADERS.length;i++ ) {
            Cell cell = headerRow.createCell(i) ;
            cell.setCellValue(STOCK_ALARM_HEADERS[i]);
        }

        // datalarını koyacağız
        int rowId=1;
//stok alarmına göre ayarlanacak
        for(Product product: products) {
            Row row = sheet.createRow(rowId++);
            //if(product.getStockAmount()<product.getStockAlarmLimit()) {
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getTitle());
                row.createCell(2).setCellValue(product.getPrice());
                row.createCell(3).setCellValue(product.getCategory().getTitle());
                row.createCell(4).setCellValue(product.getStockAmount());
//                row.createCell(5).setCellValue(product.getStockAlarmLimit());

            //}
        }
        workbook.write(out);
        workbook.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

    public static ByteArrayInputStream getReportUnorderedProducts(List<Product> products,List<Order>orders) throws IOException {

        Workbook workbook = new XSSFWorkbook();

        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        Sheet sheet =  workbook.createSheet(SHEET_UNORDERED_PRODUCTS);
        Row headerRow =  sheet.createRow(0);

        // header alanını doldurduk
        for(int i=0; i<UNORDERED_PRODUCTS_HEADERS.length;i++ ) {
            Cell cell = headerRow.createCell(i) ;
            cell.setCellValue(UNORDERED_PRODUCTS_HEADERS[i]);
        }
List<Product> unOrderedProducts=products.stream().filter(t->t.getOrderItems().isEmpty()).collect(Collectors.toList());
        // datalarını koyacağız
        int rowId=1;

        for(Product product: unOrderedProducts) {
            Row row = sheet.createRow(rowId++);

                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getTitle());
                row.createCell(2).setCellValue(product.getPrice());
                row.createCell(3).setCellValue(product.getCategory().getTitle());

          }


        workbook.write(out);
        workbook.close();



        return new ByteArrayInputStream(out.toByteArray());


    }
}

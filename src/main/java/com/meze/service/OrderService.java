package com.meze.service;

import com.meze.domains.*;
import com.meze.domains.Order;
import com.meze.domains.User;
import com.meze.domains.UserAddress;
import com.meze.domains.enums.*;
import com.meze.dto.OrderDTO;
import com.meze.dto.OrderItemDTO;
import com.meze.dto.request.OrderItemQuantityUpdateRequest;
import com.meze.dto.request.OrderRequest;
import com.meze.dto.request.OrderUpdateProduct;
import com.meze.dto.request.OrderUpdateRequest;
import com.meze.exception.BadRequestException;
import com.meze.exception.ResourceNotFoundException;
import com.meze.exception.message.ErrorMessage;
import com.meze.mapper.OrderItemMapper;
import com.meze.mapper.OrderMapper;
import com.meze.repository.*;
import com.meze.reusableMethods.UniqueIdGenerator;
import com.meze.service.email.EmailSender;
import com.meze.service.email.EmailService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
//@RequiredArgsConstructor
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ShoppingCartService shoppingCartService;
    private final UserAddressService userAddressService;
    private final TransactionRepository transactionRepository;
    private final OrderItemService orderItemService;
    private final OrderItemMapper orderItemMapper;
    private final PaymentService paymentService;
    private final ProductService productService;
//    private final CouponsService couponsService;
    private final UniqueIdGenerator uniqueIdGenerator;
//    private final OrderCouponRepository orderCouponRepository;
    private final OrderItemRepository orderItemRepository;
//    private final CouponsRepository couponsRepository;
    private final EntityManager entityManager;
    private final EmailSender emailSender;
    private final EmailService emailService;

    public OrderService(OrderMapper orderMapper,
                        OrderRepository orderRepository,
                        @Lazy UserService userService,
                        @Lazy ShoppingCartService shoppingCartService,
                        @Lazy UserAddressService userAddressService,
                        TransactionRepository transactionRepository,
                        OrderItemService orderItemService,
                        OrderItemMapper orderItemMapper,
                        PaymentService paymentService,
                        @Lazy ProductService productService,
//                        CouponsService couponsService,
                        UniqueIdGenerator uniqueIdGenerator,
//                        OrderCouponRepository orderCouponRepository,
                        OrderItemRepository orderItemRepository,
//                        CouponsRepository couponsRepository,
                        EntityManager entityManager,
                        EmailSender emailSender,
                        EmailService emailService) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.shoppingCartService = shoppingCartService;
        this.transactionRepository = transactionRepository;
        this.userAddressService = userAddressService;
        this.orderItemService = orderItemService;
        this.orderItemMapper = orderItemMapper;
        this.paymentService = paymentService;
        this.productService = productService;
//        this.couponsService = couponsService;
        this.uniqueIdGenerator = uniqueIdGenerator;
//        this.orderCouponRepository = orderCouponRepository;
        this.orderItemRepository = orderItemRepository;
//        this.couponsRepository = couponsRepository;
        this.entityManager = entityManager;
        this.emailSender = emailSender;
        this.emailService = emailService;
    }


    public PageImpl<OrderDTO> getAllOrdersWithPage(String query, List<OrderStatus> status, String date1, String date2, Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = cb.createQuery(Order.class);
        Root<Order> root = criteriaQuery.from(Order.class);

        List<Predicate> predicates = new ArrayList<>();
        Predicate finalPredicate = null;

        if (query != null && !query.isEmpty()){
            String likeSearchText = "%" + query.toLowerCase(Locale.US) + "%";
            Predicate searchByOrderCode = cb.like(cb.lower(root.get("code")),likeSearchText);
            Predicate searchByContactName = cb.like(cb.lower(root.get("contactName")),likeSearchText);
            predicates.add(cb.or(searchByOrderCode,searchByContactName));
        }
        if (status != null && !status.isEmpty()){
            predicates.add(root.get("status").in(status));
        }
        if (date1 != null && date2 != null){
            LocalDateTime startDate = LocalDate.parse(date1, formatter).atStartOfDay();
            LocalDateTime endDate = LocalDate.parse(date2, formatter).atStartOfDay();
            predicates.add(cb.between(root.get("createAt"),startDate,endDate));
        }else{
            if (date1 != null){
                LocalDateTime startDate = LocalDate.parse(date1, formatter).atStartOfDay();
                predicates.add(cb.greaterThan(root.get("createAt"),startDate));
            }
            if (date2 != null) {
                LocalDateTime endDate = LocalDate.parse(date2, formatter).atStartOfDay();
                predicates.add(cb.lessThan(root.get("createAt"), endDate));
            }
        }


        finalPredicate = cb.and(predicates.toArray(new Predicate[0]));

        criteriaQuery.orderBy(pageable.getSort().stream()
                .map(order -> {
                    if (order.isAscending()) {
                        return cb.asc(root.get(order.getProperty()));
                    } else {
                        return cb.desc(root.get(order.getProperty()));
                    }
                })
                .collect(Collectors.toList()));

        criteriaQuery.where(finalPredicate);

        TypedQuery<Order> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult((int)pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(Order.class)));
        countQuery.where(finalPredicate);
        Long totalRecords = entityManager.createQuery(countQuery).getSingleResult();

        List<OrderDTO> orderDTOList = orderMapper.map(typedQuery.getResultList());

        return new PageImpl<>(orderDTOList,pageable,totalRecords);
    }


    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(ErrorMessage.ORDER_NOT_FOUND_MESSAGE, id)));
    }

    public OrderDTO findOrderByIdAndUser(Long id) {
        User user = userService.getCurrentUser();
        Order order = orderRepository.findByIdAndUser(id,user).orElseThrow(
                () -> new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));
        return orderMapper.orderToOrderDTO(order);


    }

    public boolean existsByUser(User user) {
        return orderRepository.existsByUser(user);

    }

    public Page<OrderDTO> findAuthOrderWithPage(Pageable pageable) {
        User user = userService.getCurrentUser();
        Page<Order> orderPage = orderRepository.findAll(user.getId(),pageable);

        return orderPage.map(orderMapper::orderToOrderDTO);
    }


    public PageImpl<OrderDTO> findUserWithOrderAndPage(Long userId,
                                                       String date1,
                                                       String date2,
                                                       List<OrderStatus> status,
                                                       Pageable pageable) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> criteriaQuery = cb.createQuery(Order.class);
        Root<Order> root = criteriaQuery.from(Order.class);

        List<Predicate> predicates = new ArrayList<>();
        Predicate finalPredicate = null;

        predicates.add(cb.equal(root.get("user").get("id"),userId));
        if (date1 != null && date2 != null){
            LocalDateTime startDate = LocalDate.parse(date1, formatter).atStartOfDay();
            LocalDateTime endDate = LocalDate.parse(date2, formatter).atStartOfDay();
            predicates.add(cb.between(root.get("createAt"),startDate,endDate));
        }else{
            if (date1 != null){
                LocalDateTime startDate = LocalDate.parse(date1, formatter).atStartOfDay();
                predicates.add(cb.greaterThan(root.get("createAt"),startDate));
            }
            if (date2 != null) {
                LocalDateTime endDate = LocalDate.parse(date2, formatter).atStartOfDay();
                predicates.add(cb.lessThan(root.get("createAt"), endDate));
            }
        }

        if (status != null && !status.isEmpty()){
            predicates.add(root.get("status").in(status));
        }

        finalPredicate = cb.and(predicates.toArray(new Predicate[0]));

        criteriaQuery.orderBy(pageable.getSort().stream()
                .map(order -> {
                    if (order.isAscending()) {
                        return cb.asc(root.get(order.getProperty()));
                    } else {
                        return cb.desc(root.get(order.getProperty()));
                    }
                })
                .collect(Collectors.toList()));
        criteriaQuery.where(finalPredicate);

        TypedQuery<Order> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult((int)pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(Order.class)));
        countQuery.where(finalPredicate);
        Long totalRecords = entityManager.createQuery(countQuery).getSingleResult();

        List<OrderDTO> orderDTOList = orderMapper.map(typedQuery.getResultList());

        return new PageImpl<>(orderDTOList,pageable,totalRecords);
    }

    //getAll***********************************
    public List<Order> getAllOrdersWithPage() {
        return orderRepository.getAllBy();


    }

    //***********************************
    public boolean existsByInvoiceAddress(UserAddress userAddress) {
        return orderRepository.existsByInvoiceAddress(userAddress);
    }

    public boolean existsByShippingAddress(UserAddress userAddress) {
        return orderRepository.existsByInvoiceAddress(userAddress);
    }

    @Transactional
    public OrderDTO createOrder(String cartUUID, OrderRequest orderRequest) {
        ShoppingCart shoppingCart = shoppingCartService.findShoppingCartByUUID(cartUUID);
        List<ShoppingCartItem> shoppingCartItemList = shoppingCart.getShoppingCartItem();
        DecimalFormat df = new DecimalFormat("#.##");
        Order order = new Order();
        Payment payment = new Payment();
        Transaction transaction = new Transaction();
        User user = userService.getCurrentUser();
        List<Product> lowStockList = new ArrayList<>();
        List<User> managerList = userService.findUserByRole(RoleType.ROLE_MANAGER);
        UserAddress shippingAddress = userAddressService.
                getAddressById(orderRequest.getShippingAddressId());
        UserAddress invoiceAddress = userAddressService.
                getAddressById(orderRequest.getInvoiceAddressId());
//        Coupons coupon = couponsService.getCouponByCouponCode(orderRequest.getCouponCode());
        OrderCoupon orderCoupon = null;


        if (shoppingCartItemList.isEmpty()) {
            throw new ResourceNotFoundException(ErrorMessage.UUID_NOT_FOUND_MESSAGE);
        }
        String[] shippingCompany = {"UPS","FedEx","Amazon Logistics","USPS","DHL Express","OnTrac","Purolator","LaserShip","Aramex","ShipBob"};
        String[] provider = {"PayPal","Stripe", "Square", "Authorize.net","Braintree", "Dwolla", "Amazon Pay", "Google Pay", "Apple Pay", "Visa Checkout"};
        List<Integer> digits = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            digits.add(i);
        }
        Collections.shuffle(digits);
        StringBuilder randomNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            randomNumber.append(digits.get((int) ((Math.random() * 10))));
        }
//        if (!orderRequest.getCouponCode().isEmpty()){
//            if (coupon.getStatus().equals(CouponsStatus.PASSIVE)){
//                throw new BadRequestException(String.format(ErrorMessage.COUPON_NOT_VALID_MESSAGE,coupon.getCode()));
//            }
//            if (coupon.getLife()==0){
//                throw new BadRequestException(String.format(ErrorMessage.COUPON_NOT_VALID_MESSAGE,coupon.getCode()));
//            }
//            if (coupon.getLife()!=-1){
//                coupon.setLife(coupon.getLife()-1);
//            }
//            orderCoupon = new OrderCoupon();
//            orderCoupon.setCoupons(coupon);
//            orderCoupon.setUser(user);
//            orderCoupon.setOrder(order);
//            boolean isCouponUsedBefore = orderCouponRepository.existsByCouponsIdAndUserId(coupon.getId(),user.getId());
//            if (isCouponUsedBefore){
//                throw new BadRequestException(String.format(ErrorMessage.COUPON_ALREADY_USED_MESSAGE,coupon.getCode()));
//            }
//            couponsRepository.save(coupon);
//            orderCouponRepository.save(orderCoupon);
//        }

        double discount = 0.0;
        double tax = calculateTaxCost(shoppingCartItemList);
        double subTotal = 0.0;

        for (ShoppingCartItem each : shoppingCartItemList) {
            Product product = each.getProduct();
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setSku(product.getSku());
            if (each.getQuantity() > each.getProduct().getStockAmount()){
                throw new BadRequestException(String.format(ErrorMessage.PRODUCT_OUT_OF_STOCK_MESSAGE,each.getProduct().getId()));
            }
            orderItem.setQuantity(each.getQuantity());
            orderItem.setDiscount(product.getDiscount());
            orderItem.setTax(product.getTax());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubTotal(each.getProduct().getPrice()* each.getQuantity());
            orderItemService.save(orderItem);
            order.getOrderItems().add(orderItem);

            discount+=((product.getPrice()-product.getDiscountedPrice()) * each.getQuantity());
            subTotal+= product.getPrice()*each.getQuantity();

//            if (each.getProduct().getStockAlarmLimit()>= each.getProduct().getStockAmount() - each.getQuantity()){
//                lowStockList.add(each.getProduct());
//            }

            Integer newStockAmount = product.getStockAmount() - each.getQuantity();
            product.setStockAmount(newStockAmount);
            productService.save(product);
        }

        double grandTotal = Double.parseDouble(df.format(grandTotalCalculator(subTotal,discount,tax)).replaceAll(",","."));
        double shippingCost = calculateShippingCost(grandTotal);

//        if(orderCoupon!=null && orderCoupon.getCoupons().getType().equals(CouponsType.EXACT_AMOUNT))
//            if (!(grandTotal >= orderCoupon.getCoupons().getAmount() * 10)) {
//                throw new BadRequestException(String.format(ErrorMessage.COUPON_CAN_NOT_BE_USED_MESSAGE, Double.parseDouble(df.format(orderCoupon.getCoupons().getAmount() * 10).replaceAll(",","."))));
//            }


        transaction.setTransaction(TransactionStatus.CREATED);
        user.getTransactions().add(transaction);
        user.getOrders().add(order);
        payment.setAmount(grandTotal+shippingCost);
        payment.setProvider(provider[(int)(Math.random()*shippingCompany.length)]);
        payment.setStatus(PaymentStatus.COMPLETED);
        transactionRepository.save(transaction);
        userService.save(user);
        paymentService.save(payment);

        order.setCode(uniqueIdGenerator.generateUniqueId(8));
        order.setContactName(orderRequest.getContactName());
        order.setContactPhone(orderRequest.getPhoneNumber());
        order.setGrandTotal(grandTotal+shippingCost);
        order.setShippingCost(calculateShippingCost(order.getGrandTotal()));
        order.setStatus(OrderStatus.PENDING);
        order.setInvoiceAddress(invoiceAddress);
        order.setShippingAddress(shippingAddress);
        order.setTax(Double.parseDouble(df.format(tax).replaceAll(",",".")));
        order.setDiscount(Double.parseDouble(df.format(discount).replaceAll(",",".")));
        order.setSubTotal(Double.parseDouble(df.format(subTotal).replaceAll(",",".")));
        order.setUser(user);
        order.setShippingDetails(shippingCompany[(int)(Math.random()*shippingCompany.length)] + " : "+ randomNumber);
        order.getTransaction().add(transaction);
        order.getPayments().add(payment);
//        if (orderCoupon!=null){
//            order.getOrderCoupons().add(orderCoupon);
//        }
        orderRepository.save(order);

        for (User each:managerList) {
            emailSender.send(
                    each.getEmail(),
                    emailService.buildOrderMail(order)
            );
        }
        emailSender.send(
                user.getEmail(),
                emailService.buildOrderMail(order)
        );

//        if(lowStockList.size()>0){
//            for (User managers:managerList) {
//                emailSender.send(
//                        managers.getEmail(),
//                        emailService.buildStockLimitAlarmEmail(managers.getFirstName(),lowStockList)
//                );
//            }
//        }
        shoppingCartService.cleanShoppingCart(cartUUID);
        return orderMapper.orderToOrderDTO(order);
    }

    private double grandTotalCalculator(double subTotal,double discount, double tax) {
        double grandTotal = 0.0;
        //        if (orderCoupon!=null) {
//            if (orderCoupon.getCoupons().getType().equals(CouponsType.EXACT_AMOUNT)) {
//                grandTotal = beforeCouponPrice - orderCoupon.getCoupons().getAmount();
//            }else{
//                grandTotal= beforeCouponPrice*((100-orderCoupon.getCoupons().getAmount())/100);
//            }
//        }else{
//            grandTotal = beforeCouponPrice;
//        }
        grandTotal= (subTotal-discount)+tax;
        return grandTotal;
    }

    private double calculateTaxCost(List<ShoppingCartItem> shoppingCartItemList) {
        double taxCost = 0.0;
        for (ShoppingCartItem each:shoppingCartItemList) {
            taxCost+= (each.getProduct().getDiscountedPrice()* each.getQuantity()*(each.getProduct().getTax()))/100;
        }
        return taxCost;
    }


    private double calculateShippingCost(double orderGrandTotal) {
        double shippingCost = 0.0;

        if (orderGrandTotal<=750){
            shippingCost = 5.0;
        } else if (orderGrandTotal <= 1500.0){
            shippingCost = 15.0;
        } else if (orderGrandTotal <= 3000.0) {
            shippingCost = 25.0;
        } else if (orderGrandTotal <= 5000.0) {
            shippingCost = 35.0;
        }else return shippingCost;

        return shippingCost;
    }

    public OrderDTO findOrderById(Long id) {
        Order order = getOrderById(id);
        return orderMapper.orderToOrderDTO(order);
    }

    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        Transaction transaction = new Transaction();
        User user = order.getUser();
        if (status.equals(OrderStatus.BEING_SUPPLIED) || status.equals(OrderStatus.READY_TO_SHIP)){
            transaction.setTransaction(TransactionStatus.UPDATED);
        }else if(status.equals(OrderStatus.DELIVERY_DONE)){
            transaction.setTransaction(TransactionStatus.COMPLETED);
        }else if (status.equals(OrderStatus.RETURNED) || status.equals(OrderStatus.CANCELED)) {
            transaction.setTransaction(TransactionStatus.CANCELED);
//            OrderCoupon orderCoupon = orderCouponRepository.findByOrderIdAndUserId(orderId,user.getId());
//            orderCouponRepository.delete(orderCoupon);
//            orderCoupon.getCoupons().setLife(orderCoupon.getCoupons().getLife()+1);
            Payment payment = new Payment();
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setAmount(order.getGrandTotal());
            payment.setProvider(order.getPayments().get(order.getPayments().size()-1).getProvider());
            for (OrderItem each:order.getOrderItems()) {
                each.getProduct().setStockAmount(each.getProduct().getStockAmount()+ each.getQuantity());
            }
            paymentService.save(payment);
            orderRepository.save(order);
        }

        transactionRepository.save(transaction);
        user.getTransactions().add(transaction);
        order.getTransaction().add(transaction);
        orderRepository.save(order);

        return orderMapper.orderToOrderDTO(order);
    }

    public OrderDTO updateOrder(OrderUpdateRequest orderUpdateRequest) {
        Order order = getOrderById(orderUpdateRequest.getOrderId());
        if (order.getUpdateAt()!=null){
            throw new BadRequestException(ErrorMessage.ORDER_CAN_BE_UPDATED_ONLY_ONCE_MESSAGE);
        }
        DecimalFormat df = new DecimalFormat("#.##");
        User user = userService.getCurrentUser();
        Transaction transaction = new Transaction();
        transaction.setTransaction(TransactionStatus.UPDATED);
        transactionRepository.save(transaction);
//        OrderCoupon orderCoupon = orderCouponRepository.findByOrderId(orderUpdateRequest.getOrderId());

        if (!(order.getStatus().equals(OrderStatus.PENDING) || order.getStatus().equals(OrderStatus.BEING_SUPPLIED)
                || order.getStatus().equals(OrderStatus.READY_TO_SHIP))){
            throw new BadRequestException(String.format(ErrorMessage.ORDER_CAN_NOT_BE_UPDATED_MESSAGE, order.getStatus()));
        }

        List<OrderItem> oldOrderItems = order.getOrderItems();
        List<OrderItem> updatedOrderItems = new ArrayList<>();
        Double tempSubTotal = null;
        Integer quantityDifference = null;
        double oldGrandTotal = order.getGrandTotal();
        double oldDiscountedPrice = order.getDiscount();
        double newOrderListSubTotal = 0.0;
        double newDiscountedPrice = 0.0;
        double newTaxPrice = 0.0;

        for (OrderItem each : oldOrderItems) {
            boolean deleted = true;
            for (OrderUpdateProduct product:orderUpdateRequest.getProducts()) {
                tempSubTotal = each.getSubTotal();
                quantityDifference = Math.abs(product.getQuantity()-each.getQuantity());
                if (each.getProduct().getId().longValue() == product.getProductId().longValue()) {
                    if (each.getQuantity() < product.getQuantity()){
                        if (quantityDifference > each.getProduct().getStockAmount()) {
                            throw new BadRequestException(String.format(ErrorMessage.PRODUCT_OUT_OF_STOCK_MESSAGE,each.getProduct().getId()));
                        }
                        each.setQuantity(product.getQuantity());
                        each.setDiscount(each.getProduct().getDiscount());
                        each.setUnitPrice(each.getProduct().getPrice());
                        each.setTax(each.getProduct().getTax());
                        each.setSubTotal(tempSubTotal+(quantityDifference*each.getUnitPrice()));
                        each.setUpdateAt(LocalDateTime.now());
                        newOrderListSubTotal +=(tempSubTotal+(quantityDifference*each.getUnitPrice()));
                        newDiscountedPrice +=(oldDiscountedPrice+((each.getProduct().getPrice()-each.getProduct().getDiscountedPrice()) * quantityDifference));
                        newTaxPrice += ((each.getProduct().getDiscountedPrice()* quantityDifference *(each.getProduct().getTax()))/100);
                        updatedOrderItems.add(each);
                        each.getProduct().setStockAmount(each.getProduct().getStockAmount()-(quantityDifference));
                        deleted = false;
                        break;
                    }else if (each.getQuantity() > product.getQuantity()){
                        each.setQuantity(product.getQuantity());
                        each.setSubTotal(each.getQuantity() * each.getUnitPrice());
                        each.setUpdateAt(LocalDateTime.now());
                        newOrderListSubTotal += (each.getQuantity() * each.getUnitPrice());
                        newDiscountedPrice += ((each.getUnitPrice()*each.getDiscount()/100) * each.getQuantity());
                        newTaxPrice += (((each.getUnitPrice() - (each.getUnitPrice() * each.getDiscount() / 100)) * quantityDifference) * (((each.getProduct().getTax()))/100));
                        updatedOrderItems.add(each);
                        each.getProduct().setStockAmount(each.getProduct().getStockAmount()+(quantityDifference));
                        deleted = false;
                        break;
                    }else{
                        newOrderListSubTotal += each.getSubTotal();
                        newDiscountedPrice += (each.getUnitPrice()*each.getDiscount()/100);
                        newTaxPrice += ((each.getUnitPrice() - (each.getUnitPrice() * each.getDiscount() / 100)) * ((each.getProduct().getTax())/100));
                        updatedOrderItems.add(each);
                        deleted = false;
                        break;
                    }
                }
            }
            if (deleted){
                each.getProduct().setStockAmount(each.getProduct().getStockAmount()+each.getQuantity());
                orderItemRepository.delete(each);
            }
        }

        order.getOrderItems().clear();
        order.getOrderItems().addAll(updatedOrderItems);
        order.setTax(Double.parseDouble(df.format(newTaxPrice).replaceAll(",",".")));
        order.setStatus(OrderStatus.PENDING);
        order.getTransaction().add(transaction);
        Payment oldPayment = order.getPayments().get(order.getPayments().size()-1);

        double updatedOrderGrandTotal = Double.parseDouble(df.format(grandTotalCalculator(newOrderListSubTotal,newDiscountedPrice,newTaxPrice)).replaceAll(",","."));
        double updatedShippingCost = calculateShippingCost(updatedOrderGrandTotal);

        Payment payment = new Payment();
        if (oldGrandTotal > updatedOrderGrandTotal){
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setAmount(oldGrandTotal-(updatedOrderGrandTotal+updatedShippingCost));
        }else if(updatedOrderGrandTotal > oldGrandTotal){
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setAmount((updatedOrderGrandTotal+updatedShippingCost)-oldGrandTotal);
        }

        order.setGrandTotal(updatedOrderGrandTotal+updatedShippingCost);
        order.setDiscount(Double.parseDouble(df.format(newDiscountedPrice).replaceAll(",",".")));
        order.setShippingCost(updatedShippingCost);
        payment.setProvider(oldPayment.getProvider());
        paymentService.save(payment);
        order.setSubTotal(Double.parseDouble(df.format(newOrderListSubTotal).replaceAll(",",".")));
        order.getPayments().add(payment);
        order.setUpdateAt(LocalDateTime.now());
        orderRepository.save(order);
        user.getTransactions().add(transaction);
        userService.save(user);
        return orderMapper.orderToOrderDTO(order);
    }

    public long countOrderRecords() {
        return orderRepository.count();
    }

    public void removeAllNotOwnedByUser() {
        orderRepository.deleteAllWithoutUser();
    }
}

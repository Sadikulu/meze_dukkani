package com.meze.service;

import com.meze.domains.*;
import com.meze.domains.enums.BrandStatus;
import com.meze.domains.enums.CategoryStatus;
import com.meze.domains.enums.ProductStatus;
import com.meze.domains.enums.RoleType;
import com.meze.dto.MostPopularProduct;
import com.meze.dto.ProductDTO;
import com.meze.dto.request.ProductRequest;
import com.meze.dto.request.ProductUpdateRequest;
import com.meze.exception.BadRequestException;
import com.meze.exception.ConflictException;
import com.meze.exception.ResourceNotFoundException;
import com.meze.exception.message.ErrorMessage;
import com.meze.mapper.ProductMapper;
import com.meze.repository.ImageFileRepository;
import com.meze.repository.ProductRepository;
import com.meze.repository.UserRepository;
import com.meze.reusableMethods.UniqueIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final CategoryService categoryService;
    private final ImageFileService imageFileService;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final UniqueIdGenerator uniqueIdGenerator;
    private final UserService userService;
    private final RoleService roleService;
    private final ImageFileRepository imageFileRepository;
    //private final BrandRepository brandRepository;
    private final EntityManager entityManager;
    private final UserRepository userRepository;

    public Product findProductById(Long id){
        return productRepository.findProductById(id).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE,id)));
    }

    public ProductDTO saveProduct(ProductRequest productRequest) {
        Set<ImageFile> imageFiles = new HashSet<>();
        for (String each:productRequest.getImageId()) {
            Product foundProduct = productRepository.findProductByImageId(each);
            if (foundProduct==null){
                imageFiles.add(imageFileService.getImageById(each));
            }else{
                throw new ConflictException(ErrorMessage.IMAGE_USED_MESSAGE);
            }
        }
        boolean hasShowcase = false;
        for (ImageFile each:imageFiles) {
            if (each.isShowcase()){
                hasShowcase = true;
                break;
            }
        }
        if(!hasShowcase){
            ImageFile imageFile = imageFiles.stream().findFirst().orElse(null);
            assert imageFile != null;
            imageFile.setShowcase(true);
        }
//        Brand brand = brandRepository.findById(productRequest.getBrandId()).orElseThrow(()->
//                new ResourceNotFoundException(String.format(ErrorMessage.BRAND_NOT_FOUND_MESSAGE,productRequest.getBrandId())));;
        Category category = categoryService.getCategoryById(productRequest.getCategoryId());

        Product product = productMapper.productRequestToProduct(productRequest);
        product.setSku(uniqueIdGenerator.generateUniqueId(8));
        product.setSlug(URLEncoder.encode(productRequest.getTitle(), StandardCharsets.UTF_8));
        product.setStatus(ProductStatus.NOT_PUBLISHED);
        //product.setDiscountedPrice(Double.parseDouble(product.getPrice())*(100-product.getDiscount())/100);
        //product.setBrand(brand);

        product.setPrice(productRequest.getPrice());
        product.setImage(imageFiles);
        //product.setStatus(productRequest.getStatus());
        product.setCategory(category);
        product.setDiscountedPrice(Double.parseDouble(product.getPrice())*(100-product.getDiscount())/100);
        productRepository.save(product);

        return productMapper.productToProductDTO(product);
    }

    public ProductDTO updateProduct(Long id, ProductUpdateRequest productUpdateRequest) {
        Product product = findProductById(id);
        Set<ImageFile> imageFiles = product.getImage();
        for (String each:productUpdateRequest.getImageId()) {
                Product foundProduct = productRepository.findProductByImageId(each);
                if (foundProduct==null){
                    imageFiles.add(imageFileService.getImageById(each));
                }else{
                    throw new ConflictException(ErrorMessage.IMAGE_USED_MESSAGE);
                }
        }
        boolean hasShowcase = false;
        for (ImageFile each:imageFiles) {
            if (each.isShowcase()){
                hasShowcase = true;
                break;
            }
        }
        if(!hasShowcase){
            ImageFile imageFile = imageFiles.stream().findFirst().orElse(null);
            assert imageFile != null;
            imageFile.setShowcase(true);
        }
//        Brand brand = brandRepository.findById(productUpdateRequest.getBrandId()).orElseThrow(()->
//                new ResourceNotFoundException(String.format(ErrorMessage.BRAND_NOT_FOUND_MESSAGE,id)));
        Category category = categoryService.getCategoryById(productUpdateRequest.getCategoryId());

        if(product.getBuiltIn()){
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }

//      product = productMapper.productUpdateRequestToProduct(productUpdateRequest);
        product.setTitle( productUpdateRequest.getTitle() );
        product.setShortDesc( productUpdateRequest.getShortDesc() );
        product.setLongDesc( productUpdateRequest.getLongDesc() );
        product.setPrice( productUpdateRequest.getPrice() );
        product.setTax( productUpdateRequest.getTax() );
        product.setDiscount( productUpdateRequest.getDiscount() );
        product.setStockAmount( productUpdateRequest.getStockAmount() );
//        product.setStockAlarmLimit( productUpdateRequest.getStockAlarmLimit() );
        product.setSlug(URLEncoder.encode(productUpdateRequest.getTitle(), StandardCharsets.UTF_8));
        product.setFeatured( productUpdateRequest.getFeatured() );
        product.setNewProduct( productUpdateRequest.getNewProduct() );
        product.setStatus( productUpdateRequest.getStatus() );
//        product.setWidth( productUpdateRequest.getWidth() );
//        product.setLength( productUpdateRequest.getLength() );
//        product.setHeight( productUpdateRequest.getHeight() );
        product.setUpdateAt( LocalDateTime.now() );
        //product.setBrand(brand);
        product.setCategory(category);
        product.setImage(imageFiles);
        product.setDiscountedPrice(Double.parseDouble(product.getPrice())*(100-product.getDiscount())/100);
        product.setPrice(productUpdateRequest.getPrice());
        productRepository.save(product);

        return productMapper.productToProductDTO(product);
    }

    public ProductDTO removeById(Long id) {
        Product product = findProductById(id);
        if (product.getBuiltIn()){
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        List<Product> products = productRepository.checkOrderItemsByID(id);
        if (!products.isEmpty()){
            throw new BadRequestException(ErrorMessage.PRODUCT_USED_BY_ORDER_MESSAGE);
        }

        ProductDTO productDTO =  productMapper.productToProductDTO(product);
        List<User> userList = userService.getAllUsers();
        for (User each:userList) {
            each.getFavoriteList().remove(product);
            userRepository.save(each);
        }
        productRepository.deleteById(id);
        return productDTO;
    }

    public Page<ProductDTO> findMostPopularProductsOfLastMonth(Pageable pageable) {
        List<MostPopularProduct> mostPopularProductInfoList = null;
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        try {
            Role role = roleService.findByRoleName(RoleType.ROLE_ADMIN);
            boolean isAdmin = userService.getCurrentUser().getRoles().stream().anyMatch(r->r.equals(role));
            if (isAdmin) {
                mostPopularProductInfoList = productRepository.findMostPopularProductsOfLastMonthAdmin(startDate,pageable);
            }else throw new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE);
        } catch (ResourceNotFoundException e) {
            CategoryStatus cStatus = CategoryStatus.PUBLISHED;
            BrandStatus bStatus = BrandStatus.PUBLISHED;
            ProductStatus pStatus = ProductStatus.PUBLISHED;
            mostPopularProductInfoList = productRepository.findMostPopularProductsOfLastMonth(startDate, cStatus, pStatus, pageable);
        }

        Page<Product> mostPopularProductList = getAllMostPopularProducts(mostPopularProductInfoList,pageable);

        return mostPopularProductList.map(productMapper::productToProductDTO);
    }

    private Page<Product> getAllMostPopularProducts(List<MostPopularProduct> mostPopularProductInfoList,Pageable pageable) {
        List<Long> productIdList = new ArrayList<>();
        for (MostPopularProduct mostPopularProduct : mostPopularProductInfoList) {
            productIdList.add(mostPopularProduct.getProductId());
        }
        return productRepository.findProductsByIdIn(productIdList,pageable);
    }

    public List<MostPopularProduct> findMostPopularProductsOfLastMonthWithoutPage() {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        return productRepository.findMostPopularProductsOfLastMonthWithoutPage(startDate);
    }


    public Page<ProductDTO> findFeaturedProducts(Pageable pageable) {
        Page<Product> featuredProductList = null;
        try {
            Role role = roleService.findByRoleName(RoleType.ROLE_ADMIN);
            boolean isAdmin = userService.getCurrentUser().getRoles().stream().anyMatch(r->r.equals(role));
            if (isAdmin){
                featuredProductList = productRepository.findFeaturedProductsForAdmin(pageable);
            }else throw new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE);
        } catch (ResourceNotFoundException e) {
            CategoryStatus cStatus = CategoryStatus.PUBLISHED;
            BrandStatus bStatus = BrandStatus.PUBLISHED;
            ProductStatus pStatus = ProductStatus.PUBLISHED;
            featuredProductList = productRepository.findFeaturedProducts(cStatus,pStatus,pageable);
        }
        assert featuredProductList != null;
        return featuredProductList.map(productMapper::productToProductDTO);
    }

    public Page<ProductDTO> findNewProducts(Pageable pageable) {
        Page<Product> newProductList = null;
        try {
            Role role = roleService.findByRoleName(RoleType.ROLE_ADMIN);
            boolean isAdmin = userService.getCurrentUser().getRoles().stream().anyMatch(r->r.equals(role));
            if (isAdmin){
                newProductList = productRepository.findNewProductsForAdmin(pageable);
            }else throw new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE);
        } catch (ResourceNotFoundException e) {
            CategoryStatus cStatus = CategoryStatus.PUBLISHED;
            BrandStatus bStatus = BrandStatus.PUBLISHED;
            ProductStatus pStatus = ProductStatus.PUBLISHED;
            newProductList = productRepository.findNewProducts(cStatus,pStatus,pageable);
        }
        assert newProductList != null;
        return newProductList.map(productMapper::productToProductDTO);
    }

    public ProductDTO getProductDTOById(Long id) {
        Product product = null;
        try {
            Role role = roleService.findByRoleName(RoleType.ROLE_ADMIN);
            boolean isAdmin = userService.getCurrentUser().getRoles().stream().anyMatch(r->r.equals(role));
            if (isAdmin){
               product = findProductById(id);
            }else throw new ResourceNotFoundException(String.format(ErrorMessage.PRODUCT_NOT_FOUND_MESSAGE,id));
        } catch (ResourceNotFoundException e) {
            product = findProductById(id);
            if (product.getStatus().equals(ProductStatus.NOT_PUBLISHED)){
                throw new ResourceNotFoundException(String.format(ErrorMessage.PRODUCT_NOT_FOUND_MESSAGE,id));
            }
        }
        return productMapper.productToProductDTO(product);
    }

    public List<Product> getAllProducts(){
       return productRepository.findAll();

    }

    public ProductDTO setFavorite(Long id) {
       User user = userService.getCurrentUser();
       Product product = findProductById(id);
       if (!user.getFavoriteList().contains(product)) {
           user.getFavoriteList().add(product);
       }else {
           user.getFavoriteList().remove(product);
       }
            userService.save(user);
            productRepository.save(product);
       return productMapper.productToProductDTO(product);
    }

    public PageImpl<ProductDTO> findAllWithQueryAndPage(String query, List<Long> categoryId,Integer minPrice, Integer maxPrice,ProductStatus status, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> criteriaQuery = cb.createQuery(Product.class);
        Root<Product> root = criteriaQuery.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (query != null && !query.isEmpty()) {
            String likeSearchText = "%" + query.toLowerCase(Locale.US) + "%";
            Predicate searchByTitle = cb.like(cb.lower(root.get("title")), likeSearchText);
            Predicate searchByShortDesc = cb.like(cb.lower(root.get("shortDesc")), likeSearchText);
            predicates.add(cb.or(searchByTitle, searchByShortDesc));
        }

        if (categoryId != null && !categoryId.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categoryId));
        }

//        if (brandId != null && !brandId.isEmpty()) {
//            predicates.add(root.get("brand").get("id").in(brandId));
//        }

        if (minPrice != null && maxPrice != null){
            double doubleMin = (double) minPrice;
            double doubleMax = (double) maxPrice;
            predicates.add(cb.between(root.get("price"),doubleMin,doubleMax));
        }else {
            if (minPrice != null){
                double doubleMin = (double) minPrice;
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"),doubleMin));
            }
            if (maxPrice != null){
                double doubleMax = (double) maxPrice;
                predicates.add(cb.lessThanOrEqualTo(root.get("price"),doubleMax));
            }
        }
        try {
            Role role = roleService.findByRoleName(RoleType.ROLE_ADMIN);
            boolean isAdmin = userService.getCurrentUser().getRoles().stream().anyMatch(r->r.equals(role));
            if (isAdmin) {
                if (status != null){
                    predicates.add(cb.equal(root.get("status"),status));
                }
            }else throw new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND_MESSAGE);
        }catch(ResourceNotFoundException e){
                CategoryStatus cStatus = CategoryStatus.PUBLISHED;
                //BrandStatus bStatus = BrandStatus.PUBLISHED;
                ProductStatus pStatus = ProductStatus.PUBLISHED;
            predicates.add(cb.and(
                    cb.equal(root.get("status"), pStatus),
                    //cb.equal(root.get("brand").get("status"), bStatus),
                    cb.equal(root.get("category").get("status"), cStatus)
            ));
        }
        Predicate finalPredicate = cb.and(predicates.toArray(new Predicate[0]));

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

        TypedQuery<Product> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult((int)pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(Product.class)));
        countQuery.where(finalPredicate);
        Long totalRecords = entityManager.createQuery(countQuery).getSingleResult();

        List<ProductDTO> productDTOList = productMapper.productListToProductDTOList(typedQuery.getResultList());

        return new PageImpl<>(productDTOList, pageable, totalRecords);
    }

    public void save(Product product){
        productRepository.save(product);
    }

    public void removeAllBuiltInFalseProducts() {
        productRepository.deleteAllByBuiltInFalse();
    }

    public long countProductRecords() {
        return productRepository.count();
    }

    public void removeImageById(String id) {
        Product product = productRepository.findProductByImageId(id);
        imageFileService.removeById(id);
        boolean hasShowcase = false;
        if (product.getImage().size()>0){
            for (ImageFile each:product.getImage()) {
                if (each.isShowcase()){
                    hasShowcase = true;
                    break;
                }
            }
            if(!hasShowcase){
                ImageFile imageFile = product.getImage().stream().findFirst().orElse(null);
                imageFile.setShowcase(true);
            }
        }


    }
}
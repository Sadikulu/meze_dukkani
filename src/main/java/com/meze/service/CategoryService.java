package com.meze.service;

import com.meze.domains.Brand;
import com.meze.domains.Category;
import com.meze.domains.Role;
import com.meze.domains.User;
import com.meze.domains.enums.BrandStatus;
import com.meze.domains.enums.CategoryStatus;
import com.meze.domains.enums.RoleType;
import com.meze.dto.BrandDTO;
import com.meze.dto.CategoryDTO;
import com.meze.dto.request.CategoryRequest;
import com.meze.dto.request.CategoryUpdateRequest;
import com.meze.exception.BadRequestException;
import com.meze.exception.ConflictException;
import com.meze.exception.ResourceNotFoundException;
import com.meze.exception.message.ErrorMessage;
import com.meze.mapper.CategoryMapper;
import com.meze.repository.CategoryRepository;
import com.meze.repository.ProductRepository;
import com.meze.reusableMethods.NameFilter;
import lombok.RequiredArgsConstructor;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final RoleService roleService;
    private final CategoryMapper categoryMapper;
    private final EntityManager entityManager;
    private final UserService userService;
    private final NameFilter nameFilter;
    private final ProductRepository productRepository;


    public CategoryDTO saveCategory(CategoryRequest categoryRequest) {
        Category category=new Category();

        boolean existTitle= categoryRepository.existsByTitle(nameFilter.getNamesWithFilter(categoryRequest.getTitle()));
        if (existTitle) {
            throw new ConflictException(String.format(ErrorMessage.CATEGORY_USED_EXCEPTION,nameFilter.getNamesWithFilter(categoryRequest.getTitle())));
        }
        category.setTitle(nameFilter.getNamesWithFilter(categoryRequest.getTitle()));
        category.setStatus(CategoryStatus.NOT_PUBLISHED);
        categoryRepository.save(category);
        return categoryMapper.categoryToCategoryDTO(category);
    }

    public Page<CategoryDTO> findAllWithPage(String query, CategoryStatus status,Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Category> criteriaQuery = cb.createQuery(Category.class);
        Root<Category> root = criteriaQuery.from(Category.class);

        List<Predicate> predicates = new ArrayList<>();
        Predicate finalPredicate = null;

        if (query != null && !query.isEmpty()) {
            String likeSearchText = "%" + query.toLowerCase(Locale.US) + "%";
            System.err.println(likeSearchText);
            predicates.add(cb.like(cb.lower(root.get("title")), likeSearchText));
        }

        try{
            Role role=roleService.findByRoleName(RoleType.ROLE_ADMIN);
            boolean isAdmin=userService.getCurrentUser().getRoles().stream().anyMatch(r->r.equals(role));
            if (isAdmin) {
                if (status != null){
                    predicates.add(cb.equal(root.get("status"),status));
                }
            }else throw  new ResourceNotFoundException(ErrorMessage.BRAND_NOT_FOUND_MESSAGE);
        }catch(ResourceNotFoundException e){
            predicates.add(cb.equal(root.get("status"), CategoryStatus.PUBLISHED));
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

        TypedQuery<Category> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult((int)pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(Category.class)));
        countQuery.where(finalPredicate);
        Long totalRecords = entityManager.createQuery(countQuery).getSingleResult();

        List<CategoryDTO> categoryDTOList = categoryMapper.categoryListToCategoryDTOList(typedQuery.getResultList());

        return new PageImpl<>(categoryDTOList, pageable, totalRecords);
    }

    public CategoryDTO findCategoryById(Long id) {
        Category category = null;
        try{
            Role role=roleService.findByRoleName(RoleType.ROLE_ADMIN);
            boolean isAdmin=userService.getCurrentUser().getRoles().stream().anyMatch(r->r.equals(role));
            if (isAdmin) {
                category=categoryRepository.findById(id).orElseThrow(()->
                        new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND_MESSAGE)) ;;
            }else{
                throw  new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND_MESSAGE);
            }
        }catch(ResourceNotFoundException e){
            CategoryStatus status=CategoryStatus.PUBLISHED;
            category=categoryRepository.getCategoryByStatus_PublishedAndId(status,id).orElseThrow(()->
                    new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND_MESSAGE));
        }
        return categoryMapper.categoryToCategoryDTO(category);
    }

    public CategoryDTO updateCategory(Long id, CategoryUpdateRequest categoryUpdateRequest) {
        Category category=getCategoryById(id);
        if (category.getBuiltIn()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        boolean existsTitle= categoryRepository.existsByTitle(nameFilter.getNamesWithFilter(categoryUpdateRequest.getTitle()));

        if(existsTitle && ! categoryUpdateRequest.getTitle().equalsIgnoreCase(category.getTitle())) {
            throw new ConflictException(String.format(ErrorMessage.CATEGORY_USED_EXCEPTION,nameFilter.getNamesWithFilter(categoryUpdateRequest.getTitle())));}

        category.setTitle(nameFilter.getNamesWithFilter(categoryUpdateRequest.getTitle()));
        category.setStatus(categoryUpdateRequest.getStatus());
        category.setUpdateAt(LocalDateTime.now());
        categoryRepository.save(category);
        return categoryMapper.categoryToCategoryDTO(category);
    }

    public CategoryDTO removeById(Long id) {
        Category category=getCategoryById(id);
        if (category.getBuiltIn()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        Boolean existsProduct= productRepository.existsByCategoryId(id);

        if(existsProduct){
            throw new BadRequestException(ErrorMessage.CATEGORY_CAN_NOT_DELETE_EXCEPTION);
        }
        CategoryDTO categoryDTO=categoryMapper.categoryToCategoryDTO(category);
        categoryRepository.delete(category);
        return categoryDTO;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));
    }

    public List<CategoryDTO> getAllCategoryList() {
        List<Category> categoryList = categoryRepository.findAll();
        return categoryMapper.categoryListToCategoryDTOList(categoryList);
    }

    public void removeAllBuiltInFalseCategories() {
        categoryRepository.deleteAllByBuiltInFalse();
    }

    public long countCategoryRecords() {
        return categoryRepository.count();
    }
}

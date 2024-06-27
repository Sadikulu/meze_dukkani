package com.meze.service;

import com.meze.domains.Brand;
import com.meze.domains.ImageFile;
import com.meze.domains.Role;
import com.meze.domains.enums.BrandStatus;
import com.meze.domains.enums.RoleType;
import com.meze.dto.BrandDTO;
import com.meze.dto.request.BrandRequest;
import com.meze.dto.request.BrandUpdateRequest;
import com.meze.exception.BadRequestException;
import com.meze.exception.ConflictException;
import com.meze.exception.ResourceNotFoundException;
import com.meze.exception.message.ErrorMessage;
import com.meze.mapper.BrandMapper;
import com.meze.repository.BrandRepository;
import com.meze.repository.ImageFileRepository;
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

@RequiredArgsConstructor
@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final EntityManager entityManager;
    private final ImageFileService imageFileService;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final RoleService roleService;
    private final NameFilter nameFilter;
    private final ImageFileRepository imageFileRepository;


    public PageImpl<BrandDTO> getAllBrandsByPage(String query,BrandStatus status,Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Brand> criteriaQuery = cb.createQuery(Brand.class);
        Root<Brand> root = criteriaQuery.from(Brand.class);

        List<Predicate> predicates = new ArrayList<>();
        Predicate finalPredicate = null;

        if (query != null && !query.isEmpty()) {
            String likeSearchText = "%" + query.toLowerCase(Locale.US) + "%";
            predicates.add(cb.like(cb.lower(root.get("name")), likeSearchText));
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
            predicates.add(cb.equal(root.get("status"), BrandStatus.PUBLISHED));
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

        TypedQuery<Brand> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult((int)pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(Brand.class)));
        countQuery.where(finalPredicate);
        Long totalRecords = entityManager.createQuery(countQuery).getSingleResult();

        List<BrandDTO> brandDTOList = brandMapper.brandListToBrandDTOList(typedQuery.getResultList());

        return new PageImpl<>(brandDTOList, pageable, totalRecords);
    }

    public BrandDTO getBrandById(Long id) {

        Brand brand=null;

        try{
            Role role=roleService.findByRoleName(RoleType.ROLE_ADMIN);
            boolean isAdmin=userService.getCurrentUser().getRoles().stream().anyMatch(r->r.equals(role));
            if (isAdmin) {
                brand=findBrandById(id);
            }else{
                throw  new ResourceNotFoundException(ErrorMessage.BRAND_NOT_FOUND_MESSAGE);
            }
        }catch(ResourceNotFoundException e){
            BrandStatus status=BrandStatus.PUBLISHED;
            brand=brandRepository.getBrandByStatus_PublishedAndId(status,id).orElseThrow(()->
                    new ResourceNotFoundException(ErrorMessage.BRAND_NOT_FOUND_MESSAGE)) ;
        }

        return brandMapper.brandToBrandDTO(brand);
    }


    public BrandDTO createBrand(String imageId, BrandRequest brandRequest) {
        ImageFile imageFile=imageFileService.getImageById(imageId);

        Integer usedBrandCount=brandRepository.findBrandByImage_Id(imageId);
        if(usedBrandCount>0){
            throw new ConflictException(ErrorMessage.IMAGE_USED_MESSAGE);
        }

      boolean existBrand= brandRepository.existsByName(nameFilter.getNamesWithFilter(brandRequest.getName()));
      if (existBrand) {
          throw new ConflictException(String.format(ErrorMessage.BRAND_CONFLICT_EXCEPTION,nameFilter.getNamesWithFilter(brandRequest.getName())));
      }


        Brand brand=brandMapper.brandRequestToBrand(brandRequest);
        brand.setBuiltIn(false);
        brand.setStatus(BrandStatus.NOT_PUBLISHED);
        brand.setName(nameFilter.getNamesWithFilter(brandRequest.getName()));
        brand.setImage(imageFile);
        brandRepository.save(brand);
        return brandMapper.brandToBrandDTO(brand);
    }

    public BrandDTO deleteBrandById(Long id) {
        Brand brand=findBrandById(id);
        if(brand.getBuiltIn()){
            throw  new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        Boolean existsProduct=productRepository.existsByBrandId(id);

        if(existsProduct){
            throw  new BadRequestException(ErrorMessage.BRAND_CAN_NOT_DELETE_EXCEPTION);
        }

        BrandDTO brandDTO=getBrandById(id);
        brandRepository.delete(brand);
        return brandDTO;
    }

    public BrandDTO updateBrand(Long id, BrandUpdateRequest brandUpdateRequest) {
        Brand brand=findBrandById(id);
        if (!brand.getImage().getId().equals(brandUpdateRequest.getImage())){
            ImageFile tempImageFile = brand.getImage();
            ImageFile imageFile = imageFileService.getImageById(brandUpdateRequest.getImage());
            brand.setImage(imageFile);
            imageFileRepository.delete(tempImageFile);
        }

        boolean brandNameExist  = brandRepository.existsByName(nameFilter.getNamesWithFilter(brandUpdateRequest.getName()));

        if(brandNameExist && ! brandUpdateRequest.getName().equalsIgnoreCase(brand.getName())) {
            throw new ConflictException(String.format(ErrorMessage.BRAND_CONFLICT_EXCEPTION,nameFilter.getNamesWithFilter(brandUpdateRequest.getName())));}

        boolean imageExists=brandRepository.existsByImageId(brandUpdateRequest.getImage());


        if(imageExists && ! brandUpdateRequest.getImage().equalsIgnoreCase(brand.getImage().getId())) {
            throw new ConflictException(String.format(ErrorMessage.IMAGE_ALREADY_EXIST_MESSAGE,brandUpdateRequest.getImage()));}


        if(brand.getBuiltIn()){
            throw  new BadRequestException(ErrorMessage.BRAND_CAN_NOT_UPDATE_EXCEPTION);
        }
        brand.setName(nameFilter.getNamesWithFilter(brandUpdateRequest.getName()));
        brand.setStatus(brandUpdateRequest.getStatus());
        brand.setUpdateAt(LocalDateTime.now());

        BrandDTO brandDTO=brandMapper.brandToBrandDTO(brand);
        brandRepository.save(brand);

        return brandDTO;
    }

    public Brand findBrandById(Long id){

        return brandRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessage.BRAND_NOT_FOUND_MESSAGE,id)));
    }


    public List<Brand> getAllBrands(){
        return brandRepository.findAll();
    }

    public List<BrandDTO> getAllBrandList(){
        List<Brand> brandList = brandRepository.findAll();
        return brandMapper.brandListToBrandDTOList(brandList);
    }

    public Page<BrandDTO> getAllBrandsByPageNoAuth(Pageable pageable) {
        BrandStatus status=BrandStatus.PUBLISHED;
        Page<Brand> brandPage=brandRepository.findAllByStatus(pageable,status).orElseThrow(()->
                new ResourceNotFoundException(ErrorMessage.BRAND_NOT_FOUND_MESSAGE)) ;;
        return brandPage.map(brandMapper::brandToBrandDTO);
    }

    public BrandDTO getBrandByIdNoAuth(Long id) {
        BrandStatus status=BrandStatus.PUBLISHED;
        Brand brand=brandRepository.getBrandByStatus_PublishedAndId(status,id).orElseThrow(()->
                new ResourceNotFoundException(ErrorMessage.BRAND_NOT_FOUND_MESSAGE)) ; ;
        return brandMapper.brandToBrandDTO(brand);
    }

    public void removeAllBuiltInFalseBrands() {
        brandRepository.deleteAllByBuiltInFalse();
    }

    public long countBrandRecords() {
        return brandRepository.count();
    }
}

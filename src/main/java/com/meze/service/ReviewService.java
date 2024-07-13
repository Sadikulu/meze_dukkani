package com.meze.service;

import com.meze.domains.*;
import com.meze.domains.enums.*;
import com.meze.dto.ReviewDTO;
import com.meze.dto.request.ReviewRequest;
import com.meze.dto.request.ReviewUpdateRequest;
import com.meze.exception.ResourceNotFoundException;
import com.meze.exception.message.ErrorMessage;
import com.meze.mapper.ReviewMapper;
import com.meze.repository.OrderRepository;
import com.meze.repository.ReviewRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final UserService userService;
    private final EntityManager entityManager;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final RoleService roleService;


    /*----------- CREATE A REVIEW ------------*/

    public ReviewDTO saveReview(ReviewRequest reviewRequest) {
        User user = userService.getCurrentUser();
        Product product=productService.findProductById(reviewRequest.getProductId());
        Boolean isOrderExists = orderRepository.existsByUserIdAndProductId(user.getId(),product.getId());
        Review review = null;
        if (isOrderExists){
            review = new Review();
            review.setContent(reviewRequest.getContent());
            review.setRating(reviewRequest.getRating());
            review.setStatus(ReviewStatus.NOT_PUBLISHED);
            review.setUser(user);
            review.setProduct(product);
            reviewRepository.save(review);
        }else{
            throw new RuntimeException(ErrorMessage.REVIEW_IS_NOT_POSSIBLE_MESSAGE);
        }
        return  reviewMapper.reviewToReviewDTO(review);
    }

    /*----------- GET ALL REVIEWS ------------*/

    public PageImpl<ReviewDTO> getAllReviewsWithFilter(String query,Byte rate,ReviewStatus status,Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Review> criteriaQuery = cb.createQuery(Review.class);
        Root<Review> root = criteriaQuery.from(Review.class);

        List<Predicate> predicates = new ArrayList<>();
        Predicate finalPredicate = null;

        if (query != null && !query.isEmpty()) {
            String likeSearchText = "%" + query.toLowerCase(Locale.US) + "%";
            Predicate searchByTitle = cb.like(cb.lower(root.get("content")), likeSearchText);
            Predicate searchByUserFirstName = cb.like(cb.lower(root.get("user").get("firstName")), likeSearchText);
            Predicate searchByUserLastName = cb.like(cb.lower(root.get("user").get("lastName")), likeSearchText);
            Predicate searchByProductTitle = cb.like(cb.lower(root.get("product").get("title")), likeSearchText);
            predicates.add(cb.or(searchByTitle, searchByUserFirstName,searchByUserLastName,searchByProductTitle));
        }
        if (rate != null && (rate > 0 && rate <= 5)) {
            predicates.add(cb.equal(root.get("rating"),(rate)));
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
            predicates.add(cb.equal(root.get("status"),ReviewStatus.PUBLISHED));
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

        TypedQuery<Review> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult((int)pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(Review.class)));
        countQuery.where(finalPredicate);
        Long totalRecords = entityManager.createQuery(countQuery).getSingleResult();

        List<ReviewDTO> reviewDTOList = reviewMapper.map(typedQuery.getResultList());

        return new PageImpl<>(reviewDTOList, pageable, totalRecords);
    }

    /*----------- GET A SINGLE REVIEW ------------*/

    public ReviewDTO findReviewById(Long id) {
        Review review = reviewRepository.findById(id).orElseThrow(()->new
                ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));;
        return reviewMapper.reviewToReviewDTO(review);
    }

    /*----------- GET REVIEWS PRODUCT ID ------------*/

    public Page<ReviewDTO> findReviewByProductId(Long productId, Pageable pageable) {
        Page<Review> review = reviewRepository.findReviewByProductId(productId,pageable);
        return review.map(reviewMapper::reviewToReviewDTO);
    }


    /*----------- GET AUTH USERS REVIEWS ------------*/

    public Page<ReviewDTO> findAllUsersReviews(Pageable pageable) {
        User user = userService.getCurrentUser();
        Page<Review> usersReviewPage = reviewRepository.findAllReviewByUser(user, pageable);

        return usersReviewPage.map(reviewMapper::reviewToReviewDTO);
    }

    /*----------- GET AUTH USER SINGLE REVIEW ------------*/


    public ReviewDTO findByIdAndUser(Long id) {
        User user = userService.getCurrentUser();
        Review review =  reviewRepository.findByIdAndUser(id, user).orElseThrow(()->new ResourceNotFoundException(
                String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));

        return reviewMapper.reviewToReviewDTO(review);
    }

    /*----------- GET USER ALL REVIEWS ------------*/
    public Page<ReviewDTO> findUserAllReviews(Long userId,Pageable pageable) {
        User user = userService.getById(userId);
        Page<Review> userAllReviewPage=reviewRepository.findReviewByUserId(user.getId(), pageable);

        return userAllReviewPage.map(reviewMapper::reviewToReviewDTO);
    }

    /*----------- UPDATE REVIEW ------------*/
    public ReviewDTO updateReviewById(Long id, ReviewUpdateRequest reviewUpdateRequest){
        Review review = reviewRepository.findById(id).orElseThrow(()-> new
                ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE,id)));


        review.setStatus(reviewUpdateRequest.getStatus());

        reviewRepository.save(review);
        return reviewMapper.reviewToReviewDTO(review);



    }




    /*----------- DELETE REVIEW ------------*/

    public ReviewDTO deleteReviewById(Long id) {
        ReviewDTO reviewDTO=findReviewById(id);
        reviewRepository.deleteById(id);
        return  reviewDTO;

    }

    public long countReviewRecords() {
        return reviewRepository.count();
    }
}

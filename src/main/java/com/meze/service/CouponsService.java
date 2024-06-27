package com.meze.service;

import com.meze.domains.Coupons;
import com.meze.domains.User;
import com.meze.domains.enums.CouponsStatus;
import com.meze.dto.CouponDTO;
import com.meze.dto.request.CouponMailRequest;
import com.meze.dto.request.CouponRequest;
import com.meze.dto.request.CouponUpdateRequest;
import com.meze.exception.CouponNotValidException;
import com.meze.exception.ResourceNotFoundException;
import com.meze.exception.message.ErrorMessage;
import com.meze.mapper.CouponsMapper;
import com.meze.repository.CouponsRepository;
import com.meze.reusableMethods.UniqueIdGenerator;
import com.meze.service.email.EmailSender;
import com.meze.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponsService {

    private final UniqueIdGenerator uniqueIdGenerator;
    private final CouponsRepository couponsRepository;
    private final CouponsMapper couponsMapper;
    private final UserService userService;
    private final EmailSender emailSender;
    private final EmailService emailService;



    public Coupons getCoupon(Long id) {
        return couponsRepository.findById(id).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id))
        );
    }

    public Coupons getCouponByCouponCode(String couponCode) {
        return couponsRepository.findByCouponCode(couponCode).orElse(null);
    }

    public CouponDTO findCoupon(Long id) {
        return couponsMapper.couponsToDto(getCoupon(id));
    }

    public CouponDTO findCouponByCouponCode(String couponCode) {
        Coupons coupon = getCouponByCouponCode(couponCode);
        if (!((coupon.getLife() == -1 || coupon.getLife() > 0) && coupon.getStatus() == CouponsStatus.ACTIVE)){
            throw new CouponNotValidException(String.format(ErrorMessage.COUPON_NOT_VALID_MESSAGE,couponCode));
        }
        return couponsMapper.couponsToDto(coupon);
    }

    public Page<CouponDTO> getAllCouponsWithPage(Pageable pageable) {
        Page<Coupons> couponsPage = couponsRepository.findAll(pageable);
        return couponsPage.map(couponsMapper::couponsToDto);
    }

    public List<CouponDTO> getAllCouponsDTO(){
        List<Coupons> coupons = couponsRepository.findAll();
        return couponsMapper.map(coupons);
    }

    public List<Coupons> getAllCoupons(){
        return couponsRepository.findAll();
    }

    public CouponDTO saveCoupon(CouponRequest couponRequest) {
        Coupons coupon = couponsMapper.couponsRequestToCoupons(couponRequest);
        coupon.setCode(uniqueIdGenerator.generateUniqueId(12));
        couponsRepository.save(coupon);
        return couponsMapper.couponsToDto(coupon);
    }

    public CouponDTO updateCoupon(Long id, CouponUpdateRequest couponUpdateRequest) {
        Coupons foundCoupon = getCoupon(id);
        foundCoupon.setAmount(couponUpdateRequest.getAmount());
        foundCoupon.setType(couponUpdateRequest.getType());
        foundCoupon.setLife(couponUpdateRequest.getLife());
        foundCoupon.setStatus(couponUpdateRequest.getStatus());
        foundCoupon.setUpdatedAt(LocalDateTime.now());

        couponsRepository.save(foundCoupon);
        return couponsMapper.couponsToDto(foundCoupon);
    }

    public void deleteCoupon(Long id) {
        Coupons foundCoupon = getCoupon(id);
        couponsRepository.delete(foundCoupon);
    }

    public void removeAll() {
        couponsRepository.deleteAll();
    }

    public long countCouponRecords() {
        return couponsRepository.count();
    }

    public void sendCoupon(CouponMailRequest couponMailRequest) {
        List<User> userList = new ArrayList<>();
        Coupons coupon = getCoupon(couponMailRequest.getCouponId());
        for (Long each:couponMailRequest.getUserId()) {
            User user = userService.getById(each);
            userList.add(user);
        }
        for (User each:userList) {
            emailSender.send(
                    each.getEmail(),
                    emailService.buildCouponMail(each.getFirstName(),coupon,couponMailRequest.getMessage())
            );
        }
    }
}

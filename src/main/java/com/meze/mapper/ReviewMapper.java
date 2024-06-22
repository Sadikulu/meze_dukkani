package com.meze.mapper;

import com.meze.domains.Review;
import com.meze.domains.User;
import com.meze.dto.ReviewDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {


   @Mapping(source = "user", target = "userFullName",qualifiedByName = "getUserFullName")
   ReviewDTO reviewToReviewDTO(Review review);
   List<ReviewDTO> map(List<Review> reviewList);

   @Named("getUserFullName")
   public static String getUserFullName(User user){
      return user.getFirstName()+" "+user.getLastName();
   }
}

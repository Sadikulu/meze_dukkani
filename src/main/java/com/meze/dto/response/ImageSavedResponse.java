package com.meze.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ImageSavedResponse extends GPMResponse{
    private Set<String> imageId;

   public ImageSavedResponse(Set<String> imageId,String message,boolean success) {
       super(message, success);
    this.imageId=imageId;

   }


}

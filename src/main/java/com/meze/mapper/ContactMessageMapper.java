package com.meze.mapper;

import com.meze.domains.ContactMessage;
import com.meze.dto.ContactMessageDTO;
import com.meze.dto.request.ContactMessageRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContactMessageMapper  {

  ContactMessageDTO contactMessageToDTO(ContactMessage contactMessage);

  @Mapping(target = "id", ignore = true)
  ContactMessage contactMessageRequestToContactMessage(ContactMessageRequest contactMessageRequest);

  //getAllContactMessage()
  List<ContactMessageDTO> map (List<ContactMessage> contactMessageList);




}

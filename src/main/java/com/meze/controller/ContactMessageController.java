package com.meze.controller;

import com.meze.dto.ContactMessageDTO;
import com.meze.dto.request.ContactMessageRequest;
import com.meze.dto.response.GPMResponse;
import com.meze.dto.response.ResponseMessage;
import com.meze.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/contact-message")
@RequiredArgsConstructor
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping("visitors")
    public ResponseEntity<GPMResponse> createMessage
            (@Valid @RequestBody ContactMessageRequest contactMessageRequest) {
        ContactMessageDTO contactMessageDTO = contactMessageService.saveMessage(contactMessageRequest);
        GPMResponse response = new GPMResponse(ResponseMessage.CONTACTMESSAGE_CREATE_RESPONSE,
                true, contactMessageDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ContactMessageDTO>> getAllContactMessage() {
        List<ContactMessageDTO> contactMessageDTOList = contactMessageService.getAllMessage();
        return ResponseEntity.ok(contactMessageDTOList);
    }

    @GetMapping("/pages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ContactMessageDTO>> getAllContactMessageWithPage(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String prop,
            @RequestParam(value = "direction", required = false, defaultValue = "DESC")
            Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, prop));
        Page<ContactMessageDTO> pageDTO = contactMessageService.getAllMessage(pageable);
        return ResponseEntity.ok(pageDTO);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ContactMessageDTO> getMessageById(@PathVariable("id") Long id) {
        ContactMessageDTO contactMessageDTO = contactMessageService.getMessageById(id);
        return ResponseEntity.ok(contactMessageDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GPMResponse>
    updatedContactMessage(@PathVariable Long id, @Valid
    @RequestBody ContactMessageRequest contactMessageRequest) {
        ContactMessageDTO contactMessageDTO = contactMessageService.updateMessage(id, contactMessageRequest);
        GPMResponse response = new GPMResponse
                (ResponseMessage.CONTACTMESSAGE_UPDATED_RESPONSE, true, contactMessageDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GPMResponse> deleteContactMessage(@PathVariable Long id) {
        ContactMessageDTO contactMessageDTO = contactMessageService.deleteContactMessage(id);
        GPMResponse response =
                new GPMResponse(ResponseMessage.CONTACTMESSAGE_DELETE_RESPONSE, true, contactMessageDTO);
        return ResponseEntity.ok(response);
    }
}
























package com.meze.controller;

import com.meze.dto.UserAddressDTO;
import com.meze.dto.request.UserAddressRequest;
import com.meze.dto.request.UserAddressUpdate;
import com.meze.dto.response.GPMResponse;
import com.meze.dto.response.ResponseMessage;
import com.meze.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/user-addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping("/auth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<List<UserAddressDTO>> getAllAddress(){
        List<UserAddressDTO> userAddressDTOS=userAddressService.getAllAddresses();
        return ResponseEntity.ok(userAddressDTOS);
    }

    @GetMapping("/{id}/auth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<UserAddressDTO> getAddressById(@PathVariable("id") Long id){
        UserAddressDTO userAddressDTO=userAddressService.getAddressesById(id);
        return ResponseEntity.ok(userAddressDTO);
    }

    @PostMapping("/auth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<GPMResponse> saveUserAddress(@Valid @RequestBody UserAddressRequest userAddressRequest) {
        UserAddressDTO userAddressDTO= userAddressService.saveAddress(userAddressRequest);
        GPMResponse response = new GPMResponse(ResponseMessage.USER_ADDRESS_CREATED_RESPONSE_MESSAGE, true,userAddressDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/auth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<UserAddressDTO> authUpdateAddress(@PathVariable("id") Long id, @Valid @RequestBody UserAddressUpdate userAddressUpdate){
        UserAddressDTO userAddressDTO=userAddressService.authUpdateAddress(id,userAddressUpdate);
        return ResponseEntity.ok(userAddressDTO);
    }

    @DeleteMapping("/{id}/auth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<GPMResponse> deleteAuthUserAddress(@PathVariable("id") Long id){
        userAddressService.removeUserAddressById(id);
        GPMResponse response = new GPMResponse(ResponseMessage.USER_ADDRESS_DELETE_RESPONSE_MESSAGE, true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<UserAddressDTO>> getAllAddressesByUserId(@PathVariable("userId") Long userId){
        List<UserAddressDTO> userAddressDTOS=userAddressService.getAllAddressesByUserId(userId);
        return ResponseEntity.ok(userAddressDTOS);
    }

    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserAddressDTO> adminGetAddressById(@PathVariable("id") Long id){
        UserAddressDTO userAddressDTO=userAddressService.adminGetAddressById(id);
        return ResponseEntity.ok(userAddressDTO);
    }

    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserAddressDTO> adminUpdateAddress(@PathVariable("id") Long id, @Valid @RequestBody UserAddressUpdate userAddressUpdate){
        UserAddressDTO userAddressDTO=userAddressService.adminUpdateAddress(id,userAddressUpdate);
        return ResponseEntity.ok(userAddressDTO);
    }

    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<GPMResponse> deleteAdminUserAddress(@PathVariable("id") Long id){
        userAddressService.removeAdminUserAddressById(id);
        GPMResponse response = new GPMResponse(ResponseMessage.USER_ADDRESS_DELETE_RESPONSE_MESSAGE, true);
        return ResponseEntity.ok(response);
    }
}

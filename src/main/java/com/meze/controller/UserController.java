package com.meze.controller;

import com.meze.domains.enums.RoleType;
import com.meze.dto.FavoriteProductDTO;
import com.meze.dto.UserDTO;
import com.meze.dto.UserDeleteDTO;
import com.meze.dto.request.AdminUserUpdateRequest;
import com.meze.dto.request.PasswordUpdateRequest;
import com.meze.dto.request.UserUpdateRequest;
import com.meze.dto.response.GPMResponse;
import com.meze.dto.response.ResponseMessage;
import com.meze.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/auth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<UserDTO> getUser() {
        UserDTO userDTO = userService.getPrincipal();
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/auth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<GPMResponse> updateUser(@Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        UserDTO userDTO= userService.updateUser(userUpdateRequest);
        GPMResponse response = new GPMResponse(ResponseMessage.USER_UPDATE_RESPONSE_MESSAGE, true,userDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth/favorites")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<List<FavoriteProductDTO>> getAuthFavorites(){
        List<FavoriteProductDTO> favoriteProductDTOList = userService.getAuthFavorites();
        return ResponseEntity.ok(favoriteProductDTOList);
    }

    @DeleteMapping ("/auth/favorites")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<GPMResponse> deleteAuthFavorites(){
        userService.deleteAuthFavorites();
        GPMResponse response = new GPMResponse(ResponseMessage.FAVORITE_LIST_CLEAN_MESSAGE,true);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/auth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<GPMResponse> updatePassword(@Valid @RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        userService.updatePassword(passwordUpdateRequest);
        GPMResponse response = new GPMResponse(ResponseMessage.PASSWORD_CHANGED_RESPONSE_MESSAGE, true);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/auth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER') or hasRole('MANAGER')")
    public ResponseEntity<GPMResponse> deleteUser(){
        UserDeleteDTO userDeleteDTO = userService.removeUserById();
        GPMResponse response = new GPMResponse(ResponseMessage.USER_DELETE_RESPONSE_MESSAGE, true, userDeleteDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pages")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PageImpl<UserDTO>> getAllUsersByPage(@RequestParam(value = "q",required = false)String query,
                                                               @RequestParam(value = "role",required = false) RoleType role,
                                                               @RequestParam("page") int page,
                                                               @RequestParam("size") int size, @RequestParam("sort") String prop,
                                                               @RequestParam(value = "direction", required = false, defaultValue = "DESC") Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, prop));
            PageImpl<UserDTO> userDTOPage = userService.getAllUserPage(query,role,pageable);
        return ResponseEntity.ok(userDTOPage);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam(value = "q",required = false)String query,
                                                               @RequestParam(value = "role",required = false)RoleType role,
                                                               @RequestParam(value = "birthday",required = false)boolean birthday,
                                                               @RequestParam(value = "anniversary",required = false)boolean anniversary) {
        List<UserDTO> userDTO = userService.getAllUser(query,role,birthday,anniversary);
        return ResponseEntity.ok(userDTO);
    }

     //getUserById
    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<GPMResponse> updateUserAuth(@PathVariable("id") Long id,
                                                     @Valid @RequestBody AdminUserUpdateRequest adminUserUpdateRequest) {
        UserDTO userDTO= userService.updateUserAuth(id,adminUserUpdateRequest);
        GPMResponse response = new GPMResponse(ResponseMessage.USER_UPDATE_RESPONSE_MESSAGE, true,userDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<GPMResponse> deleteUser(@PathVariable Long id){
        UserDeleteDTO userDeleteDTO= userService.adminRemoveUserById(id);
        GPMResponse response = new GPMResponse(ResponseMessage.USER_DELETE_RESPONSE_MESSAGE, true,userDeleteDTO);
        return ResponseEntity.ok(response);
    }
}

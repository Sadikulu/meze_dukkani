package com.meze.controller;

import com.meze.dto.UserDTO;
import com.meze.dto.request.ForgotPasswordRequest;
import com.meze.dto.request.LoginRequest;
import com.meze.dto.request.PasswordResetRequest;
import com.meze.dto.request.RegisterRequest;
import com.meze.dto.response.GPMResponse;
import com.meze.dto.response.LoginResponse;
import com.meze.dto.response.ResponseMessage;
import com.meze.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class UserJwtController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<GPMResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest)  {
        String token = userService.saveUser(registerRequest);
        GPMResponse response = new GPMResponse(ResponseMessage.REGISTER_RESPONSE_MESSAGE,true,token);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @GetMapping(path = "confirm")
    public ResponseEntity<GPMResponse> confirm(@RequestParam("token") String token) {
        UserDTO userDTO = userService.confirmToken(token);
        GPMResponse response = new GPMResponse(ResponseMessage.ACCOUNT_CONFIRMED_RESPONSE,true,userDTO);
        return ResponseEntity.ok(response);
    }

    // login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(//@RequestHeader(value = "cartUUID",required = false)String cartUUID,
                                                      @Valid @RequestBody LoginRequest loginRequest)  {
        LoginResponse response = userService.loginUser(//cartUUID,
                loginRequest);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GPMResponse> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest){
        String message = userService.forgotPassword(forgotPasswordRequest);
        GPMResponse response = new GPMResponse(message,true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GPMResponse> resetPassword(@RequestParam("token") String token, @Valid @RequestBody PasswordResetRequest passwordResetRequest)  {
        String message = userService.confirmResetToken(token,passwordResetRequest);
        GPMResponse response = new GPMResponse(message,true);
        return ResponseEntity.ok(response);
    }
}

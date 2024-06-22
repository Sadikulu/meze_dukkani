package com.meze.controller;

import com.meze.dto.DashboardCountDTO;
import com.meze.dto.response.GPMResponse;
import com.meze.dto.response.ResponseMessage;
import com.meze.service.DatabaseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/database")
public class DatabaseController {

    private final DatabaseService databaseService;

    @Operation(summary = "Delete all database records except built-in true", description = "This endpoint going to reset all data in database which built-in status is false")
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GPMResponse> deleteAllData(){
        databaseService.resetAll();
        GPMResponse response = new GPMResponse(ResponseMessage.DATABASE_RESET_RESPONSE,true,null);
        return  ResponseEntity.ok(response);
    }

    @Operation(summary = "Get count of all sections for dashboard")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<GPMResponse> getCountOfAllRecords(){
        DashboardCountDTO dashboardCountDTO = databaseService.getCountOfAllRecords();
        GPMResponse response = new GPMResponse(ResponseMessage.COUNT_OF_ALL_RECORDS_RESPONSE,true,dashboardCountDTO);
        return ResponseEntity.ok(response);
    }

}

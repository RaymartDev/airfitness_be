package com.airfitness.airfitness.service.interfaces;

import com.airfitness.airfitness.common.util.ApiResponse;
import com.airfitness.airfitness.dto.AccessDto;
import com.airfitness.airfitness.dto.LoginDto;
import com.airfitness.airfitness.dto.RegisterDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface IAuthService {
    ResponseEntity<ApiResponse<AccessDto>> register(@RequestBody RegisterDto dto, HttpServletResponse response);
    void logout(HttpServletRequest request, HttpServletResponse response);
    ResponseEntity<ApiResponse<AccessDto>> refresh(HttpServletRequest req);
    ResponseEntity<ApiResponse<AccessDto>> login(@RequestBody LoginDto dto, HttpServletResponse response);
}

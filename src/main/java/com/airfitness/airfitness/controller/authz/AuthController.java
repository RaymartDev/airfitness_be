package com.airfitness.airfitness.controller.authz;

import com.airfitness.airfitness.common.util.ApiResponse;
import com.airfitness.airfitness.dto.AccessDto;
import com.airfitness.airfitness.dto.LoginDto;
import com.airfitness.airfitness.dto.RegisterDto;
import com.airfitness.airfitness.repository.OrganizationRepository;
import com.airfitness.airfitness.repository.OrganizationUserRepository;
import com.airfitness.airfitness.repository.UserRepository;
import com.airfitness.airfitness.common.util.JwtService;
import com.airfitness.airfitness.service.RefreshTokenService;
import com.airfitness.airfitness.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API for Authentication Control")
@RequiredArgsConstructor
public class AuthController {
    private final PasswordEncoder encoder;
    private final OrganizationUserRepository orgUserRepo;
    private final OrganizationRepository orgRepo;
    private final UserRepository userRepo;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthServiceImpl authService;

    @PostMapping("/register")
    @Transactional
    @Operation(summary = "Registration", description = "Creates a new user")
    public ResponseEntity<ApiResponse<AccessDto>> register(@RequestBody RegisterDto dto, HttpServletResponse response) {
        return authService.register(dto, response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke user access")
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        authService.logout(req, res);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Refresh user's refresh token")
    public ResponseEntity<ApiResponse<AccessDto>> refreshToken(HttpServletRequest req) {
        return authService.refresh(req);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AccessDto>> login(@RequestBody LoginDto dto, HttpServletResponse res) {
        return authService.login(dto, res);
    }
}

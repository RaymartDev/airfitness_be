package com.airfitness.airfitness.controller.authz;

import com.airfitness.airfitness.common.exception.UnauthorizedException;
import com.airfitness.airfitness.common.user_details.UserDetailsImpl;
import com.airfitness.airfitness.common.util.ApiResponse;
import com.airfitness.airfitness.common.util.CookieUtil;
import com.airfitness.airfitness.dto.LoginDto;
import com.airfitness.airfitness.dto.RegisterDto;
import com.airfitness.airfitness.entity.Organization;
import com.airfitness.airfitness.entity.OrganizationUser;
import com.airfitness.airfitness.entity.RefreshToken;
import com.airfitness.airfitness.entity.User;
import com.airfitness.airfitness.repository.OrganizationRepository;
import com.airfitness.airfitness.repository.OrganizationUserRepository;
import com.airfitness.airfitness.repository.UserRepository;
import com.airfitness.airfitness.common.util.JwtService;
import com.airfitness.airfitness.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @PostMapping("/register")
    @Transactional
    @Operation(summary = "Registration", description = "Creates a new user")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@RequestBody RegisterDto dto, HttpServletResponse response) {

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(encoder.encode(dto.password()));
        userRepo.save(user);

        Organization org = new Organization();
        org.setName(dto.gymName());
        orgRepo.save(org);

        OrganizationUser ou = new OrganizationUser();
        ou.setUser(user);
        ou.setOrganization(org);
        ou.setRole("OWNER");
        orgUserRepo.save(ou);

        String access = jwtService.generateAccessToken(
                user.getId(), org.getId(), "OWNER");

        String refresh = refreshTokenService.create(user, org);

        CookieUtil.addRefreshCookie(response, refresh);

        return ResponseEntity.status(201)
                .body(ApiResponse.ok("Login Successful",
                        Map.of("accessToken", access)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke user access")
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        String raw = CookieUtil.get(req, "refreshToken");
        refreshTokenService.revokeByRaw(raw);
        CookieUtil.clear(res, "refreshToken");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Refresh user's refresh token")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(HttpServletRequest req) {

        String raw = CookieUtil.get(req, "refreshToken");

        Claims claims = jwtService.parseExpired(req.getHeader("Authorization"));

        Long userId = Long.valueOf(claims.getSubject());
        Long orgId = Long.valueOf(claims.get("org", String.class));

        RefreshToken token = refreshTokenService.validate(userId, raw);

        OrganizationUser ou =
                orgUserRepo.findByUserIdAndOrganizationId(userId, orgId)
                        .orElseThrow(UnauthorizedException::new);

        String newAccess = jwtService.generateAccessToken(
                userId, orgId, ou.getRole());

        return ResponseEntity.status(200)
                .body(ApiResponse.ok(
                        "Successfully generated refresh token",
                        Map.of("accessToken", newAccess)
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody LoginDto dto, HttpServletResponse res) {

        // auth
        Authentication authentication =  authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // fetch
        OrganizationUser ou = orgUserRepo.findFirstByUserId(userDetails.getId())
                .orElseThrow(() -> new UnauthorizedException("User has no organization"));

        // generate access
        String accessToken = jwtService.generateAccessToken(
                userDetails.getId(),
                ou.getOrganization().getId(),
                ou.getRole()
        );

        // refresh token
        String refreshToken = refreshTokenService.create(userDetails.getUser(), ou.getOrganization());

        // add refreshtoken
        CookieUtil.addRefreshCookie(res, accessToken);

        // access token
        return ResponseEntity.status(200)
                .body(ApiResponse.ok("Login Successful", Map.of("accessToken", accessToken)));
    }
}

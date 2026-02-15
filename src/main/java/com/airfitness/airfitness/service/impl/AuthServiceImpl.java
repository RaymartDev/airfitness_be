package com.airfitness.airfitness.service.impl;

import com.airfitness.airfitness.common.exception.UnauthorizedException;
import com.airfitness.airfitness.common.user_details.UserDetailsImpl;
import com.airfitness.airfitness.common.util.ApiResponse;
import com.airfitness.airfitness.common.util.CookieUtil;
import com.airfitness.airfitness.common.util.JwtService;
import com.airfitness.airfitness.dto.AccessDto;
import com.airfitness.airfitness.dto.LoginDto;
import com.airfitness.airfitness.dto.RegisterDto;
import com.airfitness.airfitness.entity.Organization;
import com.airfitness.airfitness.entity.OrganizationUser;
import com.airfitness.airfitness.entity.RefreshToken;
import com.airfitness.airfitness.entity.User;
import com.airfitness.airfitness.repository.OrganizationRepository;
import com.airfitness.airfitness.repository.OrganizationUserRepository;
import com.airfitness.airfitness.repository.UserRepository;
import com.airfitness.airfitness.service.RefreshTokenService;
import com.airfitness.airfitness.service.interfaces.IAuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final PasswordEncoder encoder;
    private final UserRepository userRepo;
    private final OrganizationRepository orgRepo;
    private final OrganizationUserRepository orgUserRepo;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<AccessDto>> register(RegisterDto dto, HttpServletResponse response) {

        // check if user exists already
        if (userRepo.findByEmail(dto.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail("User already exists!"));
        }

        // check if gym already exists
        if (orgRepo.findByName(dto.gymName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail("Gym already exists!"));
        }

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
                .body(ApiResponse.ok("Registered Successful",
                        AccessDto.builder()
                                .accessToken(access)
                                .build()));
    }

    @Override
    public void logout(HttpServletRequest req, HttpServletResponse res) {
        String raw = CookieUtil.get(req, "refreshToken");
        refreshTokenService.revokeByRaw(raw);
        CookieUtil.clear(res, "refreshToken");
    }

    @Override
    public ResponseEntity<ApiResponse<AccessDto>> refresh(HttpServletRequest req) {
        String raw = CookieUtil.get(req, "refreshToken");

        Claims claims = jwtService.parseExpired(req.getHeader("Authorization"));

        Long userId = Long.valueOf(claims.getSubject());
        Long orgId = Long.valueOf(claims.get("org", String.class));

        RefreshToken refreshToken = refreshTokenService.validate(userId, raw);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Refresh Token is invalid"));
        }

        OrganizationUser ou =
                orgUserRepo.findByUserIdAndOrganizationId(userId, orgId)
                        .orElseThrow(UnauthorizedException::new);

        String newAccess = jwtService.generateAccessToken(
                userId, orgId, ou.getRole());

        return ResponseEntity.status(200)
                .body(ApiResponse.ok(
                        "Successfully generated refresh token",
                        AccessDto.builder()
                                .accessToken(newAccess)
                                .build()
                ));
    }

    @Override
    public ResponseEntity<ApiResponse<AccessDto>> login(LoginDto dto, HttpServletResponse res) {
        // auth
        Authentication authentication =  authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("Invalid username or password!"));
        }

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

        // add refreshToken to cookies
        CookieUtil.addRefreshCookie(res, refreshToken);

        // access token
        return ResponseEntity.status(200)
                .body(ApiResponse.ok("Login Successful", AccessDto.builder().accessToken(accessToken).build()));
    }
}

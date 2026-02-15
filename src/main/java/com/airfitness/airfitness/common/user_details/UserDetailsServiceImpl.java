package com.airfitness.airfitness.common.user_details;

import com.airfitness.airfitness.entity.OrganizationUser;
import com.airfitness.airfitness.entity.User;
import com.airfitness.airfitness.repository.OrganizationUserRepository;
import com.airfitness.airfitness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepo;
    private final OrganizationUserRepository orgUserRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // For simplicity, pick first organization role
        OrganizationUser ou = orgUserRepo.findFirstByUser(user)
                .orElseThrow(() -> new UsernameNotFoundException("User has no organization"));

        return new UserDetailsImpl(user, ou.getRole());
    }
}

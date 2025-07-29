package lendrix.web.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lendrix.web.app.dto.UserDto;
import lendrix.web.app.entity.User;
import lendrix.web.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
    Registers a new user (no email verification needed)
     */
    public User registerUser(UserDto userDto) {
        User user = mapToUser(userDto);
        return userRepository.save(user); // Save user and return
    }

    /**
    Authenticates user and returns JWT token + user info
     */
    public Map<String, Object> authenticateUser(UserDto userDto) {
        Map<String, Object> authObject = new HashMap<>();

        //Load user from DB
        User user = (User) userDetailsService.loadUserByUsername(userDto.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        //Authenticate username + password
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword())
        );

        //Generate JWT token
        String token = jwtService.generateToken(userDto.getUsername());

        //Prepare response (token + user info)
        authObject.put("token", "Bearer ".concat(token));
        authObject.put("user", user);

        return authObject;
    }

    /**
    Helper: Map DTO to User entity
     */
    private User mapToUser(UserDto dto) {
        return User.builder()
                .lastname(dto.getLastname())
                .firstname(dto.getFirstname())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .tag("lendrix_" + dto.getUsername())
                .dob(dto.getDob())
                .roles(List.of("USER")) // Default role
                .build();
    }
}

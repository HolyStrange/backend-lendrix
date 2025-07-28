package lendrix.web.app.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lendrix.web.app.dto.UserDto;
import lendrix.web.app.entity.User;
import lendrix.web.app.entity.VerificationToken;
import lendrix.web.app.repository.UserRepository;
import lendrix.web.app.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    /**
     *Registers a user, marks them as unverified, and sends verification email.
     */
    public User registerUser(UserDto userDto) {
        //Create user with verified = false
        User user = mapToUser(userDto);
        user.setVerified(false);  // New users are unverified
        User savedUser = userRepository.save(user);

        //Generate verification token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .owner(savedUser)
                .expiryDate(LocalDateTime.now().plusHours(24)) // Token expires in 24 hours
                .build();

        tokenRepository.save(verificationToken);

        //Send email with verification link
        String verificationLink = "http://localhost:8080/user/verify?token=" + token;
        emailService.sendSimpleEmail(
                savedUser.getEmail(),
                "Verify your Lendrix Account",
                "Hello " + savedUser.getFirstname() + ",\n\n"
                        + "Please click the following link to verify your account:\n"
                        + verificationLink + "\n\n"
                        + "This link will expire in 24 hours."
        );

        return savedUser;
    }

    /**
    Authenticates user (only if verified).
     */
    public Map<String, Object> authenticateUser(UserDto userDto) {
        Map<String, Object> authObject = new HashMap<>();
        User user = (User) userDetailsService.loadUserByUsername(userDto.getUsername());

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        //Block login if user not verified
        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword())
        );

        authObject.put("token", "Bearer ".concat(jwtService.generateToken(userDto.getUsername())));
        authObject.put("user", user);

        return authObject;
    }

    /**
    Verifies user using token.
     */
    public String verifyUser(String token) {
        Optional<VerificationToken> verificationTokenOpt = tokenRepository.findByToken(token);

        if (verificationTokenOpt.isEmpty()) {
            return "Invalid verification token.";
        }

        VerificationToken verificationToken = verificationTokenOpt.get();

        //Check token expiry
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return "Verification token has expired. Please request a new one.";
        }

        //Mark user as verified
        User user = verificationToken.getOwner();
        user.setVerified(true);
        userRepository.save(user);

        //Clean up used token
        tokenRepository.delete(verificationToken);

        return "Account verified successfully!";
    }

    /**
    Helper method: maps UserDto to User entity.
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

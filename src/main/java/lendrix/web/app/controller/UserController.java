package lendrix.web.app.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lendrix.web.app.dto.AccountDto;
import lendrix.web.app.dto.RegistrationRequest;
import lendrix.web.app.dto.UserDto;
import lendrix.web.app.entity.User;
import lendrix.web.app.service.AccountService;
import lendrix.web.app.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AccountService accountService;

    @PostMapping("/register")
    @SuppressWarnings("CallToPrintStackTrace")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {

        // Null safety to prevent NPE (instead of throwing a 500)
        if (request.getUser() == null) {
            return ResponseEntity.badRequest().body("User information is required for registration.");
        }

        // Register the user (no logic changed)
        User user = userService.registerUser(request.getUser());

        // Create the account for the user
        AccountDto accountDto = new AccountDto();
        accountDto.setCode(request.getAccountType());
        accountDto.setInitialBalance(0.0);

        try {
            accountService.createAccount(accountDto, user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody UserDto userDto) {
        var authObject = userService.authenticateUser(userDto);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, (String) authObject.get("token"))
                .body(authObject.get("user"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam("token") String token) {
        //Get verification message from UserService
        String message = userService.verifyUser(token);

        //Decide response based on message content
        if (message.equals("Account verified successfully!")) {
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.badRequest().body(message);
        }
    }
}

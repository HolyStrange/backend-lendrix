package lendrix.web.app.controller;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lendrix.web.app.dto.MoneyRequestDto;
import lendrix.web.app.entity.MoneyRequest;
import lendrix.web.app.entity.User;
import lendrix.web.app.service.MoneyRequestService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/money-requests")
@RequiredArgsConstructor
public class MoneyRequestController {

    private final MoneyRequestService moneyRequestService;

    // request money
    @PostMapping("/request")
    public ResponseEntity<?> requestMoney(@RequestBody MoneyRequestDto requestDto,
                                        Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        try {
            MoneyRequest request = moneyRequestService.requestMoney(
                    requester.getUsername(),
                    requestDto.getRecipientUsername(),
                    requestDto.getAmount()
            );
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Respond to a request (approve/reject)
    @PostMapping("/respond/{requestId}")
    public ResponseEntity<?> respondToRequest(@PathVariable String requestId,
                                            @RequestParam boolean approve) throws OperationNotSupportedException {
        try {
            MoneyRequest request = moneyRequestService.respondToRequest(requestId, approve);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //View incoming requests
    @GetMapping("/incoming")
    public ResponseEntity<List<MoneyRequest>> getIncomingRequests(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(moneyRequestService.getRequestsForUser(user.getUsername()));
    }

    //View outgoing requests
    @GetMapping("/outgoing")
    public ResponseEntity<List<MoneyRequest>> getOutgoingRequests(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(moneyRequestService.getRequestsByUser(user.getUsername()));
    }

    //money request controller for the requesting money from another user
}

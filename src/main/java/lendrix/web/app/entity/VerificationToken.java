package lendrix.web.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationToken {

    private static final int EXPIRATION_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    //Extra constructor for convenience
    public VerificationToken(String token, User owner) {
        this.token = token;
        this.owner = owner;
        this.expiryDate = LocalDateTime.now().plusHours(EXPIRATION_HOURS);
    }

    // helper method to check if token is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
}

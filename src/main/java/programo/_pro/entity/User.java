package programo._pro.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Email
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Getter
	@Column(name = "user_name", nullable = false)
    private String username;


    @Column(name = "password")
    private String password;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}

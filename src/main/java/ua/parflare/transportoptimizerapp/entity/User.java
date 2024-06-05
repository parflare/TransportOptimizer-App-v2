package ua.parflare.transportoptimizerapp.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ua.parflare.transportoptimizerapp.entity.enums.UserRole;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Document(collection = "users")
public class User implements UserDetails {
    @Id
    @Generated
    private UUID id;

    @NotBlank(message = "Name is mandatory")
    @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
    @Indexed(unique = true)
    private String userName;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 30, message = "Name must be between 6 and 30 characters")
    private String password;

    @Email(message = "Email should be valid")
    private String email;

    private boolean active;

    private Set<UserRole> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}

package com.gespromag.store.service;

import com.gespromag.store.dto.UserCreateDto;
import com.gespromag.store.dto.UserEditDto;
import com.gespromag.store.entity.User;
import com.gespromag.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Utilisateur introuvable : " + id));
    }

    public User create(UserCreateDto dto) {
        String username = dto.getUsername().trim();
        String email = dto.getEmail().trim();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est deja utilise.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Cet email est deja utilise.");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setActive(true);
        return userRepository.save(user);
    }

    public User update(Long id, UserEditDto dto) {
        User user = getById(id);
        String username = dto.getUsername().trim();
        String email = dto.getEmail().trim();
        if (!username.equalsIgnoreCase(user.getUsername()) && userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est deja utilise.");
        }
        if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Cet email est deja utilise.");
        }
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            if (dto.getNewPassword().length() < 8) {
                throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caracteres.");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(dto.getRole());
        user.setActive(dto.isActive());
        return userRepository.save(user);
    }
}

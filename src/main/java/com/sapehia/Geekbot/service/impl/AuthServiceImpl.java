package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.User;
import com.sapehia.Geekbot.repository.UserRepository;
import com.sapehia.Geekbot.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(User user) {
        Optional<User> existingUser= userRepository.findByEmail(user.getEmail());
        if(existingUser.isPresent()){
            throw new RuntimeException("User alrrady exist");
        }else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
        }
        return user;
    }
}

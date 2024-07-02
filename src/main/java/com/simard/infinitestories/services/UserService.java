package com.simard.infinitestories.services;

import com.simard.infinitestories.entities.User;
import com.simard.infinitestories.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class    UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long userId) {
        return this.userRepository.findById(userId).orElse(null);
    }

    public User createAndSaveNewUser(String userName, String userPWD) {
        User newUser = new User();
        newUser.setUsername(userName);
        newUser.setPassword(userPWD);
        return this.userRepository.save(newUser);
    }
}

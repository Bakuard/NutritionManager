package com.bakuard.nutritionManager.services;

import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.util.Pair;

import java.util.UUID;

public class AuthService {

    private JwsService jwsService;
    private EmailService emailService;
    private UserRepository userRepository;

    public AuthService(JwsService jwsService, EmailService emailService, UserRepository userRepository) {
        this.jwsService = jwsService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public Pair<String, User> enter(String name, String password) {
        User user = null;
        try {
            user = userRepository.getByName(name);
        } catch(ValidateException e) {
            throw new ValidateException(
                    "Incorrect credentials",
                    getClass(),
                    "enter"
            ).addReason(e);
        }

        if(user.isCorrectPassword(password)) {
            String jws = jwsService.generateAccessJws(user);
            return new Pair<>(jws, user);
        } else {
            throw new ValidateException(
                    "Incorrect credentials",
                    getClass(),
                    "enter"
            );
        }
    }

    public void verifyEmailForRegistration(String email) {
        String jws = jwsService.generateRegistrationJws(email);
        emailService.confirmEmailForRegistration(jws, email);
    }

    public void verifyEmailForChangeCredentials(String email) {
        String jws = jwsService.generateChangeCredentialsJws(email);
        emailService.confirmEmailForChangeCredentials(jws, email);
    }

    public Pair<String, User> registration(String jws, String name, String password) {
        try {
            String email = jwsService.parseRegistrationJws(jws);

            User user = new User(
                    UUID.randomUUID(),
                    name,
                    password,
                    email
            );
            userRepository.save(user);

            String accessJws = jwsService.generateAccessJws(user);
            return new Pair<>(accessJws, user);
        } catch(ValidateException e) {
            throw new ValidateException(
                    "Fail to register new user=" + name,
                    getClass(),
                    "registration"
            ).addReason(e);
        }
    }

    public Pair<String, User> changeCredential(String jws, String name, String password) {
        try {
            String email = jwsService.parseChangeCredentialsJws(jws);

            User user = userRepository.getByEmail(email);
            user.setPassword(password);
            user.setName(name);
            userRepository.save(user);

            String accessJws = jwsService.generateAccessJws(user);
            return new Pair<>(accessJws, user);
        } catch(ValidateException e) {
            throw new ValidateException(
                    "Fail to change credential for user=" + name,
                    getClass(),
                    "changeCredential"
            ).addReason(e);
        }
    }

    public User changeLoginAndEmail(String jws, String newName, String newEmail, String currentPassword) {
        try {
            UUID userId = jwsService.parseAccessJws(jws);

            User user = userRepository.getById(userId);
            if(!user.isCorrectPassword(currentPassword)) {
                throw new ValidateException(
                        "Incorrect password",
                        getClass(),
                        "changeLoginAndEmail"
                );
            }
            user.setName(newName);
            user.setEmail(newEmail);

            userRepository.save(user);

            return user;
        } catch(ValidateException e) {
            throw new ValidateException(
                    "Fail to change login and email for user",
                    getClass(),
                    "changeLoginAndEmail"
            ).addReason(e);
        }
    }

    public User changePassword(String jws, String currentPassword, String newPassword) {
        try {
            UUID userId = jwsService.parseAccessJws(jws);

            User user = userRepository.getById(userId);
            if(!user.isCorrectPassword(currentPassword)) {
                throw new ValidateException(
                        "Incorrect password",
                        getClass(),
                        "changePassword"
                );
            }
            user.setPassword(newPassword);

            userRepository.save(user);

            return user;
        } catch(ValidateException e) {
            throw new ValidateException(
                    "Incorrect password",
                    getClass(),
                    "changePassword"
            ).addReason(e);
        }
    }

    public User getUserByJws(String jws) {
        UUID userId = jwsService.parseAccessJws(jws);
        return userRepository.getById(userId);
    }

    public void logout(String jws) {
        jwsService.invalidateJws(jws);
    }

}

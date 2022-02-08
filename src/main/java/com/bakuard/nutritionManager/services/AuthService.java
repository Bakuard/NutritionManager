package com.bakuard.nutritionManager.services;

import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;
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
        try {
            User user = userRepository.getByName(name);
            if(!user.isCorrectPassword(password)) {
                throw Checker.of(getClass(), "enter").
                        createServiceException("Incorrect credentials");
            }
            String jws = jwsService.generateAccessJws(user);
            return new Pair<>(jws, user);
        } catch(ServiceException e) {
            throw Checker.of(getClass(), "enter").
                    createServiceException("Incorrect credentials").
                    addReasons(e.getConstraints());
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
            throw Checker.of(getClass(), "registration").
                    createServiceException("Fail to register new user=" + name).
                    addReasons(e.getConstraints());
        } catch(ServiceException e) {
            throw Checker.of(getClass(), "registration").
                    createServiceException("Fail to register new user=" + name).
                    addReasons(e.getConstraints());
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
            throw Checker.of(getClass(), "changeCredential").
                    createServiceException("Fail to change credential for user=" + name).
                    addReasons(e.getConstraints());
        } catch(ServiceException e) {
            throw Checker.of(getClass(), "changeCredential").
                    createServiceException("Fail to change credential for user=" + name).
                    addReasons(e.getConstraints());
        }
    }

    public User changeLoginAndEmail(String jws, String newName, String newEmail, String currentPassword) {
        try {
            UUID userId = jwsService.parseAccessJws(jws);

            User user = userRepository.getById(userId);
            if(!user.isCorrectPassword(currentPassword)) {
                throw Checker.of(getClass(), "changeLoginAndEmail").
                        createServiceException("Incorrect password");
            }
            user.setName(newName);
            user.setEmail(newEmail);

            userRepository.save(user);

            return user;
        } catch(ValidateException e) {
            throw Checker.of(getClass(), "changeLoginAndEmail").
                    createServiceException("Fail to change login and email for user").
                    addReasons(e.getConstraints());
        } catch(ServiceException e) {
            throw Checker.of(getClass(), "changeLoginAndEmail").
                    createServiceException("Fail to change login and email for user").
                    addReasons(e.getConstraints());
        }
    }

    public User changePassword(String jws, String currentPassword, String newPassword) {
        try {
            UUID userId = jwsService.parseAccessJws(jws);

            User user = userRepository.getById(userId);
            if(!user.isCorrectPassword(currentPassword)) {
                throw Checker.of(getClass(), "changePassword").
                        createServiceException("Incorrect password");
            }
            user.setPassword(newPassword);

            userRepository.save(user);

            return user;
        } catch(ValidateException e) {
            throw Checker.of(getClass(), "changePassword").
                    createServiceException("Fail to change password for user").
                    addReasons(e.getConstraints());
        } catch(ServiceException e) {
            throw Checker.of(getClass(), "changePassword").
                    createServiceException("Fail to change password for user").
                    addReasons(e.getConstraints());
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

package com.bakuard.nutritionManager.service;

import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.util.Pair;

import java.util.UUID;

import static com.bakuard.nutritionManager.validation.Rule.failure;
import static com.bakuard.nutritionManager.validation.Rule.notNull;

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
        User user = userRepository.tryGetByName(name);

        if(user.isCorrectPassword(password)) {
            String jws = jwsService.generateAccessJws(user);
            return new Pair<>(jws, user);
        } else {
            throw new ValidateException("Incorrect credentials").
                    addReason(Rule.of("AuthService.enter", failure(Constraint.CORRECT_CREDENTIALS)));
        }
    }

    public void verifyEmailForRegistration(String email) {
        Validator.check("AuthService.email", notNull(email));

        if(userRepository.getByEmail(email).isPresent()) {
            throw new ValidateException().
                    addReason(Rule.of("AuthService.verifyEmailForRegistration", failure(Constraint.ENTITY_MUST_BE_UNIQUE_IN_DB)));
        }

        String jws = jwsService.generateRegistrationJws(email);
        emailService.confirmEmailForRegistration(jws, email);
    }

    public void verifyEmailForChangeCredentials(String email) {
        Validator.check("AuthService.email", notNull(email));

        String jws = jwsService.generateChangeCredentialsJws(email);
        emailService.confirmEmailForChangeCredentials(jws, email);
    }

    public Pair<String, User> registration(String jws, String name, String password) {
        String email = jwsService.parseRegistrationJws(jws);

        User user = new User.Builder().
                generateId().
                setName(name).
                setEmail(email).
                setPassword(password).
                tryBuild();
        userRepository.save(user);

        String accessJws = jwsService.generateAccessJws(user);
        return new Pair<>(accessJws, user);
    }

    public Pair<String, User> changeCredential(String jws, String name, String password) {
        String email = jwsService.parseChangeCredentialsJws(jws);

        User user = userRepository.tryGetByEmail(email);
        user.setPassword(password);
        user.setName(name);
        userRepository.save(user);

        String accessJws = jwsService.generateAccessJws(user);
        return new Pair<>(accessJws, user);
    }

    public User changeLoginAndEmail(UUID userId, String newName, String newEmail, String currentPassword) {
        User user = userRepository.tryGetById(userId);
        if(!user.isCorrectPassword(currentPassword)) {
            throw new ValidateException("Incorrect password").
                    addReason(Rule.of("AuthService.changeLoginAndEmail", failure(Constraint.CORRECT_CREDENTIALS)));
        }
        user.setName(newName);
        user.setEmail(newEmail);

        userRepository.save(user);

        return user;
    }

    public User changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.tryGetById(userId);
        if(!user.isCorrectPassword(currentPassword)) {
            throw new ValidateException("Incorrect password").
                    addReason(Rule.of("AuthService.changePassword", failure(Constraint.CORRECT_CREDENTIALS)));

        }
        user.setPassword(newPassword);

        userRepository.save(user);

        return user;
    }

    public User getUserByJws(String jws) {
        UUID userId = jwsService.parseAccessJws(jws);
        return userRepository.tryGetById(userId);
    }

    public UUID logout(String jws) {
        return jwsService.invalidateJws(jws);
    }

}

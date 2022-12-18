package com.bakuard.nutritionManager.service;

import com.bakuard.nutritionManager.config.configData.ConfigData;
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
    private ConfigData configData;

    public AuthService(JwsService jwsService,
                       EmailService emailService,
                       UserRepository userRepository,
                       ConfigData configData) {
        this.jwsService = jwsService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.configData = configData;
    }

    public Pair<String, User> enter(String name, String password) {
        User user = userRepository.tryGetByName(name);

        if(user.isCorrectPassword(password)) {
            String accessJws = jwsService.generateJws(user.getId(),
                    "commonToken",
                    configData.jws().commonTokenLifeTime());
            return new Pair<>(accessJws, user);
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

        String jws = jwsService.generateJws(email,
                "registration",
                configData.jws().registrationTokenLifeTime());
        emailService.confirmEmailForRegistration(jws, email);
    }

    public void verifyEmailForChangeCredentials(String email) {
        Validator.check("AuthService.email", notNull(email));

        String jws = jwsService.generateJws(email,
                "restorePassword",
                configData.jws().restorePassTokenLifeTime());
        emailService.confirmEmailForChangeCredentials(jws, email);
    }

    public Pair<String, User> registration(String email, String name, String password) {
        User user = new User.Builder().
                generateId().
                setName(name).
                setEmail(email).
                setPassword(password).
                tryBuild();
        userRepository.save(user);

        String accessJws = jwsService.generateJws(user.getId(),
                "commonToken",
                configData.jws().commonTokenLifeTime());
        return new Pair<>(accessJws, user);
    }

    public Pair<String, User> changeCredential(String email, String name, String password) {
        User user = userRepository.tryGetByEmail(email);
        user.setPassword(password);
        user.setName(name);
        userRepository.save(user);

        String accessJws = jwsService.generateJws(user.getId(),
                "commonToken",
                configData.jws().commonTokenLifeTime());
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

    public void logout(String jws) {
        jwsService.invalidateJws(jws, "commonToken");
    }

}

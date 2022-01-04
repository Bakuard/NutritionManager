package com.bakuard.nutritionManager.services;

import com.bakuard.nutritionManager.dal.UserRepository;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.*;

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

    public String enter(String name, String password) {
        try {
            User user = userRepository.getByName(name);
            if(!user.isCorrectPassword(password)) {
                throw new FailEnterException("Incorrect password for user=" + name);
            }
            return jwsService.generateAccessJws(user);
        } catch(UnknownUserException e) {
            throw new FailEnterException("Unknown user=" + name);
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

    public String registration(String jws, String name, String password) {
        try {
            String email = jwsService.parseRegistrationJws(jws);

            User user = new User(
                    UUID.randomUUID(),
                    name,
                    password,
                    email
            );
            userRepository.save(user);

            return jwsService.generateAccessJws(user);
        } catch(AbstractDomainException e) {
            throw new FailRegistrationException("Fail registration user=" + name, e);
        }
    }

    public String changeCredential(String jws, String name, String password) {
        try {
            String email = jwsService.parseChangeCredentialsJws(jws);

            User user = userRepository.getByEmail(email);
            user.setPassword(password);
            user.setName(name);
            userRepository.save(user);

            return jwsService.generateAccessJws(user);
        } catch(AbstractDomainException e) {
            throw new FailChangeCredentialsException("Fail change credential user=" + name, e);
        }
    }

}

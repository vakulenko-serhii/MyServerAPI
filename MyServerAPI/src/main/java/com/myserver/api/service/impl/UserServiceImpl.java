package com.myserver.api.service.impl;

import com.myserver.api.auth.TokenHandler;
import com.myserver.api.dao.UserDAO;
import com.myserver.api.dto.response.AuthResponse;
import com.myserver.api.exception.NotAuthorizedException;
import com.myserver.api.model.User;
import com.myserver.api.service.UserService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

/**
 * @author Igor Hnes on 18.06.2020.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDAO userDao;
    @Autowired
    private TokenHandler tokenHandler;

    @Override
    public User findOne(Integer id) {
        return userDao.findOne(id);
    }

    @Override
    public AuthResponse login(String login, String password) {
        final User user = userDao.findByLogin(login);
        if (user == null) {
            throw new NotAuthorizedException("Unauthorized");
        }
        final boolean match = match(user.getPassword(), password);
        if (match) {
            final String token = tokenHandler.createTokenForUser(user);
            return new AuthResponse(token);
        }
        throw new NotAuthorizedException("Unauthorized");
    }

    @SneakyThrows
    public boolean match(String password, String compare) {
        final String md5Hash = generateMd5Hash(compare);
        return md5Hash.equals(password);
    }

    @SneakyThrows
    public String generateMd5Hash(String password) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] digest = md.digest();
        return printHexBinary(digest);
    }
}

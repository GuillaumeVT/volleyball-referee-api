package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.dao.UserDao;
import com.tonkar.volleyballreferee.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserDao userDao;

    public Page<User> listUsers(String filter, Pageable pageable) {
        return userDao.listUsers(filter, pageable);
    }

    // TODO CRUD user
}

package pers.cz.autoapidoc.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pers.cz.autoapidoc.common.User;
import pers.cz.autoapidoc.common.dto.UserQueryDTO;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/task")
public class UserController {

    @RequestMapping(value = "/url1/1", method = RequestMethod.POST)
    public List<User> queryAllUsers(UserQueryDTO userQueryDTO) {
        ArrayList<User> list = new ArrayList<>();
        User user = new User();
        userQueryDTO.getId();
        userQueryDTO.getName();
        user.setId(1);
        user.setName("admin");
        user.setAge(20);
        list.add(user);
        list.add(user);
        return list;
//        return user;
//        return new HashMap<>();
    }
}

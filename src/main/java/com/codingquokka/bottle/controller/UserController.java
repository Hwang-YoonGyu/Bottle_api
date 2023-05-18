package com.codingquokka.bottle.controller;


import com.codingquokka.bottle.dao.UserDao;
import com.codingquokka.bottle.service.UserService;
import com.codingquokka.bottle.core.AES128;
import com.codingquokka.bottle.vo.UserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {


    @Autowired
    private UserDao udao;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AES128 aes128;

    @PostMapping("/login")
    public ResponseEntity<Object> login(HttpSession session, @RequestBody HashMap<String, Object> map) throws Exception {
        HashMap<String, Object> param = new HashMap<>();
        param.put("email", aes128.decrypt((String) map.get("email")));
        param.put("password", map.get("password"));
        Map<String, Object> res = userService.login(param);

        Map<String, String> responseData = new HashMap<String, String>();
        if (res != null) {
            session.setAttribute("userData", res);

            responseData.put("status", "200");
            responseData.put("message", "success");
        } else {
            responseData.put("status", "500");
            responseData.put("message", "fail");
        }
        System.out.println(param.get("email").toString());

        String loginResult = objectMapper.writeValueAsString(responseData); // Map을 JSON 형식으로 바꿔준다 !!
        return ResponseEntity.ok(loginResult);
    }

    @PostMapping("/join")
    public void join(HttpSession session, UserVO uservo) throws Exception {
        HashMap<String, Object> param = new HashMap<>();
        param.put("userId", uservo.getUserId());
        System.out.println(param.get("userId"));
        Map<String, Object> res = userService.login(param);


        udao.join();

    }

    @GetMapping("/certUser/{encyptedUuid}")
    public ModelAndView cert(@PathVariable("encyptedUuid") String encyptedUuid) throws Exception {

        System.out.println(encyptedUuid);

//        if (userService.cert(aes128.decrypt(encyptedUuid)) == 1) {
//            //인증 완료 페이지
//            return "/certSuccess";
//        }


        //인증 실패 페이지
        return new ModelAndView("/certFail");
    }



}

package com.codingquokka.bottle.controller;


import com.codingquokka.bottle.core.MessageUtils;
import com.codingquokka.bottle.dao.UserDao;
import com.codingquokka.bottle.service.EmoticonService;
import com.codingquokka.bottle.service.MailDomainService;
import com.codingquokka.bottle.service.MailService;
import com.codingquokka.bottle.service.UserService;
import com.codingquokka.bottle.core.AES128;
import com.codingquokka.bottle.vo.UserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MailDomainService mailDomainService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AES128 aes128;

    @Autowired
    private MailService mailService;

    @Autowired
    private EmoticonService emoticonService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody HashMap<String, Object> map) throws Exception {
        map.put("email", aes128.decrypt((String) map.get("email"), "common"));
        Map<String, Object> res = userService.login(map);

        Map<String, String> responseData = new HashMap<String, String>();
        if (res != null) {
            if (res.get("IS_CERTIFIED").equals("Y")) {
                responseData.put("auth",aes128.encrypt(objectMapper.writeValueAsString(res),"login"));
                responseData.put("status", "success");
                responseData.put("message", "성공");
//                System.out.println(aes128.decrypt(responseData.get("auth"), "login"));
//                System.out.println(objectMapper.readValue(aes128.decrypt(responseData.get("auth"), "login"), Map.class));

            } else {
                responseData.put("status", "fail");
                responseData.put("message", "인증되지 않은 계정입니다.");
            }
        } else {
            responseData.put("status", "fail");
            responseData.put("message", "존재하지 않는 계정정보입니다.\n이메일과 비밀번호를 확인해주세요.");
        }
        String loginResult = objectMapper.writeValueAsString(responseData); // Map을 JSON 형식으로 바꿔준다 !!
        return ResponseEntity.ok(loginResult);
    }

    @PostMapping("/join")
    public ResponseEntity<Object> join(@RequestBody HashMap<String, Object> map) throws Exception  {
        map.put("email",aes128.decrypt(map.get("email").toString(), "common"));
        map.put("uuid",UUID.randomUUID().toString());

        Map<String, String> responseData = new HashMap<String, String>();

        String[] email = map.get("email").toString().split("@");
        map.put("belong", email[1]);
        map.put("domain_cd", email[1]);
        int result =  userService.join(map);
        if (result == 1) {
            responseData.put("status", "success");
            responseData.put("message", "회원가입을 위한 인증 메일이 전송되었습니다.");
        } else if (result == -1) {
            responseData.put("status", "fail");
            responseData.put("message", "가입할 수 없는 메일 도메인입니다.");
        }
        String joinResult = objectMapper.writeValueAsString(responseData); // Map을 JSON 형식으로 바꿔준다 !!

        return ResponseEntity.ok(joinResult);
    }

    @PostMapping("/checkEmail")
    public ResponseEntity<Object> checkEmail(@RequestBody HashMap<String, Object> map) throws Exception {
        map.put("email", aes128.decrypt((String) map.get("email"), "common"));

        int res = userService.checkEmail(map);

        Map<String, String> responseData = new HashMap<String, String>();
        if(res == 1){
            responseData.put("status", "fail");
            responseData.put("message", "이미 존재하는 계정입니다.");
        } else {
            responseData.put("status", "success");
            responseData.put("message", "가입 가능한 계정입니다.");
        }
        String checkEmailResult = objectMapper.writeValueAsString(responseData); // Map을 JSON 형식으로 바꿔준다 !!
        return ResponseEntity.ok(checkEmailResult);
    }

    @GetMapping("/certUser/{base64Uuid}")
    public ModelAndView cert(@PathVariable("base64Uuid") String base64Uuid) throws Exception {
       byte [] base64EncryptedUuid = Base64.getDecoder().decode(base64Uuid);
       String encryptedUuid = Base64.getEncoder().encodeToString(base64EncryptedUuid);

        ModelAndView mv = new ModelAndView();
        if (userService.cert(aes128.decrypt(encryptedUuid, "common")) == 1) {
            mv.setViewName("/cert/cert_Success");
        } else {
            mv.setViewName("/cert/cert_Fail");
        }

        return mv;
    }

    @GetMapping("/getEmoticon/{emoId}")
    public ResponseEntity<Object> getEmoticon(@PathVariable("emoId") int emoId) {
        Map<String, String> responseData = new HashMap<>();

        String emoticon = emoticonService.getEmoticon(emoId);

        if (emoticon != null) {
            responseData.put("status", "success");
            responseData.put("message", emoticon);
        }
        else {
            responseData.put("status", "fail");
        }
        return ResponseEntity.ok(responseData);
    }



}

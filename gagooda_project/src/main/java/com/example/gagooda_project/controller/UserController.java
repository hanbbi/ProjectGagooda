package com.example.gagooda_project.controller;

import com.example.gagooda_project.dto.UserDto;
import com.example.gagooda_project.service.UserService;
import com.example.gagooda_project.service.UserServiceImp;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class UserController {
    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{gDet}/signup.do")
    public String signup(
            @SessionAttribute(required = false) String msg,
            HttpSession session,
            Model model,
            @PathVariable String gDet
    ) {
        if (msg != null) {
            session.removeAttribute("msg");
            System.out.println(msg);
        }
        model.addAttribute("msg", msg);
        if (gDet != null) {
            model.addAttribute("gDet", gDet);
        }
        return "/user/signup";
    }

    @PostMapping("/signup.do")
    public String signup(
            UserDto user,
            HttpSession session
    ) {
        int signup = 0;
        try {
            signup = userService.register(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(user);
        if (signup > 0) {
            session.setAttribute("msg", "회원가입을 성공적으로 마쳤습니다.");
            return "redirect:/user/login.do";
        } else {
            session.setAttribute("msg", "회원가입 중 오류가 있었습니다.");
            return "redirect:/user/signup.do";
        }
    }

    @GetMapping("/login.do")
    public String login(@SessionAttribute(required = false) String msg,
                        HttpSession session,
                        Model model) {
        if (msg != null) {
            session.removeAttribute("msg");
            System.out.println(msg);
        }
        model.addAttribute("msg", msg);
        return "/user/login";
    }

    @PostMapping("/login.do")
    public String login(
            HttpSession session,
            String email,
            String pw,
            @SessionAttribute(required = false) String redirectUri
    ) {
        UserDto user = null;
        try {
            user = userService.login(email, pw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (user == null) {
            session.setAttribute("msg", "이메일 주소나 비밀번호가 잘못되었습니다.");
            return "redirect:/user/login.do";
        } else {
            session.setAttribute("loginUser", user);
            session.removeAttribute("redirectUri");
            return (redirectUri != null) ? "redirect:" + redirectUri : "redirect:/";
        }
    }

    @GetMapping("/findpw.do")
    public String findPw(
            @SessionAttribute(required = false) String msg,
            HttpSession session
    ) {
        if (msg != null) {
            System.out.println(msg);
            session.removeAttribute("msg");
        }
        return "/user/find_pw";
    }

    @PostMapping("/findpw.do")
    public String findPw(
            String uname,
            String email,
            HttpSession session
    ) {
        UserDto user = null;
        try {
            user = userService.findpw(email, uname);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (user == null) {
            session.setAttribute("msg", "존재하지 않는 이름이거나 이메일 주소 입니다");
            return "redirect:/user/findpw.do";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/{userId}/password_reset.do")
    public String resetPw(
            Model model,
            @PathVariable(required = true, name = "userId") int userId,
            @SessionAttribute(required = false) String msg,
            HttpSession session
    ) {
        if (msg != null) {
            System.out.println(msg);
            session.removeAttribute("msg");
        }
        System.out.println("userId: " + userId);
        model.addAttribute("userId", userId);
        return "/user/password_reset";
    }

    @PostMapping("/password_reset.do")
    public String resetPw(
            String pw,
            int userId,
            HttpSession session
    ) {
        int reset = 0;
        try {
            UserDto user = userService.selectOne(userId);
            user.setPw(pw);
            reset = userService.modifyOne(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (reset > 0) {
            return "redirect:/user/login.do";
        } else {
            session.setAttribute("msg", "비밀번호 재설정에 실패했습니다.");
            return "redirect:/user/" + userId + "/password_reset.do";
        }
    }

    @GetMapping("/double_check.do")
    public String doubleCheck(
            @SessionAttribute(required = true) UserDto loginUser,
            @SessionAttribute(required = false) String msg,
            HttpSession session
    ) {
        if (msg != null) {
            session.removeAttribute(msg);
            System.out.println(msg);
        }
        return "/user/double_check";
    }

    @PostMapping("/double_check.do")
    public String doubleCheck(
            @SessionAttribute(required = true) UserDto loginUser,
            String pw
    ) {
        UserDto user = null;
        try {
            user = userService.doubleCheck(loginUser.getEmail(), pw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (user != null) {
            return "redirect:/user/temp.do";
        } else {
            return "redirect:/user/double_check.do";
        }
    }

    /*임시적인 경로*/
    @GetMapping("/temp.do")
    public String temp(
            @SessionAttribute(required = true) UserDto loginUser,
            @SessionAttribute(required = false) String msg,
            HttpSession session
    ) {
        if (msg != null) {
            session.removeAttribute("msg");
            System.out.println(msg);
        }
        return "/user/temp";
    }

    @GetMapping("/{userId}/remove.do")
    public String remove(
            @SessionAttribute(required = true) UserDto loginUser,
            HttpSession session
    ) {
        int delete = 0;
        try {
            delete = userService.delete(loginUser.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (delete > 0) {
            session.removeAttribute("loginUser");
            return "redirect:/";
        } else {
            session.setAttribute("msg", "삭제 중 오류가 생겼습니다. 다시 시도해 주세요.");
            return "redirect:/user/temp.do";
        }
    }

    @GetMapping("/{userId}/modify.do")
    public String modify(
            @SessionAttribute(required = true) UserDto loginUser,
            Model model,
            @PathVariable int userId,
            @SessionAttribute(required = false) String msg,
            HttpSession session
    ) {
        if (msg != null) {
            session.removeAttribute("msg");
            System.out.println(msg);
        }
        model.addAttribute("user", loginUser);
        model.addAttribute("userId", userId);
        return "/user/modify";
    }

    @PostMapping("/modify.do")
    public String modify(
            @SessionAttribute(required = true) UserDto loginUser,
            UserDto modifiedUser,
            HttpSession session
    ) {
        int modify=0;
        try {
            modifiedUser.setPw(loginUser.getPw());
            modifiedUser.setMsDet(loginUser.getMsDet());
            modify = userService.modifyOne(modifiedUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (modify > 0 ) {
            session.removeAttribute("loginUser");
            UserDto user = userService.selectOne(loginUser.getUserId());
            session.setAttribute("loginUser", user);
            return "redirect:/user/temp.do";
        } else {
            session.setAttribute("msg", "사용자 정보 수정 중 오류 뜸");
            return "redirect:/user/"+loginUser.getUserId()+"/modify.do";
        }
    }

    @GetMapping("/admin/list.do")
    public String adminList(
            @SessionAttribute(required = true) UserDto loginUser
    ) {
        return "";
    }

    @GetMapping("/logout.do")
    public String logout(
            @SessionAttribute UserDto loginUser,
            HttpSession session
    ) {
        session.removeAttribute("loginUser");
        return "redirect:/";
    }
}

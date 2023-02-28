package com.example.gagooda_project.controller;

import com.example.gagooda_project.dto.*;
import com.example.gagooda_project.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/review")
public class ReviewController {
    ReviewService reviewService;
    @Value("${img.upload.path}")
    private String imgPath;
    private Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/list.do")
    public String list(Model model,
                       @RequestParam(name = "productCode", required = false) String productCode,
                       HttpSession session,
                       @SessionAttribute(required = false) String msg) {
        if (msg != null) {
            session.removeAttribute("msg");
            model.addAttribute("msg", msg);
        }
        try {
            List<ReviewDto> reviewList = reviewService.reviewList(productCode);
            model.addAttribute("reviewList", reviewList);
            return "/review/list";
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("msg","리뷰를 불러올 수 없습니다.");
            return "redirect:/";
        }

    }

    @GetMapping("/user_yes/mypage/m_list")
    public String m_list(Model model,
                         HttpSession session,
                         @SessionAttribute UserDto loginUser,
                         @SessionAttribute(required = false) String msg) {

        return "/review/m_list";
    }

    @GetMapping("/user_yes/{productCode}/register.do")
    public String register(@SessionAttribute UserDto loginUser,
                           @SessionAttribute(required = false) String msg,
                           @PathVariable(required = true, name = "productCode") String productCode,
                           @RequestParam(name = "orderId") String orderId,
                           HttpSession session,
                           Model model
    ) {
        if (msg != null) {
            session.removeAttribute("msg");
            model.addAttribute("msg", msg);
        }
        try {
            List<OptionProductDto> optionProductList = reviewService.showOptionProduct(productCode);
            model.addAttribute("orderId",orderId);
            model.addAttribute("optionProductList",optionProductList);
            model.addAttribute("productCode",productCode);
            return "/review/register";
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("msg","리뷰를 등록할 수 없습니다.");
            return "/review/list";
        }

    }

    // localhost:8888/review/user_yes/register.do?optionCode=HA00G01EEU_GY

    @PostMapping("/user_yes/register.do")
    public String register(ReviewDto review,
                           @RequestParam(name = "imgFile") List<MultipartFile> imgFileList,
                           HttpSession session,
                           @SessionAttribute UserDto loginUser) {
        int register = 0;
        if (loginUser.getUserId() == review.getUserId()) {
            try {
                register = reviewService.register(imgFileList, review, imgPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (register > 0) {
            session.setAttribute("msg","리뷰 등록에 성공하였습니다.");
            return "redirect:/review/list.do?productCode=" + review.getProductCode();
        } else {
            session.setAttribute("msg","리뷰 등록에 실패하였습니다.");
            return "redirect:/review/user_yes/"+review.getProductCode()+"/register.do";
        }
    }
    @GetMapping("/user_yes/modify.do")
    public String modify( @SessionAttribute UserDto loginUser,
                          @RequestParam (name = "reviewId") int reviewId,
                          @SessionAttribute(required = false) String msg,
                          HttpSession session,
                          Model model) {
        if (msg != null) {
            session.removeAttribute("msg");
            model.addAttribute("msg", msg);
            System.out.println(msg);
        }
        try {
            ReviewDto review = reviewService.selectOne(reviewId);
            List<OptionProductDto> optionProductList = reviewService.showOptionProduct(review.getProductCode());
            model.addAttribute("review",review);
            model.addAttribute("optionProductList",optionProductList);
            return "/review/modify";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/review/list.do";
        }
    }
    @PostMapping("/user_yes/modify.do")
    public String modify(ReviewDto review,
                         @RequestParam(name = "imgFile", required = false)List<MultipartFile> imageList,
                         @RequestParam(name = "imgToDelete", required = false) List<String> imgToDeleteList,
                         HttpSession session,
                         @SessionAttribute UserDto loginUser) {
        log.info("imgToDeleteList: "+imgToDeleteList);
        reviewService.update(imageList, review, imgPath, imgToDeleteList);
        return "redirect:/review/list.do?productCode=" + review.getProductCode();
    }

    @GetMapping("/user_yes/delete.do")
    public String remove( @RequestParam(name = "reviewId") int reviewId,
                          @SessionAttribute UserDto loginUser,
                          @SessionAttribute(required = false) String msg,
                          HttpSession session,
                          Model model
                          ) {
        if (msg != null) {
            session.removeAttribute("msg");
            model.addAttribute("msg", msg);
            System.out.println(msg);
        }
        ReviewDto review = reviewService.selectOne(reviewId);
        try {
            if (loginUser.getUserId() == review.getUserId()) {
                int delete = reviewService.delete(review, reviewId);
                if (delete > 0) {
                    session.setAttribute("msg","성공적으로 삭제하였습니다.");
                    return "redirect:/review/list.do?productCode=" + review.getProductCode();
                } else {
                    session.setAttribute("msg","삭제를 실패하였습니다.");
                    return "redirect:/review/list";
                }
            } else {
                session.setAttribute("msg","삭제할 권한이 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("msg","삭제하는 중 오류가 있었습니다.");
        }
        return "redirect:/review/list.do?productCode=" + review.getProductCode();
    }

    // admin
    @GetMapping("/admin/list.do")
    public String adminList(Model model,
                            @SessionAttribute UserDto loginUser,
                            PagingDto paging,
                            HttpServletRequest req,
                            HttpSession session,
                            @SessionAttribute(required = false) String msg,
                            @RequestParam(name = "searchDiv", required = false, defaultValue = "") String searchDiv,
                            @RequestParam(name = "searchWord", required = false, defaultValue = "") String searchWord,
                            @RequestParam(name = "dateType", required = false, defaultValue = "") String dateType,
                            @RequestParam(name = "startDate", required = false, defaultValue = "") String startDate,
                            @RequestParam(name = "endDate", required = false, defaultValue = "") String endDate){
        log.info("req.getParameterMap:"+req.getParameterMap());
        String adminMsg = "";
        if (session.getAttribute(msg) != null){
            adminMsg = session.getAttribute(msg).toString();
            session.removeAttribute(msg);
        }
        try {
            if(loginUser.getGDet().equals("g1")) {
                Map<String, Object> searchFilter = new HashMap<>();
                paging.setQueryString(req.getParameterMap());
                searchFilter.put("searchDiv", searchDiv);
                searchFilter.put("searchWord", searchWord);
                searchFilter.put("dateType", dateType);
                searchFilter.put("startDate", startDate);
                searchFilter.put("endDate", endDate);
                searchFilter.put("paging", paging);
                List<ReviewDto> reviewList = reviewService.showReviews(searchFilter);
                int count = reviewService.countByReviews(searchFilter);
                model.addAttribute("reviewList", reviewList);
                model.addAttribute("paging", paging);
                model.addAttribute("count", count);
                model.addAttribute("msg", msg);
                return "/review/admin/list";
            } else{
                return "/index";
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return "/review/admin/list";
    }

    @GetMapping("/admin/{reviewId}/detail.do")
    public String adminDetail(@SessionAttribute UserDto loginUser,
                              @PathVariable int reviewId,
                              Model model
                       ){
        int detail = 0;
        System.out.println(reviewId);
        try {
            ReviewDto review = reviewService.selectOne(reviewId);
            model.addAttribute("review",review);
            detail = 1;
        }catch (Exception e){
            e.printStackTrace();
        }
        if (detail > 0){
            return "/review/admin/detail";
        } else  {
            return "/errorHandler";
        }
    }
}
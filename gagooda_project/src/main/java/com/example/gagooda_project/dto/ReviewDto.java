package com.example.gagooda_project.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ReviewDto {
    private int reviewId;       // 상품 후기ID, review_id, PK, NN
    private int userId;         // 사용자 ID, user_id, FK, NN, ( REVIEW : USER ) 1:1
    private int productId;      // 상품 ID, product_id, FK , NN, ( REVIEW : PRODUCT ) 1:N
    private String optionId;    // 옵션상품 ID, option_id, FK, NN, ( PRODUCT : OPTION ) 1:N
    private String content;     // 후기 내용, content, NULL
    private String imgCode;     // 이미지 코드, img_code, NULL
    private double rate;        // 평점, rate, NN
    private Date regDate;       // 등록일, reg_date, NN
}

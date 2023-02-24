package com.example.gagooda_project.service;

import com.example.gagooda_project.StaticMethods;
import com.example.gagooda_project.dto.ImageDto;
import com.example.gagooda_project.dto.OptionProductDto;
import com.example.gagooda_project.dto.ReviewDto;
import com.example.gagooda_project.mapper.ImageMapper;
import com.example.gagooda_project.mapper.OptionProductMapper;
import com.example.gagooda_project.mapper.ReviewMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ReviewServiceImp implements ReviewService {
    ReviewMapper reviewMapper;
    OptionProductMapper optionProductMapper;
    ImageMapper imageMapper;


    public ReviewServiceImp(ReviewMapper reviewMapper, OptionProductMapper optionProductMapper, ImageMapper imageMapper) {
        this.reviewMapper = reviewMapper;
        this.optionProductMapper = optionProductMapper;
        this.imageMapper = imageMapper;
    }

    @Override
    public List<ReviewDto> reviewList(String productCode) {
        return reviewMapper.listByProductCode(productCode);
    }

    @Override
    public ReviewDto selectOne(int reviewId) {
        return reviewMapper.findById(reviewId);
    }

    @Override
    public int insertOne(ReviewDto reviewDto) {
        return reviewMapper.insertOne(reviewDto);
    }

    @Override
    public int updateOne(ReviewDto dto) {
        return 0;
    }

    @Override
    public int remove(int reviewId) {
        return reviewMapper.deleteOne(reviewId);
    }

    @Override
    public List<OptionProductDto> showOptionProduct(String productCode) {
        return optionProductMapper.listByProductCode(productCode);
    }

    @Override
    @Transactional
    public int register(List<MultipartFile> imgFileList, ReviewDto review, String imgPath) {
        try {
            if(reviewMapper.insertOne(review)<=0) throw new Error("후기를 등록하는데 문제가 있었습니다.");
            review.setImgCode("review"+review.getReviewId());
            if(reviewMapper.updateOne(review)<=0) throw new Error("후기 이미지 코드를 수정하는데 문제가 있었습니다.");
            for (int imgFile = 0; imgFile < imgFileList.size(); imgFile++) {
                ImageDto image = StaticMethods.parseIntoImage(imgFileList.get(imgFile), review.getImgCode(), imgPath+"/review", imgFile+1);
                if(imageMapper.insertOne(image)<=0) throw new Error("사진을 올리는데 문제가 있었습니다.");
            }
            return 1;
        } catch (Exception | Error e) {
            e.printStackTrace();
            throw new Error("전체적인 오류");
        }
    }
}

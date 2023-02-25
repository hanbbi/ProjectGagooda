package com.example.gagooda_project.mapper;

import com.example.gagooda_project.dto.PagingDto;
import com.example.gagooda_project.dto.ReviewDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewMapper {

    List<ReviewDto> listByProductCode (String productCode);

    List<ReviewDto> listAll();

    ReviewDto findById(int id);

    int insertOne(ReviewDto review);

    int updateOne(ReviewDto review);

    int deleteOne(int reviewId);

    int countByProductCode(String productCode);

    List<ReviewDto> listByProductCodeAll(String productCode);

    List<ReviewDto> pageAll (PagingDto paging);

    int count (PagingDto paging);
}

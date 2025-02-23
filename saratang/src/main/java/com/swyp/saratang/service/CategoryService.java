package com.swyp.saratang.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swyp.saratang.mapper.CategoryMapper;
import com.swyp.saratang.model.CategoryDTO;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryMapper categoryMapper;
	//조회
	public CategoryDTO getCategoryList(int userId) {
		List<Integer> categoryIds =categoryMapper.getCategoryList(userId);
		CategoryDTO categoryDTO = new CategoryDTO();
		
		categoryDTO.setUserId(userId);
		
		boolean[] categoryFlags = new boolean[12];
		for(Integer categoryId:categoryIds) {
			categoryFlags[categoryId - 1] = true;
		}
        categoryDTO.setOuterwear(categoryFlags[0]);
        categoryDTO.setTops(categoryFlags[1]);
        categoryDTO.setBottoms(categoryFlags[2]);
        categoryDTO.setUnderwearHomewear(categoryFlags[3]);
        categoryDTO.setShoes(categoryFlags[4]);
        categoryDTO.setBags(categoryFlags[5]);
        categoryDTO.setFashionAccessories(categoryFlags[6]);
        categoryDTO.setKids(categoryFlags[7]);
        categoryDTO.setSportsLeisure(categoryFlags[8]);
        categoryDTO.setDigitalLife(categoryFlags[9]);
        categoryDTO.setBeauty(categoryFlags[10]);
        categoryDTO.setFood(categoryFlags[11]);
        
        return categoryDTO;
	}
	
	//저장
	public void saveCategory(CategoryDTO categoryDTO) {
		int userId=categoryDTO.getUserId();
		
        // 기존 카테고리 삭제
        categoryMapper.deleteCategoryByUserId(userId);
		
        // 카테고리 배열 (순서대로 categoryId와 매칭)
        boolean[] categories = {
            categoryDTO.isOuterwear(),
            categoryDTO.isTops(),
            categoryDTO.isBottoms(),
            categoryDTO.isUnderwearHomewear(),
            categoryDTO.isShoes(),
            categoryDTO.isBags(),
            categoryDTO.isFashionAccessories(),
            categoryDTO.isKids(),
            categoryDTO.isSportsLeisure(),
            categoryDTO.isDigitalLife(),
            categoryDTO.isBeauty(),
            categoryDTO.isFood()
        };

        // 반복문으로 true인 카테고리만 저장
        for (int i = 0; i < categories.length; i++) {
            if (categories[i]) {
            	System.out.println("저장"+userId+"->"+i);//debug
                categoryMapper.saveCategory(userId, i+1); // 주의 categoryId는 1부터 시작
            }
        }
	}
	

}

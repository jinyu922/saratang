package com.swyp.saratang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import com.swyp.saratang.data.RequestList;
import com.swyp.saratang.model.PostDTO;
import com.swyp.saratang.model.PostImageDTO;

@Mapper
public interface PostMapper {
	
	public List<PostDTO> getFashionList(RequestList<?> requestList); //패션정보 조회
	
	public int getFashionListCount(); //패션정보 페이징
	
	public List<String> getImagesByPostId(Integer postId);
	
    @Insert("INSERT INTO posts (user_id, category_id, concern_keyword_id, post_type, brand, original_price, discount_price, product_link, title, current_price, is_spec_public, memo, detail_memo) " +
            "VALUES (#{userId}, #{categoryId}, #{concernKeywordId}, #{postType} , #{brand}, #{originalPrice}, #{discountPrice}, #{productLink}, #{title}, #{currentPrice}, #{isSpecPublic}, #{memo}, #{detailMemo})")
    @Options(useGeneratedKeys = true, keyProperty = "id") //options로 user_id 를 postDTO에 넣기 위해 실행
	public void createFashionPost(PostDTO postDTO); //패션정보 게시
	
	public void insertPostImage(PostImageDTO postImageDTO);
}

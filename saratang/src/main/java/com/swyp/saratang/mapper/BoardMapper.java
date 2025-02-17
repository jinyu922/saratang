package com.swyp.saratang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import com.swyp.saratang.data.RequestList;
import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.model.PostImageDTO;

@Mapper
public interface BoardMapper {
	
	public List<BoardDTO> getFashionList(RequestList<?> requestList); //패션정보 조회
	public List<BoardDTO> getDiscountList(RequestList<?> requestList); //패션정보 조회
	
    @Insert("INSERT INTO posts (user_id, category_id, concern_keyword_id, post_type, brand, original_price, discount_price, product_link, title, current_price, is_spec_public, memo, detail_memo) " +
            "VALUES (#{userId}, #{categoryId}, #{concernKeywordId}, #{postType} , #{brand}, #{originalPrice}, #{discountPrice}, #{productLink}, #{title}, #{currentPrice}, #{isSpecPublic}, #{memo}, #{detailMemo})")
    @Options(useGeneratedKeys = true, keyProperty = "id") //options로 user_id 를 boardDTO에 넣기 위해 실행
	public void createPost(BoardDTO boardDTO); //패션정보 게시
	
	public int getBoardListCount(); //패션정보 페이징
	
	public List<String> getImagesByPostId(Integer postId);
	
	public void insertPostImage(PostImageDTO postImageDTO);
}

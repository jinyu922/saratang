package com.swyp.saratang.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import com.swyp.saratang.data.RequestList;
import com.swyp.saratang.model.BoardDTO;
import com.swyp.saratang.model.CommentDTO;
import com.swyp.saratang.model.PostImageDTO;

@Mapper
public interface BoardMapper {
	
	public List<BoardDTO> getFashionListPaging(RequestList<?> requestList); //패션정보 페이징 조회 (최신순으로 페이징)
	public List<BoardDTO> getFashionListLatest(RequestList<?> requestList); //패션정보 최신순 조회
	public List<BoardDTO> getFashionListPopular(RequestList<?> requestList); //패션정보 인기순 조회
	public List<BoardDTO> getFashionListRandom(RequestList<?> requestList); //패션정보 랜덤 조회
	public List<BoardDTO> getFashionBest(RequestList<?> requestList); //베스트 패션정보 조회
	public List<BoardDTO> getDiscountBest(RequestList<?> requestList); //베스트 할인정보 조회
	
	public BoardDTO getFashionPostById(@Param("id") int id,@Param("postType") String postType);//패션정보 상세조회
	
	public List<BoardDTO> getHistory(RequestList<?> requestList); //히스토리 조회

    @Insert("INSERT INTO posts (user_id, category_id, concern_keyword_id, post_type, brand, original_price, discount_price, product_link, title, current_price, is_spec_public, memo, detail_memo) " +
            "VALUES (#{userId}, #{categoryId}, #{concernKeywordId}, #{postType} , #{brand}, #{originalPrice}, #{discountPrice}, #{productLink}, #{title}, #{currentPrice}, #{isSpecPublic}, #{memo}, #{detailMemo})")
    @Options(useGeneratedKeys = true, keyProperty = "id") //options로 user_id 를 boardDTO에 넣기 위해 실행
	public void createPost(BoardDTO boardDTO); //패션정보 게시
	
	public int getBoardListCount(); //패션정보 페이징
	
	
	public List<String> getImagesByPostId(Integer postId); //NCP object storage 이미지 url 리스트 조회
	public void insertPostImage(PostImageDTO postImageDTO); //NCP object storage 이미지 url 저장
	
	public void insertComment(CommentDTO commentDTO);//댓글 저장
	public List<CommentDTO> getCommentList(@Param("postId") int postId,Pageable pageable);//댓글 조회
	public int getCommentListCount(); //댓글정보 페이징
	
	
	public BoardDTO getBoardById(Integer boardId); //url가져오기
}

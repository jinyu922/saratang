package com.swyp.saratang.mapper;

import com.swyp.saratang.model.OotdDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;


@Mapper
public interface OotdMapper {

    // 특정 Ootd 조회
    OotdDTO getOotdById(@Param("id") int id);

    // 특정 Ootd 존재 여부 확인
    int existOotdById(@Param("id") int id);
    
    // 특정 사용자의 Ootd 존재 여부 확인
    int existOotdByUserId(@Param("userId") int userId);

    // Ootd 업데이트
    void updateOotd(OotdDTO ootd);

    // 좋아요 수 증가
    void incrementOotdLikeCount(@Param("id") int id);

    // 좋아요 수 감소
    void decrementOotdLikeCount(@Param("id") int id);

    // 스크랩 수 증가
    void incrementOotdScrapCount(@Param("id") int id);

    // 스크랩 수 감소
    void decrementOotdScrapCount(@Param("id") int id);

    // Ootd 저장 (작성자 id 는 DTO에 포함해서 전달)
    void insertOotd(OotdDTO ootd);
    
    // Ootd 이미지 url 저장
    void insertOotdImage(@Param("id") int id,@Param("imageUrl") String imageUrl);
    
    // Ootd 이미지 url 삭제 (cascade 설정에 의해 미사용)
    void deleteOotdImageByOotdPostId(@Param("OotdPostId") int OotdPostId);
    
    // Ootd 이미지 url 불러오기
    List<String> getImagesByOotdPostId(@Param("OotdPostId") int OotdPostId);

    // Ootd 삭제
    void deleteOotdById(@Param("id") int id);
    
    // Ootd 좋아요 존재여부 (연결테이블)
    int existOotdLike(@Param("userId") int userId, @Param("ootdPostId") int ootdPostId);

    // Ootd 좋아요 추가 (연결테이블)
    void insertOotdLike(@Param("userId") int userId, @Param("ootdPostId") int ootdPostId);

    // Ootd 좋아요 삭제 (연결테이블)
    void deleteOotdLike(@Param("userId") int userId, @Param("ootdPostId") int ootdPostId);
    
    // Ootd 스크랩 존재여부 (연결테이블)
    int existOotdScrap(@Param("userId") int userId, @Param("ootdPostId") int ootdPostId);
    
    // Ootd 스크랩 추가 (연결테이블)
    void insertOotdScrap(@Param("userId") int userId, @Param("ootdPostId") int ootdPostId);

    // Ootd 스크랩 삭제 (연결테이블)
    void deleteOotdScrap(@Param("userId") int userId, @Param("ootdPostId") int ootdPostId);

    // 최신순 Ootd 리스트 조회 (페이징)
    List<OotdDTO> selectOotds(@Param("pageable") Pageable pageable);

    // 좋아요순 Ootd 리스트 조회 (페이징)
    List<OotdDTO> selectOotdsByLikes(@Param("pageable") Pageable pageable);
    int selectOotdsCount();

    // 특정 사용자가 좋아요한 Ootd 리스트 조회 (페이징)
    List<OotdDTO> selectLikedOotdsByUserId(@Param("userId") int userId, 
                                           @Param("pageable") Pageable pageable);
    int selectLikedOotdsByUserIdCount(@Param("userId") int userId);
    
    // 특정 사용자가 스크랩한 Ootd 리스트 조회 (페이징)
    List<OotdDTO> selectScrapedOotdsByUserId(@Param("userId") int userId, 
    										@Param("pageable") Pageable pageable);
    int selectScrapedOotdsByUserIdCount(@Param("userId") int userId);

    // 특정 사용자가 작성한 Ootd 리스트 조회 (페이징)
    List<OotdDTO> selectOotdsByUserId(@Param("userId") int userId,
    								 @Param("pageable") Pageable pageable);
    int selectOotdsByUserIdCount(@Param("userId") int userId);
}

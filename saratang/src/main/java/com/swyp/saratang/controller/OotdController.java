package com.swyp.saratang.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swyp.saratang.model.OotdDTO;

@RestController
@RequestMapping("/Ootd")
public class OotdController {
	
    // OOTD 저장
    @PostMapping("")
    public void createOotd(@RequestBody OotdDTO ootdDTO) {
        // Logic to create an OOTD
    }

    // OOTD 삭제
    @DeleteMapping("/{id}")
    public void deleteOotd(@PathVariable Long id) {
        // Logic to delete an OOTD by id
    }

    // OOTD 좋아요
    @PostMapping("/{id}/likes")
    public void likeOotd(@PathVariable Long id) {
        // Logic to like an OOTD by id
    }

    // OOTD 좋아요 취소
    @DeleteMapping("/{id}/likes")
    public void unlikeOotd(@PathVariable Long id) {
        // Logic to unlike an OOTD by id
    }

    // OOTD 스크랩
    @PostMapping("/{id}/scraps")
    public void scrapOotd(@PathVariable Long id) {
        // Logic to scrap an OOTD by id
    }

    // OOTD 스크랩 취소
    @DeleteMapping("/{id}/scraps")
    public void unscrapOotd(@PathVariable Long id) {
        // Logic to cancel scrap of an OOTD by id
    }

    // OOTD 조회 (인기순, 최신순)
    @GetMapping("")
    public void getOotds(@RequestParam String sort, @RequestParam int page) {
        // Logic to retrieve OOTDs based on sort (popular, latest) and pagination (page)
    }

    // OOTD 좋아요한 글 조회
    @GetMapping("/liked")
    public void getLikedOotds(@RequestParam int page) {
        // Logic to retrieve liked OOTDs by page
    }

    // OOTD 스크랩한 글 조회
    @GetMapping("/scrapped")
    public void getScrappedOotds(@RequestParam int page) {
        // Logic to retrieve scrapped OOTDs by page
    }

    // OOTD 내가 쓴 글 조회
    @GetMapping("/my")
    public void getMyOotds(@RequestParam int page) {
        // Logic to retrieve OOTDs created by the logged-in user by page
    }

}

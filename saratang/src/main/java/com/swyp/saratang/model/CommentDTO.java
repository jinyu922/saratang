package com.swyp.saratang.model;

import lombok.Data;

@Data
public class CommentDTO {
	private Integer id;//댓글 고유 아이디
	private int postId;
	private int userId;
	private String content;
	
	//추가정보
	private String nickname;
	private String judgement;
	private String color;
}

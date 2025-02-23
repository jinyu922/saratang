package com.swyp.saratang.model;

import lombok.Data;

@Data
public class CommentDTO {
	private Integer id;//댓글 고유 아이디
	private int postId;
	private int userId;
	private String content;
}

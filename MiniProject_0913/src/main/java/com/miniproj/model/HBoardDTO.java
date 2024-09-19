package com.miniproj.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class HBoardDTO {
	private int boardNo;
	private String title;
	private String content;
	private String writer;
	
	private List<BoardUpFilesVODTO> fileList;
}

package com.miniproj.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class PagingInfoDTO {

	private int pageNo;
	private int pagingSize; // 1페이지에 보여줄 글의 갯수
}

package com.miniproj.util;

import javax.servlet.http.HttpServletRequest;

// 로그인을 하지 않았을 때, 로그인 페이지로 이동되기 전에, 원래 가려던 페이지 경로를 저장하는 객체
public class DesatinationPath {
	private String destPath;
	
	//  /hboard/saveBoard
	public void setDestPath(HttpServletRequest req) {
	// 글작성  uri : /hboard/saveBoard
		String uri = req.getRequestURI();
		
		destPath = uri;
		req.getSession().setAttribute("destPath", destPath);
	}
	
	
}

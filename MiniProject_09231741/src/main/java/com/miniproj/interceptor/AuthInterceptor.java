package com.miniproj.interceptor;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.miniproj.service.hboard.HBoardService;
import com.miniproj.util.DesatinationPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthInterceptor extends HandlerInterceptorAdapter {

	// 로그인 인증이 필요한 페이지에서 클라이언트가 현재 로그린 되어 있는지 아닌 검사한다.
	// 로그인 인증이 필요한 페이지(글작성, 글수정, 글삭제 등)
	// 로그인이 되어 있지 않으면, 로그인하도록 하고
	// 로그인이 되어 있다면, 클라이언트가 원래 하려던 작업(목적지)을 하도록 해야 한다.
	@Inject
	private HBoardService service;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		log.info("preHandle()동작중..........");

		new DesatinationPath().setDestPath(request); // 로그인하기 전 호출했던 페이지를 세션에 저장

		HttpSession ses = request.getSession();

		if (ses.getAttribute("loginMember") == null) {
			// 로그인하지 않았다 -> 로그인페이지로 끌려감
			log.info("preHandle()={}", "로그인 하지 않았다.");

			response.sendRedirect("/member/login"); // 로그인페이지로 끌려감
		} else {
			// 로그인했다면 -> 글쓰러 가면 됨
			log.info("preHandle()={}", "로그인 됐다.");

			// 수정이나 삭제 페이지에서 왔다면 그 글에 대한 수정/삭제 권한(본인글) 있는지?

		}

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		log.info("postHandle()동작중..........");
	}
}

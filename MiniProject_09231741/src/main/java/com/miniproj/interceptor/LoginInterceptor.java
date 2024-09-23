package com.miniproj.interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.miniproj.model.MemberDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginInterceptor extends HandlerInterceptorAdapter {
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession ses = request.getSession();
		if(ses.getAttribute("loginMember")!=null) {
			log.info("cleaning loginMember.........");
			ses.removeAttribute("loginMember");
			
		}
		return true; // 해당 컨트롤로단의 메서드로 제어가 들아간다.
		//return false; // 해당 컨트롤로단의 메서드로 제어가 돌아가지않는다.
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		log.info("postHandle() 호출......");
		if(request.getMethod().toUpperCase().equals("POST")) {
			
		Map<String, Object> model = modelAndView.getModel();
		MemberDTO loginMember = (MemberDTO) model.get("loginMember");
		
		log.info("loginMember :" + loginMember);
		
		if(loginMember != null) {
			//세션에 로그인한 유저의 정보를 저장
			HttpSession ses = request.getSession();
			ses.setAttribute("loginMember", loginMember);
			
			// 로그인하기 이전에 저장한 경로가 있다면, 그 쪽으로 가고
			// 없다면 "/"로 페이지 이동
			
			String tmp = (String) ses.getAttribute("destPath");
			log.info("가야할 곳 : {}",tmp);
			
			response.sendRedirect((tmp == null) ? "/" : tmp);
		}else { // 로그인 실패
			 response.sendRedirect("/member/login?status=fail");
		}
		}
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		System.out.println("afterCompletion() 동작...");
		super.afterCompletion(request, response, handler, ex);
	}

}

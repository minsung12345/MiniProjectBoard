package com.miniproj.controller;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.miniproj.model.HBoardVO;
import com.miniproj.model.MyResponseWithoutData;
import com.miniproj.service.hboard.HBoardService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Inject
	private HBoardService hService;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);

		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);

		String formattedDate = dateFormat.format(date);

		model.addAttribute("serverTime", formattedDate);

		return "index";
	}

	@GetMapping("/getBoardTop5")
	@ResponseBody
	public ResponseEntity<List<HBoardVO>> getTopBoard() {
		try {
			List<HBoardVO> lst = hService.getTopBoards();
			return new ResponseEntity<List<HBoardVO>>(lst, HttpStatus.OK);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}

	}

	@RequestMapping("/saveCookie")
	public ResponseEntity<String> saveCookie(HttpServletResponse response) {

		Cookie myCookie = new Cookie("notice", "N"); // name, value
		myCookie.setMaxAge(60 * 60 * 24); // 쿠키 만료일 설정

		response.addCookie(myCookie); // 쿠키를 응답 객체에 실어 보냄

		return new ResponseEntity<String>("success", HttpStatus.OK);
	}

	@RequestMapping(value = "/readCookie", produces = "application/json; charset=UTF-8;")
	public ResponseEntity<MyResponseWithoutData> readCookie(HttpServletRequest request) {
		// 응답 데이터 타입이 "json"인 경우
		MyResponseWithoutData result = null;

		Cookie[] cookies = request.getCookies();
		// 이름이 notice인 쿠키가 있는지 확인
		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equals("notice") && cookies[i].getValue().equals("N")) {
				// 이름이 notice인 쿠키가 있고, 그 값이 N 이다..
				result = new MyResponseWithoutData(200, null, "success");
			}
		}

		if (result == null) {
			result = new MyResponseWithoutData(400, null, "fail");
		}

		return new ResponseEntity<MyResponseWithoutData>(result, HttpStatus.OK);
	}

	@RequestMapping("/exampleInterceptor")
	public void examInterceptor() {
		System.out.println("exampleInterceptor() 호출 !!!");
	}

	@RequestMapping("/example")
	public void testInterceptor() {
		System.out.println("testInterceptor() 호출 !!!");
	}
}

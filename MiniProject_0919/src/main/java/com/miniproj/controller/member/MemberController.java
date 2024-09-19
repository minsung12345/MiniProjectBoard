package com.miniproj.controller.member;

import java.util.UUID;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.miniproj.model.MyResponseWithoutData;
import com.miniproj.service.member.MemberService;
import com.miniproj.util.SendMailService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/member")
@Controller
public class MemberController {

	@Inject
	private MemberService mService;

	@RequestMapping("/register")
	public void showRegisterForm() {

	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public void registerMember() {

	}

	@RequestMapping(value = "/isDuplicate", method = RequestMethod.POST)
	public ResponseEntity<MyResponseWithoutData> idIsDuplicate(@RequestParam("tmpUserId") String tmpUserId){
		System.out.println(tmpUserId + "가 중복되는지 확인하자");
		
		MyResponseWithoutData json = null;
		ResponseEntity<MyResponseWithoutData> result = null;
		
		try {
			
		if( mService.idIsDuplicate(tmpUserId)) {
			//아이디가 중복된다
			json = new MyResponseWithoutData(200, tmpUserId, "duplicate");
		}else {
			//아이디가 중복되지 않는다
			json = new MyResponseWithoutData(200, tmpUserId, "not duplicate");
		}
		result = new ResponseEntity<MyResponseWithoutData>(json,HttpStatus.OK);
	}
	catch (Exception e) {
		e.printStackTrace();
		result = new ResponseEntity<MyResponseWithoutData>(HttpStatus.CONFLICT);
		
	}
		return result;
	} 
	@RequestMapping(value="/callSendMail")
	public void sendMailAuthCode(@RequestParam("tmpUserEmail") String tmpUserEmail) {
		System.out.println("tmpUserEmail : " + tmpUserEmail + "로 이메일을 보내자");
		String authCode = UUID.randomUUID().toString();
		try {
			new SendMailService().sendMail(tmpUserEmail, authCode);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}

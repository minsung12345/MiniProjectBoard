package com.miniproj.controller.member;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.miniproj.model.LoginDTO;
import com.miniproj.model.MemberDTO;
import com.miniproj.model.MyResponseWithoutData;
import com.miniproj.service.member.MemberService;
import com.miniproj.util.FileProcess;
import com.miniproj.util.SendMailService;
import com.mysql.cj.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/member")
@Controller
public class MemberController {

	@Inject
	private MemberService mService;

	private FileProcess fp;
	
	@RequestMapping("/register")
	public void showRegisterForm() {

	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public void registerMember(MemberDTO registerMember, MultipartFile userProfile , HttpServletRequest request ,RedirectAttributes rttr) {
		System.out.println("회원 가입 진행하자~~" + registerMember.toString());
		System.out.println(userProfile.getOriginalFilename());
		
		String resultPage = "redirect:/"; // 회원가입 완료 후 index.jsp로 가자
		
		String realPath =request.getSession().getServletContext().getRealPath("/resources/userImg");
		System.out.println("실제 파일 경로 : "+ realPath);
		
		String tmpUserProfileName = userProfile.getOriginalFilename();
		if(StringUtils.isNullOrEmpty(tmpUserProfileName)) {
			String ext = tmpUserProfileName.substring(tmpUserProfileName.lastIndexOf(".")+1);
			registerMember.setUserImg(registerMember.getUserId()+"." +ext);
		}
		try {
			if(mService.saveMember(registerMember)) {
				
				rttr.addAttribute("status","success");
				//프로필을 올렸는지 확인 -> 프로필 사진을 업로드 했다면
				if(!StringUtils.isNullOrEmpty(tmpUserProfileName)) {
					fp.saveUserProfile(userProfile.getBytes(),realPath,registerMember.getUserImg());{
						
					}
				}
			};
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		if(e instanceof IOException) {
			//회원가입한 유저의 회원가입 취소 처리 (service-dao)
			rttr.addAttribute("status","fileFail");
		}else {
			rttr.addAttribute("status","fail");
		}
		resultPage = "redirect:/member/register"; // 실패한 경우 -> 다시 회원가입 페이지로
		}
		
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
	public ResponseEntity<String> sendMailAuthCode(@RequestParam("tmpUserEmail") String tmpUserEmail, HttpSession session) {
		System.out.println("tmpUserEmail : " + tmpUserEmail + "로 이메일을 보내자");
		String result = "";
		String authCode = UUID.randomUUID().toString();
		System.out.println("authCode: " +authCode);
		try {
			new SendMailService().sendMail(tmpUserEmail, authCode);
			
			//인증코드를 세션 객체에 저장
			session.setAttribute("authCode", authCode);
			result = "success";
			
		} catch (Exception e) {
			e.printStackTrace();
			result = "fail";
		}
		return new ResponseEntity<String>(result,HttpStatus.OK);
	}
	@RequestMapping(value="/checkAuthCode",method = RequestMethod.POST)
	public ResponseEntity<String> checkAuthCode(@RequestParam("tmpUserAuthCode")String tmpUserAuthCode, HttpSession session) {
		System.out.println("tmpUserAuthCode : " + tmpUserAuthCode + "우리가 보낸 코드 확인");
		
		System.out.println("session에 저장된 인증코드 : " + session.getAttribute("authCode"));
		String result = "fail";
		if(session.getAttribute("authCode") != null) {
			String sesAuthCode = (String) session.getAttribute("authCode");
			if(tmpUserAuthCode.equals(sesAuthCode)) {
				result = "success";
			}
		}
		return new ResponseEntity<String>(result, HttpStatus.OK);
		
	}
	@RequestMapping("/clearAuthCode")
	public ResponseEntity<String> clearCode(HttpSession session) {
		if(session.getAttribute("authCode") != null) {
			//세션에 저장된 인증코드를 삭제
			session.removeAttribute("authCode");
		}
		return new ResponseEntity<String>("success",HttpStatus.OK);
	}
	// 로그인
	@RequestMapping("/login")
	public void loginGET() {
		// login.jsp 응답
	}
	@RequestMapping(value="/login", method = RequestMethod.POST)
	public void loginPOST(LoginDTO loginDTO, Model model) {
		log.info(loginDTO + "으로 로그인하자~");
		String result = "";
		
		try {
			MemberDTO loginMember = mService.login(loginDTO);
			if(loginMember != null) {
				System.out.println(loginMember.toString());
				log.info("로그인 성공");
				//로그인 정보를 model객체에 저장 -> 인터셉터로 넘겨서 나머지 로그인 처리를 하자.
				model.addAttribute("loginMember", loginMember);
				
//				session.setAttribute("loginMember", loginMember);
//				result = "redirect:/";
			}else {
				log.info("로그인 실패");
				result = "redirect:/member/login";
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@RequestMapping("/logout")
	public String logoutMember(HttpSession session) {
		if(session.getAttribute("loginMember") != null) {
			//세션에 저장된 값들 삭제
			session.removeAttribute("loginMember");
			//세션 무효화
			session.invalidate();
		}
		return "redirect:/";
	}
}

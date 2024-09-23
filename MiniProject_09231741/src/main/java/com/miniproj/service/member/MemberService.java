package com.miniproj.service.member;

import com.miniproj.model.LoginDTO;
import com.miniproj.model.MemberDTO;

public interface MemberService {
	//아이디 중복검사
	boolean idIsDuplicate(String tmpUserId) throws Exception;

	//회원가입
	boolean saveMember(MemberDTO registerMember) throws Exception;

	//로그인
	MemberDTO login(LoginDTO loginDTO) throws Exception;
}

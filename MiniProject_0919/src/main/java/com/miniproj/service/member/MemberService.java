package com.miniproj.service.member;

public interface MemberService {
	//아이디 중복검사
	boolean idIsDuplicate(String tmpUserId) throws Exception;
}

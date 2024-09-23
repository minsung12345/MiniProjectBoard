package com.miniproj.service.member;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.miniproj.model.LoginDTO;
import com.miniproj.model.MemberDTO;
import com.miniproj.model.PointLogDTO;
import com.miniproj.persistence.MemberDAO;
import com.miniproj.persistence.PointLogDAO;

@Service
public class MemberServiceImpl implements MemberService {
	
	@Inject
	private MemberDAO mDao;
	
	@Inject
	private PointLogDAO pDao;
	
	@Override
	public boolean idIsDuplicate(String tmpUserId) throws Exception {
		boolean result = false;
		if(mDao.selectDuplicateId(tmpUserId)==1){
			result = true; // 중복된다!
		}
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
	public boolean saveMember(MemberDTO registerMember) throws Exception {
		boolean result = false;
		// 멤버 : 취미 = 1:N -> 취미를 저장할 테이블을 별도로 만들지 않기 위해
		// 여러개의 취미를 하나의 문자열로 저장
		
		String tmphobby = "";
		
		for(int i=0; i<registerMember.getHobbies().length;i++) {
			if(i == registerMember.getHobbies().length - 1) {
				tmphobby += registerMember.getHobbies()[i];
			}else {
				tmphobby += registerMember.getHobbies()[i]+", ";
			}
		}
		registerMember.setHobby(tmphobby);
		
		
		// 회원가입
		// 1) 회원데이터를 DB에 저장 (insert)
		if(mDao.insertMember(registerMember)==1) {
			// 2) 가입한 회원에게 100포인트 부여 (로그 기록 - insert)
			if(pDao.insertPointLog(new PointLogDTO(registerMember.getUserId(),"회원가입")) == 1 ) {
				result = true;
			}
		};
		return result;
		// 
	}

	@Override
	public MemberDTO login(LoginDTO loginDTO) throws Exception {
		MemberDTO loginMember = mDao.login(loginDTO);
		return loginMember;
	}

}

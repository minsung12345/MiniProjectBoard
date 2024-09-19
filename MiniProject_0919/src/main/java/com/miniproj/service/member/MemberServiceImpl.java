package com.miniproj.service.member;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.miniproj.persistence.MemberDAO;

@Service
public class MemberServiceImpl implements MemberService {
	
	@Inject
	private MemberDAO mDao;
	
	@Override
	public boolean idIsDuplicate(String tmpUserId) throws Exception {
		boolean result = false;
		if(mDao.selectDuplicateId(tmpUserId)==1){
			result = true; // 중복된다!
		}
		return result;
	}

}

package com.miniproj.persistence;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.miniproj.model.LoginDTO;
import com.miniproj.model.MemberDTO;

@Repository
public class MemberDAOImpl implements MemberDAO {
	@Inject
	SqlSession ses;
	
	private String ns = "com.miniproj.mappers.memberMapper.";
	
	@Override
	public int updateUserPoint(String userId) throws Exception {
		
		return ses.update(ns + "updateUserPoint", userId);
	}

	@Override
	public int selectDuplicateId(String tmpUserId) throws Exception {
		return ses.selectOne(ns +"selectUserId", tmpUserId);
	}

	@Override
	public int insertMember(MemberDTO registerMember) throws Exception {
		return ses.insert(ns + "insertMember", registerMember);
	}

	@Override
	public MemberDTO login(LoginDTO loginDTO) throws Exception {
		return ses.selectOne(ns+"loginWithLoginDTO",loginDTO);
	}

	
}

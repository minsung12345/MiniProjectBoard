package com.miniproj.persistence;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

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

}

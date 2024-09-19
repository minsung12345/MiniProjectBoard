package com.miniproj.persistence;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.miniproj.model.PointLogDTO;

@Repository
public class PointLogDAOImpl implements PointLogDAO {

	@Inject
	private SqlSession ses;
	
	private static String ns = "com.miniproj.mappers.pointlogmapper.";
	
	@Override
	public int insertPointLog(PointLogDTO pointLogDTO) throws Exception {
		return ses.insert(ns + "insertPointLog", pointLogDTO);
	}

}

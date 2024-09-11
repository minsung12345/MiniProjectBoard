package com.miniproj.persistence;

import com.miniproj.model.PointLogDTO;

public interface PointLogDAO {
	// pointlog에 저장하는 메서드
	int insertPointLog(PointLogDTO pointLogDTO) throws Exception;
}

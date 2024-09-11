package com.miniproj.persistence;

import java.util.List;

import com.miniproj.model.BoardDetailInfo;
import com.miniproj.model.BoardUpFilesVODTO;
import com.miniproj.model.HBoardDTO;
import com.miniproj.model.HBoardReplyDTO;
import com.miniproj.model.HBoardVO;

public interface HBoardDAO {
	// 게시판 전체 목록 가져오는 메서드
	List<HBoardVO> selectAllBoard() throws Exception;

	// 게시글 저장
	int insertNewBoard(HBoardDTO newBoard) throws Exception;

	// 최근 저장된 글의 글번호를 얻어오는 메서드
	int selectMaxBoardNo() throws Exception;

	// 업로드된 첨부파일을 저장하는 메서드
	int insertBoardUpFile(BoardUpFilesVODTO file) throws Exception;

	
	
	// 유저가 입력한 텍스트만 hboard에서 조회
	HBoardVO selectBoardByBoardNo(int boardNo) throws Exception;

	// 업로드한 파일만 조회
	List<BoardUpFilesVODTO> selecyBoardUpfileByBoardNo(int boardNo) throws Exception;
	
	
	// resultmap 테스트
	HBoardDTO testResultMap(int boardNo) throws Exception;
	
	// 게시글 상세정보
	List<BoardDetailInfo> selectBoardDetailByBoardNo(int boardNo) throws Exception;

	// 조회수 처리
	
	int selectDateDiff(String ipAddr, int boardNo) throws Exception;

	int insertBoardReadLog(String ipAddr, int boardNo) throws Exception;

	int updateReadwhen(String ipAddr, int boardNo) throws Exception;

	int updateReadCount(int boardNo) throws Exception;

	// 저장된 게시글의 글 번호를 ref컬럼에 update
	void updateBoardRef(int newBoardNo) throws Exception;
	
	// refORder 업데이트 (답글처리 : 자리확보)
	void updateRefOrder(int ref, int refOrder) throws Exception;
	
	// 답글 저장
	int insertReplyBoard(HBoardReplyDTO replyBoard) throws Exception;
	
	// 업로드 파일 삭제
	void deleteAllBoardUpFiles(int boardNo) throws Exception;

	// 게시글 삭제
	int deleteBoardByBoardNo(int boardNo) throws Exception;
}

package com.miniproj.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.miniproj.model.BoardDetailInfo;
import com.miniproj.model.BoardUpFilesVODTO;
import com.miniproj.model.HBoardDTO;
import com.miniproj.model.HBoardReplyDTO;
import com.miniproj.model.HBoardVO;
import com.miniproj.model.PagingInfo;
import com.miniproj.model.SearchCriteriaDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository // 아래의 클래스가 DAO객체임을 명시
public class HBoardDAOImpl implements HBoardDAO {

	@Inject
	private SqlSession ses;
	
	private static String ns = "com.miniproj.mappers.hboardmapper.";
	
	@Override
	public List<HBoardVO> selectAllBoard() throws Exception {
//		log.info("Here is HBoardDAOImpl......");
		return ses.selectList(ns + "getAllHBoard"); // select 쿼리태그의 id를 잘못 준 경우 
	}

	@Override
	public int insertNewBoard(HBoardDTO newBoard) throws Exception {
		return ses.insert(ns + "saveNewBoard", newBoard);
		
	}

	@Override
	public int selectMaxBoardNo() throws Exception {
		return ses.selectOne(ns + "getMAxNo");
	}

	@Override
	public int insertBoardUpFile(BoardUpFilesVODTO file) throws Exception {
		return ses.insert(ns + "saveUpFile", file);
	}

	@Override
	public HBoardVO selectBoardByBoardNo(int boardNo) throws Exception {
//		System.out.println(ses.selectOne(ns + "selectBoardByBoardNo", boardNo).toString());
		return ses.selectOne(ns + "selectBoardByBoardNo", boardNo);
	}

	@Override
	public List<BoardUpFilesVODTO> selecyBoardUpfileByBoardNo(int boardNo) throws Exception {
//		System.out.println(ses.selectList(ns + "selecyBoardUpfileByBoardNo", boardNo).toString());
		return ses.selectList(ns + "selecyBoardUpfileByBoardNo", boardNo);
	}

	@Override
	public HBoardDTO testResultMap(int boardNo) throws Exception {
		return ses.selectOne(ns + "selectResultmapTest", boardNo);
	}

	@Override
	public List<BoardDetailInfo> selectBoardDetailByBoardNo(int boardNo) throws Exception {
		return ses.selectList(ns + "selectBoardDetailInfoByBoardNo", boardNo);
	}

	@Override
	public int selectDateDiff(String ipAddr, int boardNo) throws Exception {
		// 넘겨줘야할 파라메터가 2개 이상이면, Map을 이용하여 파라메터를 매핑하여 넘겨준다
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("readWho", ipAddr);
		params.put("boardNo", boardNo);
		return ses.selectOne(ns + "selectBoardDateDiff", params);
	}

	@Override
	public int insertBoardReadLog(String ipAddr, int boardNo) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("readWho", ipAddr);
		params.put("boardNo", boardNo);
		return ses.insert(ns + "saveBoardReadLog", params);
	}

	@Override
	public int updateReadwhen(String ipAddr, int boardNo) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("readWho", ipAddr);
		params.put("boardNo", boardNo);
		return ses.update(ns + "updateBoardReadLog", params);
	}

	@Override
	public int updateReadCount(int boardNo) throws Exception {
		return ses.update(ns + "updateReadCount", boardNo);
	}

	@Override
	public void updateBoardRef(int newBoardNo) throws Exception {
		ses.update(ns + "updateBoardRef", newBoardNo);
	}

	@Override
	public void updateRefOrder(int ref, int refOrder) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ref", ref);
		params.put("refOrder", refOrder);
		
		ses.update(ns + "updateBoardRefOrder", params);
	}

	@Override
	public int insertReplyBoard(HBoardReplyDTO replyBoard) throws Exception {
		return ses.insert(ns + "insertReplyBoard", replyBoard);
	}
	
	@Override
	public void deleteAllBoardUpFiles(int boardNo) throws Exception {
		ses.delete(ns + "deleteBoardUpfileByPK", boardNo);
	}

	@Override
	public int deleteBoardByBoardNo(int boardNo) throws Exception {
		// isDelete컬럼 update
		return ses.update(ns + "deleteBoardByBoardNo", boardNo);
	}

	@Override
	public int updateBoardByBoardNo(HBoardDTO modifyBoard) throws Exception {
		return ses.update(ns + "updateBoardByBoardNo", modifyBoard);
	}

	@Override
	public void deleteBoardUpFile(int boardUpFileNo) throws Exception {
		ses.delete(ns + "deleteBoardUpFileModify", boardUpFileNo);
	}

	@Override
	public int getTotalPostCnt() throws Exception {
		return ses.selectOne(ns + "selectTotalCount");
	}

	@Override
	public List<HBoardVO> selectAllBoard(PagingInfo pi) throws Exception {
		return ses.selectList(ns + "getAllHBoard", pi);
	}

	@Override
	public List<HBoardVO> selectAllBoard(SearchCriteriaDTO searchCriteriaDTO) throws Exception {
		return ses.selectList(ns + "getSearchBoard", searchCriteriaDTO);
	}

	@Override
	public int getTotalPostCnt(SearchCriteriaDTO sc) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("searchType", sc.getSearchType());
		params.put("searchWord", "%" + sc.getSearchWord() + "%");
		
		return ses.selectOne(ns + "selectTotalCountWithSearchCriteria", params);
	}

	@Override
	public List<HBoardVO> selectAllBoard(PagingInfo pi, SearchCriteriaDTO sc) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("startRowIndex", pi.getStartRowIndex());
		params.put("viewPostCntPerPage", pi.getViewPostCntPerPage());
		params.put("searchType", sc.getSearchType());
		params.put("searchWord", "%" + sc.getSearchWord() + "%");
		
		return ses.selectList(ns + "getSearchBoard", params);
	}

	@Override
	public List<HBoardVO> selectTopBoards() throws Exception {
		return ses.selectList(ns + "selectTopBoards");
	}

}

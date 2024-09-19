package com.miniproj.service.hboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.miniproj.model.BoardDetailInfo;
import com.miniproj.model.BoardUpFileStatus;
import com.miniproj.model.BoardUpFilesVODTO;
import com.miniproj.model.HBoardDTO;
import com.miniproj.model.HBoardReplyDTO;
import com.miniproj.model.HBoardVO;
import com.miniproj.model.PagingInfo;
import com.miniproj.model.PagingInfoDTO;
import com.miniproj.model.PointLogDTO;
import com.miniproj.model.SearchCriteriaDTO;
import com.miniproj.persistence.HBoardDAO;
import com.miniproj.persistence.MemberDAO;
import com.miniproj.persistence.PointLogDAO;
import com.mysql.cj.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

// Service단에서 해야 할 작업
// 1) Controller에서 넘겨진 파라미터를 처리한 후 (비즈로직에 의해...트랜잭션 처리를 통해)
// 2) DB작업이 필요하다면 DAO단 호출...
// 3) DAO단에서 반환된 값을 Controller단으로 넘겨준다.


@Slf4j
@Service // 아래의 클래스가 서비스 객체임을 컴파일러 공지
public class HBoardServiceImpl implements HBoardService {

	@Inject
	private HBoardDAO bDao;
	
	@Inject
	private PointLogDAO pDao;
	
	@Inject
	private MemberDAO mDao;
	
	
	@Override
	public List<HBoardVO> getAllBoard() throws Exception {
		System.out.println("HBoardServiceImpl........");
//		log.info("HBoardServiceImpl........");
		
	    List<HBoardVO> lst = bDao.selectAllBoard(); // DAO 메서드 호출 
		
		return lst;
	}

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public boolean saveBoard(HBoardDTO newBoardDTO) throws Exception {
		boolean result = false;
		
		// 1) newBoard를 DAO단을 통해서 insert(유저가 입력한 텍스트)한다.
		 if (bDao.insertNewBoard(newBoardDTO) == 1) { // 10번 글 저장
			 // 첨부파일이 있다면, 첨부파일 또한 저장한다. boardNo = 10
			  // 1-1) 방금 저장된 게시글의 번호 
			 	int newBoardNo = bDao.selectMaxBoardNo();
			 	
			 	// 1-1-1) 1-1)에서 가져온 글 번호를 ref컬럼에 update
			 	 bDao.updateBoardRef(newBoardNo);
			 	
			 // 1-2) 1-1)에서 얻어온 게시글번호를 참조하는 첨부파일정보를 insert해야 한다.
			 	
			 	for (BoardUpFilesVODTO file : newBoardDTO.getFileList()) {
			 		file.setBoardNo(newBoardNo);
			 		bDao.insertBoardUpFile(file);
			 	}
			 
			 // 2) 1)에서 insert가 성공하면, pointlog에 저장
			 if (pDao.insertPointLog(new PointLogDTO(newBoardDTO.getWriter(), "글작성")) == 1) {
				 // 3) 작성자의 userPoint값을 update한다.
				 if (mDao.updateUserPoint(newBoardDTO.getWriter()) == 1) {
					 result = true;
				 }
			 }
		 }
		return result;
	}

	@Override
	public HashMap<String, Object> viewBoardByBoardNo(int boardNo) throws Exception {
//			bDao.selectBoardByBoardNo(boardNo); // 유저가 입력한 텍스트만 hboard에서 조회
//			bDao.selecyBoardUpfileByBoardNo(boardNo); // 업로드한 파일만 조회
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("board", bDao.selectBoardByBoardNo(boardNo));
			map.put("fileList", bDao.selecyBoardUpfileByBoardNo(boardNo));
			
			return map;
	}

	@Override
	public HBoardDTO testResultMap(int boardNo) throws Exception {
		return bDao.testResultMap(boardNo);
	}

	@Override
	@Transactional( rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
	public List<BoardDetailInfo> read(int boardNo, String ipAddr) throws Exception {
		List<BoardDetailInfo> boardInfo = bDao.selectBoardDetailByBoardNo(boardNo); 
		
		// 조회수 증가
		// dateDiff = 날짜차이계산결과
		int dateDiff = bDao.selectDateDiff(ipAddr, boardNo);
		
		if (dateDiff == -1) { // ipAddr가 boardNo번 글을 최초로 조회
			// ipAddr가 boardNo번 글을 읽은 시간을 테이블에 저장 (insert)
			if (bDao.insertBoardReadLog(ipAddr, boardNo) == 1) {
				// 조회수 증가 (+1)
				updateReadCount(boardNo, boardInfo);
			}
			
		} else if (dateDiff >= 1) { // ipAddr가 boardNo번 글을 다시 조회
			//  ipAddr가 boardNo번 글을 읽은 시간을 테이블에 수정 (update)
			bDao.updateReadwhen(ipAddr, boardNo);
			// 조회수 증가 (+1)
			updateReadCount(boardNo, boardInfo);
		}
		
		return boardInfo;
	}

	private void updateReadCount(int boardNo, List<BoardDetailInfo> boardInfo) throws Exception {
		if (bDao.updateReadCount(boardNo) == 1) {
			for (BoardDetailInfo b : boardInfo) {
				b.setReadCount(b.getReadCount() + 1);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public boolean saveReply(HBoardReplyDTO replyBoard) throws Exception {
		boolean result = false;
		
		System.out.println("답글저장 서비스 호출");
	//		부모글에 대한 다른 답글이 있는 상태에서, 부모글의 답글이 추가되는 경우,
	//		-- (자리확보를 위해) 기존의 답글의 refOrder값을 수정(+1)해야 한다.
		bDao.updateRefOrder(replyBoard.getRef(), replyBoard.getRefOrder()); // update
		
	//		 답글을 입력받아서, 답글 저장
	//		 -- ref = pRef, step = pStep + 1, refOrder = pRefOrder + 1로 등록한다.
		replyBoard.setStep(replyBoard.getStep() + 1); // step = pStep + 1
		replyBoard.setRefOrder(replyBoard.getRefOrder() + 1); // refOrder = pRefOrder + 1
		if (bDao.insertReplyBoard(replyBoard) == 1) { // insert
			result = true;
		}
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<BoardUpFilesVODTO> removeBoard(int boardNo) throws Exception {
		// 게시글 삭제 처리
		// 1) 삭제해야할 업로드파일 정보를 불러오자
		List<BoardUpFilesVODTO> fileList = bDao.selecyBoardUpfileByBoardNo(boardNo); // select
		
		// 2) boardNo번글의 첨부파일을 테이블에서 삭제하자.
		bDao.deleteAllBoardUpFiles(boardNo); // delete
		
		// 3) boardNo글을 hboard테이블에서 삭제하자.
		if (bDao.deleteBoardByBoardNo(boardNo) == 1) { // update
			return fileList;
		} else {
			return null;
		}
		
	}

	@Override
	public List<BoardDetailInfo> read(int boardNo) throws Exception {
		List<BoardDetailInfo> boardInfo = bDao.selectBoardDetailByBoardNo(boardNo);
		return boardInfo;
	}

	// 게시글 수정 처리
	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = Exception.class)
	public boolean modifyBoard(HBoardDTO modifyBoard) throws Exception {
		boolean result = false;
		
		System.out.println(modifyBoard.toString());
		System.out.println(modifyBoard.getFileList().size() + "개의 파일");
		
		// 1) 순수게시글 update
			if (bDao.updateBoardByBoardNo(modifyBoard) == 1) {
				// 2) 업로드파일의 fileStatus = INSERT이면 insert, DELETE이면 delete
					List<BoardUpFilesVODTO> fileList = modifyBoard.getFileList();
					
					for (BoardUpFilesVODTO file : fileList) {
						if (file.getFileStatus() == BoardUpFileStatus.INSERT) {
							file.setBoardNo(modifyBoard.getBoardNo()); // 저장되는 파일의 글번호를 수정되는 글의 글번호로 세팅
							bDao.insertBoardUpFile(file);
						} else if (file.getFileStatus() == BoardUpFileStatus.DELETE) {
							bDao.deleteBoardUpFile(file.getBoardUpFileNo());
						}
					}
					
					result = true;
			}
		
		return result;
	}

	@Override
	public Map<String, Object> getAllBoard(PagingInfoDTO dto) throws Exception {
		PagingInfo pi = makePagingInfo(dto);
		List<HBoardVO> lst = bDao.selectAllBoard(pi);
		
//		for (HBoardVO hBoardVO : lst) {
//			System.out.println(hBoardVO.toString());
//		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("pagingInfo", pi);
		resultMap.put("boardList", lst);
		
		return resultMap;
	}

	private PagingInfo makePagingInfo(PagingInfoDTO dto) throws Exception {
		PagingInfo pi = new PagingInfo(dto);
		
		// setter호출
		pi.setTotalPostCnt(bDao.getTotalPostCnt()); // 전체 글(데이터)의 수
		
		pi.setTotalPageCnt(); // 전체 페이지 수 세팅
		pi.setStartRowIndex(); // 현재 페이지에서 보여주기 시작할 글의 index번호
		
		// 페이징 블럭
		pi.setPageBlockNoCurPage();
		pi.setStartPageNoCurBlock();
		pi.setEndPageNoCurBlock();
		
		log.info(pi.toString());
		
		return pi;
	}

	@Override
	public Map<String, Object> getAllBoard(PagingInfoDTO dto, SearchCriteriaDTO searchCriteriaDTO) throws Exception {
//		searchCriteriaDTO.setSearchWord("%" + searchCriteriaDTO.getSearchWord() + "%");
		
		PagingInfo pi = makePagingInfo(dto, searchCriteriaDTO);
		
		List<HBoardVO> lst = null;
		
		if (StringUtils.isNullOrEmpty(searchCriteriaDTO.getSearchType()) && StringUtils.isNullOrEmpty(searchCriteriaDTO.getSearchWord())) {
			lst = bDao.selectAllBoard(pi);
		} else {
			lst = bDao.selectAllBoard(pi, searchCriteriaDTO);
		}
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("pagingInfo", pi);
		resultMap.put("boardList", lst);
		
		return resultMap;
	}

	private PagingInfo makePagingInfo(PagingInfoDTO dto, SearchCriteriaDTO sc) throws Exception {
		PagingInfo pi = new PagingInfo(dto);
		
		// setter호출
		log.info("검색된 글의 갯수: " + bDao.getTotalPostCnt(sc));
		
		System.out.println(StringUtils.isNullOrEmpty("")); // true
		System.out.println(StringUtils.isNullOrEmpty(null)); // true
		System.out.println(StringUtils.isNullOrEmpty("test")); // false
		
		
		if (StringUtils.isNullOrEmpty(sc.getSearchType()) && StringUtils.isNullOrEmpty(sc.getSearchWord())) {
			pi.setTotalPostCnt(bDao.getTotalPostCnt());
		} else {
			pi.setTotalPostCnt(bDao.getTotalPostCnt(sc));
		}
		
		pi.setTotalPageCnt(); // 전체 페이지 수 세팅
		pi.setStartRowIndex(); // 현재 페이지에서 보여주기 시작할 글의 index번호
		
		// 페이징 블럭
		pi.setPageBlockNoCurPage();
		pi.setStartPageNoCurBlock();
		pi.setEndPageNoCurBlock();
		
		log.info(pi.toString());
		
		return pi;
	}

	@Override
	public List<HBoardVO> getTopBoards() throws Exception {

		return bDao.selectTopBoards();
	}
}

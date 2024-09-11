package com.miniproj.controller.hboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import com.miniproj.model.BoardDetailInfo;
import com.miniproj.model.BoardUpFileStatus;
import com.miniproj.model.BoardUpFilesVODTO;
import com.miniproj.model.HBoardDTO;
import com.miniproj.model.HBoardReplyDTO;
import com.miniproj.model.HBoardVO;
import com.miniproj.model.MyResponseWithoutData;
import com.miniproj.service.hboard.HBoardService;
import com.miniproj.util.FileProcess;
import com.miniproj.util.GetClientIPAddr;

// Controller단에서 해야 할 일
// 1) URI 매핑 (어떤 URI가 어떤방식 (GET/POST)으로 호출되었을 때 어떤 메서드에 매핑 시킬 것이냐)
// 2) 있다면 view단에서 보내준 매개 변수 수집
// 3) 데이터베이스에 대한 CRUD를 수행하기 위해 service단의 해당 메서드를 호출. 
//    service단에서 return 값을 view으로 바인딩+ view단 호출
// 4) 부가적으로 컨트롤러단은 Servlet에 의해 동작되는 모듈이기 때문에, HttpServletRequest, HttpServletResponse, 
//     HttpSession 등의 Servlet객체를 이용할 수 있다. -> 이러한 객체들을 이용하여 구현할 기능이 있다면, 
//     그 기능은 컨트롤러단에서 구현한다. 

@Controller // 아래의 클래스가 컨트롤러 객체임을 명시
@RequestMapping("/hboard")
public class HBoardController {

	private static final Logger logger = LoggerFactory.getLogger(HBoardController.class);

	// 유저가 업로드한 파일을 임시 보관하는 객체(컬렉션)
	private List<BoardUpFilesVODTO> uploadFileList = new ArrayList<BoardUpFilesVODTO>();

	private List<BoardUpFilesVODTO> modifyFileList; // 수정요청시 유저가 업로드한 파일을 저장

	@Inject
	private HBoardService service; // 서비스객체 주입

	@Inject
	private FileProcess fileProcess; // FileProcess객체 주입

	@RequestMapping("/listAll") // /hboard/listAll
	public void listAll(Model model) {
		logger.info("HBoardController.listAll().................");

		List<HBoardVO> lst;
		try {
			lst = service.getAllBoard();
			model.addAttribute("boardList", lst); // model객체에 데이터 바인딩

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("exception", "error");
		} // 서비스 메서드 호출

//		for (HBoardVO b : lst) {
//			System.out.println(b.toString());
//		}
	}

	// 게시판 글 저장페이지를 출력하는 메서드
	@RequestMapping(value = "/saveBoard", method = RequestMethod.GET)
	public String showSaveBoardForm() {
		return "/hboard/saveBoardForm";
	}

	// 게시글 저장 버튼을 누르면 해당 게시글을 DB에 저장하는 메서드
	@RequestMapping(value = "/saveBoard", method = RequestMethod.POST)
	public String saveBoard(HBoardDTO boardDTO, RedirectAttributes rttr) {
		System.out.println("글 저장하러 가자 : " + boardDTO.toString());

		// 첨부파일리스트를 boardDTO에 추가
		boardDTO.setFileList(uploadFileList);

		String returnPage = "redirect:/hboard/listAll";

		try {
			if (service.saveBoard(boardDTO)) {
				System.out.println("게시글+파일 저장 성공");

				rttr.addAttribute("status", "success");
			}

		} catch (Exception e) {
			e.printStackTrace();
			rttr.addAttribute("status", "fail");
		}

		// 이전 글의 파일들 저장시 사용된 리스트를 지워주는 작업이 필요하다.
		uploadFileList.clear();

		return returnPage; // 게시글 전체 목록 페이지로 돌아감.
	}

	@RequestMapping(value = "/upfiles", method = RequestMethod.POST)
	public ResponseEntity<MyResponseWithoutData> saveBoardfile(@RequestParam("file") MultipartFile file,
			HttpServletRequest request) {
		System.out.println("파일 전송 요청됨");
		System.out.println("파일의 오리지널 이름 : " + file.getOriginalFilename());
		System.out.println("파일의 사이즈 : " + file.getSize());
		System.out.println("파일의 contentType : " + file.getContentType());

		ResponseEntity<MyResponseWithoutData> result = null;

		try {
			BoardUpFilesVODTO fileInfo = fileSave(file, request);
			System.out.println(fileInfo.toString());

			uploadFileList.add(fileInfo);

			String tmp = null;
			if (fileInfo.getThumbFileName() != null) {
				// 이미지
				tmp = fileInfo.getThumbFileName();
			} else {
				// 노이미지
				tmp = fileInfo.getNewFileName();
			}

			MyResponseWithoutData mrw = MyResponseWithoutData.builder().code(200).msg("success").newFileName(tmp)
					.build();

			result = new ResponseEntity<MyResponseWithoutData>(mrw, HttpStatus.OK);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
		}

		return result;
	}

	private BoardUpFilesVODTO fileSave(MultipartFile file, HttpServletRequest request) throws IOException {
		// 파일의 기본 정보 저장
		String originalFileName = file.getOriginalFilename();
		long fileSize = file.getSize();
		String contentType = file.getContentType();
		byte[] upfile = file.getBytes(); // 파일의 실제 데이터를 저장

		String realPath = request.getSession().getServletContext().getRealPath("/resources/boardUpFiles");
		System.out.println("서버의 실제 물리적 경로 : " + realPath);

		// 실제 파일 저장
		BoardUpFilesVODTO result = fileProcess.saveFileToRealPath(upfile, realPath, contentType, originalFileName,
				fileSize);
		return result;
	}

	@RequestMapping(value = "/removefile", method = RequestMethod.POST)
	public ResponseEntity<MyResponseWithoutData> removeUpFile(@RequestParam("removeFileName") String removeFileName,
			HttpServletRequest request) {
		System.out.println("업로드된 파일을 삭제하자~ : " + removeFileName); // \2024\09\04\thumb_plane_1725429910541.jpg

		ResponseEntity<MyResponseWithoutData> result = null;

		String realPath = request.getSession().getServletContext().getRealPath("/resources/boardUpFiles");

		// 이미지라면
//			-> thumb_xxx.png 삭제 
//			-> originalxxx.png 삭제

		// 이미지가 아니라면
//			-> originalyy.ext 삭제
		int removeIndex = -1;
		boolean removeResult = false;

		if (removeFileName.contains("thumb_")) {
			for (int i = 0; i < uploadFileList.size(); i++) {
				if (uploadFileList.get(i).getThumbFileName().contains(removeFileName)) {
					System.out.println(i + "번째에서 해당 파일 찾았음 (thumb): " + uploadFileList.get(i).getThumbFileName());
					if (fileProcess.removeFile(realPath + removeFileName)
							&& fileProcess.removeFile(realPath + uploadFileList.get(i).getNewFileName())) {
						removeIndex = i;
						System.out.println(removeFileName + "파일 삭제 완료");
						System.out.println(uploadFileList.get(i).getNewFileName() + "파일 삭제 완료");
						removeResult = true;
						break;
					}
				}
			}
		} else {
			for (int i = 0; i < uploadFileList.size(); i++) {
				if (uploadFileList.get(i).getNewFileName().contains(removeFileName)) {
					System.out.println(i + "번째에서 해당 파일 찾았음 : " + uploadFileList.get(i).getNewFileName());
					if (fileProcess.removeFile(realPath + uploadFileList.get(i).getNewFileName())) {
						removeIndex = i;
						System.out.println("noimage - " + removeFileName + "파일 삭제 완료");
						removeResult = true;
						break;
					}
				}
			}
		}

		if (removeResult) {
			uploadFileList.remove(removeIndex); // 리스트에서 삭제
			System.out.println("============================================");
			System.out.println("현재 파일리스트에 있는 파일들");
			for (BoardUpFilesVODTO f : uploadFileList) {
				System.out.println(f.toString());
			}
			System.out.println("============================================");
			result = new ResponseEntity<MyResponseWithoutData>(new MyResponseWithoutData(200, "", "success"),
					HttpStatus.OK);
		} else {
			result = new ResponseEntity<MyResponseWithoutData>(new MyResponseWithoutData(200, "", "fail"),
					HttpStatus.CONFLICT);
		}

		return result;

	}

	// 취소 처리
//	@GetMapping("/cancelBoard")
	@RequestMapping(value = "/cancelBoard", method = RequestMethod.GET)
	public ResponseEntity<String> cancelBoard(HttpServletRequest request) {
		System.out.println("유저가 업로드한 모든 파일을 삭제하자");
		String realPath = request.getSession().getServletContext().getRealPath("/resources/boardUpFiles");

		allUploadFilesDelete(realPath, uploadFileList);

		return new ResponseEntity<String>("success", HttpStatus.OK);

	}

	private void allUploadFilesDelete(String realPath, List<BoardUpFilesVODTO> fileList) {
//		System.out.println(realPath);

		for (int i = 0; i < fileList.size(); i++) {
			fileProcess.removeFile(realPath + fileList.get(i).getNewFileName()); // realPath + \2024\09\05\natural.jpg
//			System.out.println(i + "번째 : " + fileList.get(i).getNewFileName()); 

			System.out.println(fileList.get(i).toString());
			// 이미지 파일이면 썸네일 파일 또한 삭제 해야 함
			if (fileList.get(i).getThumbFileName() != null) {
				fileProcess.removeFile(realPath + fileList.get(i).getThumbFileName());
			}
		}

	}

	// -------------- 게시글 상세페이지 -----------------------
	@GetMapping("/viewBoard1") // hboard/viewBoard?boardNo=10
	public void viewBoard1(@RequestParam("boardNo") int boardNo, Model model) {

//		logger.info(boardNo + "번 글을 조회하자~");

		// viewBoard.jsp 에 상세글 + 업로드 파일 정보 출력
		HashMap<String, Object> boardMap = null;
		try {
			boardMap = service.viewBoardByBoardNo(boardNo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		model.addAttribute("board", boardMap.get("board"));
		model.addAttribute("fileList", boardMap.get("fileList"));

	}

	// resultMap 테스트
	@GetMapping("/viewBoard2")
	public void viewBoard2(@RequestParam("boardNo") int boardNo, Model model) {

		HBoardDTO dto = null;

		try {
			dto = service.testResultMap(boardNo);

		} catch (Exception e) {
			e.printStackTrace();
		}
		model.addAttribute("board", dto);
	}

	// 아래의 viewBoard()는 /viewBoard(게시글상세보기), /modifyBoard(게시글 수정하기 위해 게시글을 불러오기위함)일
	// 때
	// 2번 호출된다.
	@GetMapping(value = { "/viewBoard", "/modifyBoard" })
	public String viewBoard(@RequestParam(value = "boardNo", defaultValue = "-1") int boardNo, Model model,
			HttpServletRequest request) {
//		logger.info(boardNo + "번 글을 조회하자~");
		List<BoardDetailInfo> boardDetailInfo = null;
		String ipAddr = GetClientIPAddr.getClientIp(request);
		String returnViewPage = "";

		logger.info(ipAddr + "가 " + boardNo + "번 글을 조회한다!");
		System.out.println("uri: " + request.getRequestURI());

		if (boardNo == -1) {
			return "redirect:/hboard/listAll";
		} else {
			try {
				if (request.getRequestURI().equals("/hboard/viewBoard")) {
					System.out.println("게시글 상세보기 호출");
					boardDetailInfo = service.read(boardNo, ipAddr);
					returnViewPage = "/hboard/viewBoard";
					System.out.println("size : " + boardDetailInfo.size());

				} else if (request.getRequestURI().equals("/hboard/modifyBoard")) {
					System.out.println("게시글 수정하기 호출");
					returnViewPage = "/hboard/modifyBoard";
					boardDetailInfo = service.read(boardNo);

					for (BoardDetailInfo b : boardDetailInfo) {
						this.modifyFileList = b.getFileList();
					}

					outputModifyFileList();
				}

			} catch (Exception e) {
				e.printStackTrace();
				returnViewPage = "redirect:/hboard/listAll?status=fail";
			}

			model.addAttribute("boardDetailInfo", boardDetailInfo);
		}
		return returnViewPage;

	}

	// 답글 처리
	@RequestMapping("/showReplyForm")
	public String showReplyForm() {
//		System.out.println("showReplyForm GET요청");
		return "/hboard/replyForm";
	}

	@RequestMapping(value = "/saveReply", method = RequestMethod.POST)
	public String saveReplyBoard(HBoardReplyDTO replyBoard, RedirectAttributes rttr) {
		System.out.println(replyBoard + "답글 저장하자~");

		String returnPage = "redirect:/hboard/listAll";

		try {
			if (service.saveReply(replyBoard)) {
				rttr.addAttribute("status", "success");
			}
		} catch (Exception e) {
			e.printStackTrace();
			rttr.addAttribute("status", "fail");
		}

		return returnPage;
	}

	// 게시글 삭제
	@RequestMapping("/removeBoard")
	public String removeBoard(@RequestParam("boardNo") int boardNo, HttpServletRequest request,
			RedirectAttributes rttr) {
		System.out.println(boardNo + " 번 글을 삭제하자!");

		String realPath = request.getSession().getServletContext().getRealPath("/resources/boardUpFiles");
		System.out.println("realPath : " + realPath);
		try {
			List<BoardUpFilesVODTO> fileList = service.removeBoard(boardNo);
			// 첨부파일이 있다면 ... 파일정보를 가져와 하드디스크에서 삭제
			if (fileList.size() > 0) {
				allUploadFilesDelete(realPath, fileList);
			}

			rttr.addAttribute("status", "removesuccess");

//	         for (BoardUpFilesVODTO f : fileList) {
//	            System.out.println(f.toString());
//	         }
		} catch (Exception e) {
			rttr.addAttribute("status", "removefail");
		}

		return "redirect:/hboard/listAll";

	}

	// 게시글 수정 처리
	@RequestMapping(value = "modifyRemoveFileCheck", method = RequestMethod.POST)
	public ResponseEntity<MyResponseWithoutData> modifyRemoveFileCheck(@RequestParam("removeFileNo") int removeFilePK) {
		System.out.println(removeFilePK + "번 파일 삭제 요청됨");

		// 아직 최송 수정이 될지 안될지 모르는 상태 : 하드에서 삭제할 수 없다.
		// 삭제될 파일을 체크만 해두고 최종요청이 들어오면 그때 실제 삭제 처리를 해야 한다.
		for (BoardUpFilesVODTO file : this.modifyFileList) {
			if (removeFilePK == file.getBoardUpFileNo()) {
				file.setFileStatus(BoardUpFileStatus.DELETE);
			}
		}

		outputModifyFileList();

		return new ResponseEntity<MyResponseWithoutData>(new MyResponseWithoutData(200, null, "success"),
				HttpStatus.OK);
	}

	private void outputModifyFileList() {
		System.out.println("============================================");
		System.out.println("현재 파일리스트에 있는 파일들 (수정시)");
		for (BoardUpFilesVODTO f : this.modifyFileList) {
			System.out.println(f.toString());
		}
		System.out.println("============================================");
	}

	@RequestMapping(value="/cancelRemFiles", method = RequestMethod.POST)
	public ResponseEntity<MyResponseWithoutData> cancelRemFiles(){
		System.out.println("파일리스트의 모든 파일삭제를 취소");
		for(BoardUpFilesVODTO file : this.modifyFileList) {
			file.setFileStatus(null);
		}
		outputModifyFileList();
		return new ResponseEntity<MyResponseWithoutData>(new MyResponseWithoutData(200, null, "success"), HttpStatus.OK);
	}
	@RequestMapping(value="/modifyBoardSave", method = RequestMethod.POST)
	public void modifyBoardSave(HBoardDTO modifyBoard, @RequestParam("modifyNewFile") MultipartFile[] modifyNewFile) {
		System.out.println(modifyBoard.toString() +"를 수정 하자");
		
		for(int i=0;i<modifyNewFile.length;i++) {
			System.out.println("새로 업로드된 파일 : " + modifyNewFile[i].getOriginalFilename()); 
		}
	}
}

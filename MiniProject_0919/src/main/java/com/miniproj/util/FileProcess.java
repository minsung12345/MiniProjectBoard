package com.miniproj.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.springframework.stereotype.Component;

import com.miniproj.model.BoardUpFilesVODTO;

@Component  // 스프링 컨테이너에게 객체를 만들어 관리하도록 하는 어노테이션
public class FileProcess {

	public BoardUpFilesVODTO saveFileToRealPath(byte[] upfile, String realPath, String contentType, String originalFileName,
			long fileSize) throws IOException {
		
		BoardUpFilesVODTO result = null;
		
		// 파일이 실제 저장되는 경로 : realPath + "년/월/일" 
		String[] ymd = makeCalendarPath(realPath);
		makeDirectory(realPath, ymd); // 실제 directory 를 만드는 메서드
		
		String saveFilePath = realPath + ymd[ymd.length -1]; // 실제 파일의 저장 경로 
		System.out.println("saveFilePath : " + saveFilePath); // \resources\boardUpFiles\2024\09\04
		
		String newFileName = null;
		String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
		
		if (fileSize > 0) {
			if (checkFileExist(saveFilePath, originalFileName)) {// 파일이름 중복 -> 파일이름 변경
				newFileName =  renameFileName(originalFileName);
			} else {
				newFileName = originalFileName;
			}
			
			File fileToSave = new File(saveFilePath + File.separator + newFileName);
			FileUtils.writeByteArrayToFile(fileToSave, upfile); // 실제 파일 저장
			
			// 이미지 파일 -> 썸네일, base64
			if (ImageMimeType.isImage(ext)) {
//				System.out.println("이미지입니다...썸네일 만듭시다");
				 String thumbImgName = makeThumbNailImage(saveFilePath, newFileName);
				
				 // base64문자열 
				 // 이진수 데이터를 ASCII(아스키코드) 영역의 문자로만 이루어진 문자열로 바꾸는 인코딩 방식이다. 
				 // 용량이 작은 썸네일 이미지만 base64로 처리.
				 
				 String base64Str = makeBase64String(saveFilePath + File.separator + thumbImgName);
//				 System.out.println("Base64 : " + base64Str);
				 
				 result = BoardUpFilesVODTO.builder()
				 .ext(ext)
				 .newFileName(ymd[2] + File.separator + newFileName)
				 .originFileName(originalFileName)
				 .size(fileSize)
				 .base64Img(base64Str)
				 .thumbFileName(ymd[2] + File.separator + thumbImgName)
				 .build();
				
//				 System.out.println(result.toString());
			} else {
				// 이미지 파일이 아닌 경우
				result = BoardUpFilesVODTO.builder()
						.ext(ext)
						.newFileName(ymd[2] + File.separator + newFileName)
						.originFileName(originalFileName)
						.size(fileSize)
						.build();
			}
		}
		
		return result;
	}

	
	private String makeBase64String(String thumbNailFileName) throws IOException {
		// 썸네일 이미지 -> base64
		String result = null;
		
		// 썸네일 파일의 경로로 File객체 생성
		File thumb = new File(thumbNailFileName);
		
		// thumb File객체가 가리키는 파일을 읽어와서 byte[]로 반환
		byte[] upfile = FileUtils.readFileToByteArray(thumb);
		
		// 인코딩
		result = Base64.getEncoder().encodeToString(upfile);
		
		return result;
	}


	private String makeThumbNailImage(String saveFilePath, String newFileName) throws IOException {
		// 썸네일 만들기
//		ImageIO.read(saveFilePath + File.separator + newFileName);
		BufferedImage originalImage = ImageIO.read(new File(saveFilePath + File.separator + newFileName));
		BufferedImage thumbNailImage = Scalr.resize(originalImage, Mode.FIT_TO_HEIGHT, 50);
		
		String thumbImgName = "thumb_" + newFileName;
		
		File saveThumbImg = new File(saveFilePath + File.separator + thumbImgName);
		String ext = thumbImgName.substring(thumbImgName.lastIndexOf(".") + 1);
		
		if (ImageIO.write(thumbNailImage, ext, saveThumbImg)) {
			return thumbImgName;
		} else {
			return null;
		}
	}


	private String renameFileName(String originalFileName) {
		String timestamp = System.currentTimeMillis() + "";
		String uuid = UUID.randomUUID().toString();
		// new.png -> new_timestampe.png
		String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1); // png
		String fileNameWithoutExt = originalFileName.substring(0, originalFileName.lastIndexOf(".")); // new
		
		String newFileName = fileNameWithoutExt + "_" + timestamp + "." + ext; 
		System.out.println("originalFileName :  " + originalFileName);
		System.out.println("newFileName :  " + newFileName);
				
		return newFileName;
	}


	private boolean checkFileExist(String saveFilePath, String originalFileName) {
		// 파일이름 중복 검사 : 중복된 파일이 있다면 true, 없다면 false
		boolean isFind = false;
//		System.out.println("originalFileName : " + originalFileName + ", " + originalFileName.length() );
		
		File tmp = new File(saveFilePath);
		String[] dirs = tmp.list();
		if (dirs != null) {
			for (String name : dirs) {
				if (name.equals(originalFileName)) {
//					System.out.println("이름이 같은 것이 있다!" + ", " + name.length());
					isFind = true;
					break;
				}
			}
		} 
		
		if (!isFind) {
//			System.out.println("이름이 같은 것이 없다!");
		}
		
		return isFind;
	}


	private void makeDirectory(String realPath, String[] ymd) {
//	private void makeDirectory(String realPath, String...ymd) { // 가변인자
	
		// 실제 directory 를 만드는 메서드
//		System.out.println(new File(realPath + ymd[ymd.length -1]).exists());
		if (!new File(realPath + ymd[ymd.length -1]).exists()) {
			// realPath + \2024\09\03 디렉토리가 모두 존재하지 않는다면...
			// 디렉토리를 생성해야 한다.
			for (String path : ymd) {
				File tmp = new File(realPath + path); 
				if (!tmp.exists()) {
					tmp.mkdir();
				}
			}
		}
		
		
	}


	private String[] makeCalendarPath(String realPath) {
		// 파일이 실제 저장되는 경로 : realPath + "/년/월/일" 
		
	 	Calendar now = Calendar.getInstance();// 현재 날짜 시간 객체
	 	String year = File.separator + now.get(Calendar.YEAR) + ""; // \2024 
	 	String month = year + File.separator + new DecimalFormat("00").format(now.get(Calendar.MONTH) + 1);   // \2024\09
	 	String date = month + File.separator + new DecimalFormat("00").format(now.get(Calendar.DATE));  // \2024\09\03
	 	
	 	System.out.println("year/month/date : " + year + ", " + month + ", " + date);
	 	
	 	String[] ymd = {year, month, date};
//	 	makeDirectory(realPath, year, month, date);
	 	return ymd;
	}


	public boolean removeFile(String removeFileName) { // realPath + 년월일경로 + 파일이름.확장자
		// 업로드된 파일을 하드디스크에서 삭제
		boolean result = false;
		
		File tmpFile = new File(removeFileName);
		
		if (tmpFile.exists()) {
			result = tmpFile.delete();
		}
		return result;
	}

	
	
}

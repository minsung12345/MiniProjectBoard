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

@Component // 스프링 컨테이너에게 객체를 만들어 관리하도록
public class FileProcess {

   public BoardUpFilesVODTO saveFileToRealPath(byte[] upfile, String realPath, String contentType,
         String originalFileName, long fileSize) throws IOException {

      // 파일이 실제 저장되는 경로: realPath + "년/월/일"

      String[] ymd = makeCalentdarPath(realPath);
      String newFileName = "";
      BoardUpFilesVODTO result = null;

      makeDirectory(realPath, ymd);

      String saveFilePath = realPath + ymd[ymd.length - 1];

      if (fileSize > 0) {
         if (checkFileExist(saveFilePath, originalFileName)) {
            // true: 파일이름 중복, 변경 필요
            newFileName = renameFileName(originalFileName);
         } else {
            // false: 업로드 가능
            newFileName = originalFileName;
         }

         File fileToSave = new File(saveFilePath + File.separator + newFileName);
         // 실제 저장
         FileUtils.writeByteArrayToFile(fileToSave, upfile);

         // 이미지 파일 -> 썸네일
         String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
         if (ImageMimeType.isImage(ext)) {
            // System.out.println("이미지입니다... 썸네일을 만들겠습니다.");
            String thumbImgName = makeThumbNailImage(saveFilePath, newFileName);

            System.out.println("thumbImgName: " + thumbImgName);

            String base64Str = makeBase64String(saveFilePath + File.separator + thumbImgName);

            System.out.println("Base64: " + base64Str);

            result = BoardUpFilesVODTO.builder()
                  .ext(ext).newFileName(ymd[2] + File.separator + newFileName)
                  .originFileName(originalFileName)
                  .size(fileSize)
                  .base64Img(base64Str)
                  .thumbFileName(ymd[2] + File.separator + thumbImgName)
                  .build();

            System.out.println("result: " + result);

         } else {
            // 이미지가 아닌 경우
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

   // 썸네일 이미지 -> base64
   private String makeBase64String(String thumbNailFileName) throws IOException {
      String result = null;

      File thumb = new File(thumbNailFileName);

      // thumb File객체가 가리키는 ㅏㅍ일을 열어와서 byte[]로 반환
      byte[] upfile = FileUtils.readFileToByteArray(thumb);

      // 인코딩
      result = Base64.getEncoder().encodeToString(upfile);

      return result;
   }

   // 썸네일 이미지 만들기
   private String makeThumbNailImage(String saveFilePath, String newFileName) throws IOException {

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

      System.out.println("originalFileName: " + originalFileName);

      String[] temp = originalFileName.split("\\.");

      String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
      String name = originalFileName.substring(0, originalFileName.lastIndexOf("."));

      // System.out.println("ext:" + ext);
      //      System.out.println("name:" + name);

      // 새 파일이름 만들기
      // 이름, 확장자 필요
      String newFileName = temp[0] + "_" + timestamp + "." + ext;
      System.out.println(newFileName);

      return newFileName;
   }

   private boolean checkFileExist(String saveFilePath, String originalFileName) {
      // 파일이름 중복검사
      // 중복된 파일이 있다면 true, 없다면 false 반환

      boolean isFind = false;
      File tmp = new File(saveFilePath);

      // 모든 dir, file 불러오기
      String[] dirs = tmp.list();

      if (dirs != null) {
         for (String dir : dirs) {
            if (dir.equals(originalFileName)) {
               System.out.println("같은 이름의 파일 존재");
               isFind = true;
               break;
            }
         }
      }

      if (!isFind) {
         System.out.println("같은 이름의 파일이 없음");
      }

      return isFind;
   }

   private void makeDirectory(String realPath, String[] ymd) {
      // 실제 directory 생성
      System.out.println(new File(realPath + ymd[ymd.length - 1]).exists());

      if (!new File(realPath + ymd[ymd.length - 1]).exists()) {
         for (String path : ymd) {
            File tmp = new File(realPath + path);
            if (!tmp.exists()) {
               tmp.mkdir();
            }
         }
      }
   }

   private String[] makeCalentdarPath(String realPath) {
      // TODO Auto-generated method stub

      Calendar now = Calendar.getInstance();

      String year = File.separator + now.get(Calendar.YEAR) + "";
      String month = year + File.separator + new DecimalFormat("00").format(now.get(Calendar.MONTH) + 1);
      String date = month + File.separator + new DecimalFormat("00").format(now.get(Calendar.DATE));

      System.out.println("year: " + year);
      System.out.println("month: " + month);
      System.out.println("date: " + date);

      String[] ymd = { year, month, date };

      return ymd;
   }

   public boolean removeFile(String removeFileName) {
      // realPath + 년월일경로 + 파일이름.확장자
      // 업로드된 파일을 하드디스크에서 삭제

      boolean result = false;

      File tmpFile = new File(removeFileName);

      if (tmpFile.exists()) {
         result = tmpFile.delete();
      }

      return result;
   }

   public void saveUserProfile(byte[] upfile, String realPath, String fileName) throws Exception {
      // 회원가입시 업로드된 유저의 프로필이미지를 저장하는 메서드
      File saveFile = new File(realPath + File.separator + fileName);
      FileUtils.writeByteArrayToFile(saveFile, upfile);
      
   }
}


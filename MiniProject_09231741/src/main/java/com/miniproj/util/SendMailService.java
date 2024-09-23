package com.miniproj.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

public class SendMailService {
	private String username;
	private String password;

	public void sendMail(String emailAddr, String activationCode)
			throws IOException, AddressException, MessagingException {
		String subject = "miniproj.com에서 보내는 회원가입 이메일 인증번호입니다.";
		String message = "회원가입을 환영합니다. 인증번호 : " + activationCode + "을 입력하시고 회원가입을 완료하세요.";

		Properties props = new Properties();
//		포트번호 587
		props.put("mail.smtp.host", "smtp.naver.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.starttls.required", "true");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		props.put("mail.smtp.auth", "true");
		// 포트번호 465
//		props.put("mail.smtp.host", "smtp.naver.com");
//		props.put("mail.smtp.port", "465");
//		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
//		props.put("mail.smtp.auth", "true");
//	props.put("mail, smpt.ssl.enable","true");
		getAccount();

		// 세션 생성
		Session mailSession = Session.getInstance(props, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		System.out.println(mailSession.toString());
		if (mailSession != null) {
			MimeMessage mime = new MimeMessage(mailSession);

			mime.setFrom(new InternetAddress("alstjd5699@naver.com")); // 보내는 사람의 메일 주소
			mime.addRecipient(RecipientType.TO, new InternetAddress(emailAddr)); // 받는 사람의 메일 주소

			
			mime.setSubject(subject); // 메일 제목
			mime.setText(message); // 메일 본문

			Transport.send(mime);
		}
	}

	private void getAccount() throws IOException {

		Properties account = new Properties();
		account.load(new FileReader(
				"C:\\Users\\goott2\\Desktop\\MiniProject_09132024\\src\\main\\webapp\\WEB-INF\\config.properties"));

		this.username = (String) account.get("username");
		this.password = (String) account.get("password");
	}
}

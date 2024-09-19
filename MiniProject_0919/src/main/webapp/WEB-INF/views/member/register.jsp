<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
 <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
<title>회원가입</title>
<script type="text/javascript">
   $(function(){
      
      // 아이디 이벤트 
      $("#userId").keyup(function(){
         let tmpUserId = $("#userId").val();
         console.log(tmpUserId);
//           1) 아이디 : 필수, 4-8자, 아이디는 중복 불가
         if (tmpUserId.length < 4 || tmpUserId.length > 8) {
            outputError("아이디는 4-8자로 입력하세요", $("#userId"), "red");
            $("#idValid").val("");
         } else {
            // 중복체크
             $.ajax({
                 url : '/member/isDuplicate',             
                 type : 'POST',                                     
                 dataType : 'json', // 수신받을 데이터타입 (text, xml, json)
                 data : {
                    tmpUserId : tmpUserId
                 }, // 보내는 데이터
                 success : function(data){
                    console.log(data);
                    if (data.msg == "duplicate"){
                       outputError("중복된 아이디입니다", $("#userId"), "red");
                       $("#userId").focus();
                       $("#idValid").val("");
                    } else if (data.msg == "not duplicate"){
                       outputError("완료", $("#userId"), "green");
                       $("#idValid").val("checked");
                    }
                 },
                error: function () {},
                complete: function () {},
                 });
         }
      });
      
      // 비밀번호 체크 이벤트
      $("#userPwd1").blur(function(){
         let tmpPwd = $("#userPwd1").val();
         
         if (tmpPwd.length < 4 || tmpPwd.length > 8) {
            outputError("비밀번호는 4-8자로 입력하세요", $("#userPwd1"), "red");
            $("#pwdValid").val("");
         } else {
            outputError("완료", $("#userPwd1"), "green");
         }
      });
      
      $("#userPwd2").blur(function(){
         let tmpPwd2 = $("#userPwd2").val();
         let tmpPwd1 = $("#userPwd1").val();
         
         if (tmpPwd1 != tmpPwd2) {
            outputError("패스워드가 다릅니다", $("#userPwd1"), "red");
            $("#userPwd1").val("");
            $("#userPwd2").val("");
            $("#pwdValid").val("");
         } else if (tmpPwd1 == tmpPwd2) {
            outputError("일치",  $("#userPwd1"), "green");
            $("#pwdValid").val("checked");
         }
      });
      
      $("#email").blur(function(){
         emailValid();
      });
      
      
   }); // end of $(function)
   
   function outputError(errorMsg, tagObj, color) {
      let errTag = $(tagObj).prev();
//       console.log(errTag);
      $(errTag).html(errorMsg);
      $(errTag).css("color", color);
      $(tagObj).css("border-color", color);
   }
   
   function isValid() {
      let result = false;
      // 유효성 검사 조건
      // 1) 아이디 : 필수, 4-8자, 아이디는 중복 불가
      // 2) 비밀번호 : 필수, 4-8자, 비밀확인과 동일해야 한다. 
      
      
      let idCheck = idValid();
      let pwdCheck = pwdValid();
      let genderCheck = genderValid();
      let mobileCheck = mobileValid();
      let emailCheck = emailValid();
      
      // 동의 항목 체크했다면
      
      if (idCheck && pwdCheck && genderCheck && mobileCheck) {
         result = true;
         console.log(result)
      } else {
         result = false;
         console.log(result)
      }
      return result;
      
   }
   
   function idValid() {
      let result = false;
      
      if ($("#idValid").val() == "checked"){
         result = true;
      } else {
         outputError("아이디는 필수 항목입니다", $("#userId"), "red");
      }
         
      return result;
   }
   
   function pwdValid() {
      let result = false;
      
      if ($("#pwdValid").val() == 'checked'){
         result = true;
      } 
      
      return result;
   }
   
   function genderValid() {
      // 성별은 남성, 여성 중 하나를 반드시 선택해야 한다.
      let genders = document.getElementsByName("gender");
      let result = false;
      
      for (let g of genders) {
         if (g.checked) {
            console.log("체크되어있음");
            result = true;
         }
      }
      
      if (!result){
         outputError("성별은 필수입니다!", $(".genderSpan").next().next(), "red");
      } else {
         outputError("완료", $(".genderSpan").next().next(), "green");
      }
      
      return result;
   }
   
   function mobileValid() {
      let result = false;
      let tmpUserMobile = $("#mobile").val();
      
      let mobileRegExp = /^(01[016789]{1})-?[0-9]{3,4}-?[0-9]{4}$/;
      if (!mobileRegExp.test(tmpUserMobile)){
         outputError("휴대폰번호 형식이 아닙니다", $("#mobile"), "red");
      } else {
         outputError("완료", $("#mobile"), "green");
         result = true;
      }
      return result;
   }
   
   function emailValid() {
      // 1) 정규표현식을 이용하여 이메일 주소 형식인지 아닌지 판단
      // 2) 이메일 주소 형식이면.. 인증번호를 이메일로 보내고, 
      //    인증번호를 다시 입력받아서 검증
      let result = false;
      
      let tmpUserEmail = $("#email").val();
      let emailRegExp = /^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*\.[a-zA-Z]{2,3}$/i;
      
      if (!emailRegExp.test(tmpUserEmail)){
         outputError("이메일 주소 형식이 아닙니다", $("#email"), "red");
      } else {
         if ($("#emailValid").val() == 'checked' ){
            result = true;
         } else {
            showAuthenticateDiv(); // 인증번호를 입력하는 div를 보여준다.
            callSendMail(); // 이메일 발송
         }
      }
         
      
      
      
   }
   
   function showAuthenticateDiv() {
      alert("이메일로 인증번호를 발송했습니다 \n 인증코드를 입력해 주세요");
      let authDiv = "<div class='authenticateDiv'>";
      authDiv += `<input type='text' class="form-control" id="userAuthCode" placeholder="인증번호를 입력하세요...." />`;
      authDiv += `<button type="button" id="authBtn" class="btn btn-info">인증하기</button>`;
      authDiv += `</div>`;
      $(authDiv).insertAfter("#email");
   }
   
   function callSendMail() {
      $.ajax({
           url : '/member/callSendMail',             
           type : 'POST',                                     
           dataType : 'text', // 수신받을 데이터타입 (text, xml, json)
           data : {
              "tmpUserEmail" : $("#email").val()
           }, // 보내는 데이터
           success : function(data){
              console.log(data);
           },
          error: function () {},
          complete: function () {},
           });
      
   }

</script>
</head>
<body>
<jsp:include page="../header.jsp"></jsp:include>
   
   <div class="container">
      <h1>회원가입 페이지</h1>
      
      <form action="/member/register" method="post">
         
          <div class="mb-3 mt-3">
             <label for="userId" class="form-label">아이디:</label><span></span>
             <input type="text" class="form-control" id="userId" placeholder="아이디를 입력하세요...." name="userId">
             <input type="hidden" id="idValid"/>
           </div>
           
            <div class="mb-3 mt-3">
             <label for="userPwd1" class="form-label">비밀번호:</label><span></span>
             <input type="password" class="form-control" id="userPwd1" placeholder="비밀번호를 입력하세요...." name="userPwd">
           </div>
           
           
           <div class="mb-3 mt-3">
             <label for="userPwd2" class="form-label">비밀번호 확인:</label>
             <input type="password" class="form-control" id="userPwd2" placeholder="비밀번호를 다시한번 입력하세요...." >
             <input type="hidden" id="pwdValid"/>
           </div>
         
         <div class="mb-3 mt-3">
             <label for="userName" class="form-label">이름:</label>
             <input type="text" class="form-control" id="userName" placeholder="이름을 입력하세요...." >
           </div>
           
           <span class="genderSpan">성별: </span><span></span>
           
           <div class="form-check">
              <label class="form-check-label" for="female">
                 <input type="radio" class="form-check-input" id="female" name="gender" value="F">여성
              </label>
         </div>
         <div class="form-check">
              <label class="form-check-label" for="male">
                 <input type="radio" class="form-check-input" id="male" name="gender" value="M">남성
              </label>
         </div>
           
           <div class="mb-3 mt-3">
             <label for="mobile" class="form-label">휴대전화번호:</label><span></span>
             <input type="text" class="form-control" id="mobile" placeholder="전화번호를 입력하세요...." name="mobile">
           </div>
           <div class="mb-3 mt-3">
             <label for="email" class="form-label">이메일:</label><span></span>
             <input type="text" class="form-control" id="email" placeholder="이메일을 입력하세요...." name="email">
           </div>
           <input type="hidden" id="emailValid"/>
           
           <div class="mb-3 mt-3">
<!--            취미 -->
           </div>
           
           <div class="mb-3 mt-3">
<!--            유저 프로필 사진 -->
           </div>
           
           <div class="form-check">
           <input class="form-check-input" type="checkbox" id="agree" name="agree" value="Y">
           <label class="form-check-label">회원가입 조항에 동의합니다.</label>
         </div>
           
           <input type="submit" class="btn btn-success" value="회원가입" onclick="return isValid();"/>
           <input type="reset" class="btn btn-danger" value="취소" />
      </form>
   
   </div>
   
   
<jsp:include page="../footer.jsp"></jsp:include>
</body>
</html>
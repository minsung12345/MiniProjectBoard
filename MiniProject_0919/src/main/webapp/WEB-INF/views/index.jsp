<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
<title>홈</title>
<script type="text/javascript">
$(function(){
	
	checkCookie();
// 	$("#myNotice").show();
	
	// 인기글 top5얻어오기 (ajax)
	getBoardTop5();
	
    $('.modalCloseBtn').click(function(){
        // 유저가 체크박스에 체크를 했는지 검사
        if ($('#agree').is(':checked')) {
           // 쿠키를 저장
               $.ajax({
              url : '/saveCookie',             
              type : 'GET',                                     
              dataType : 'text',   
              success : function(data){
                 console.log(data);
                 
                 }
                 
              });
        } else {
           alert('쿠키를 저장하지 않습니다.')
        }
        
        $("#myNotice").hide(); 

     });
});

function getBoardTop5() {
	$.ajax({
	    url: "/getBoardTop5", // 데이터가 송수신될 서버의 주소
	    type: "GET", // 통신 방식 (GET, POST, PUT, DELETE)
	    dataType: "json", // 수신 받을 데이터 타입 (MIME TYPE)
	    async: false, // 동기통신방식
	    success: function (data) {  // 통신이 성공하면 수행할 함수
	      console.log(data);
	      outputTopBoard(data);
	    },
	    error: function () {},
	    complete: function () {},
	  });
}

function outputTopBoard(json) {
	output = `<table class="table table-hover">`;
	output += `<thead><tr><th>제목</th><th>작성자</th><th>작성일</th><th>조회수</th></tr></thead>`;
	output += `<tbody>`;
	$.each(json, function(i, item){
		output += `<tr><td>\${item.title}</td><td>\${item.writer}</td>`;
	    let postDate = new Date(item.postDate).toLocaleDateString();
	    output += `<td>\${postDate}</td><td>\${item.readCount}</td></tr>`;
	});
	output += `</tbody></table>`;
	
	$(".topBoard").html(output);
}

function checkCookie() {
    $.ajax({
       url : '/readCookie',             
       type : 'POST',                                     
       dataType : 'json',   
       success : function(data){
          console.log(data);
          if (data == 'fail') {
             $('#myNotice').show();
          }
       
          }                        
       });
 }
 
 
</script>
</head>
<body>
<jsp:include page="header.jsp"></jsp:include>
	<div class="container">
		<h1>index.jsp</h1>
		<h2>인기글 Top5</h2>
		<div class="topBoard"></div>
	</div>
<jsp:include page="footer.jsp"></jsp:include>

<!-- The Modal -->
<div class="modal" id="myNotice" style="display: none;">
  <div class="modal-dialog">
    <div class="modal-content">

      <!-- Modal Header -->
      <div class="modal-header">
        <h4 class="modal-title">Mini-Project</h4>
        <button type="button" class="btn-close modalCloseBtn" data-bs-dismiss="modal"></button>
      </div>

      <!-- Modal body -->
      <div class="modal-body">
         추석 잘 보내세요~
      </div>

      <!-- Modal footer -->
      <div class="modal-footer">
      	<input class="form-check-input" type="checkbox" id="agree" name="agree"  >하루동안 공지 열지 않기
        <button type="button" class="btn btn-danger modalCloseBtn" data-bs-dismiss="modal">닫기</button>
      </div>

    </div>
  </div>
</div>

	
</body>
</html>
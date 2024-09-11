<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
<title>게시판 목록</title>
<script>
	$(function(){
		timediffPostDate(); 
		
		showModal();
		
		$(".modalCloseBtn").click(function(){
			$("#myModal").hide(); // 모달창 닫기
		});
	});
	
	
	function showModal(){
		let status = '${param.status}'; // url주소창에서 status쿼리스트링의 값을 가져와 변수 저장
		console.log(status);
		
		if (status == 'success') {
			// 글저장성공 모달창
			$(".modal-body").html('<h5>글 저장에 성공하였습니다.</h5>');
			$("#myModal").show();
		} else if (status == 'fail') {
			$(".modal-body").html('<h5>글 저장에 실패하였습니다.</h5>');
			$("#myModal").show();
		}
		
		// 게시글을 불러올 때 예외가 발생한 경우
		let except = '${exception}';
		console.log(except);
		if (except == 'error') {
			$(".modal-body").html('<h2>문제가 발생해 데이터를 불러오지 못했습니다.</h2>');
			$("#myModal").show();
		}
	}
	
	// 게시글의 글작성일을 얻어와서 2시간 이내에 작성한 글이면 new.png 이미지를 제목 옆에 출력한다. 
	function timediffPostDate(){
// 		console.log($(".postDate"));
	$(".postDate").each(function(i, e){
		console.log(i + "번째 : " + $(e).html());
		
		let postDate = new Date($(e).html()); // 글 작성일 저장 (Date객체로 변환)
		let curDate = new Date(); // 현재 날짜 시간 객체 생성
		console.log(curDate - postDate); // 밀리초 단위
		
		let diff = (curDate - postDate) / 1000 / 60 / 60; // 시간단위
		console.log(diff);
		
		let title = $(e).prev().prev().html();
		console.log(title);
		
		if (diff < 2) { // 2시간 이내에 작성한 글이라면
			let output = "<span><img src='/resources/images/new.png' width='26px;' /></span>";
			$(e).prev().prev().html(title + output);
		}
	});
	
	}
	
</script>
</head>
<body>
<jsp:include page="./../header.jsp"></jsp:include>
	<div class="container">
		<h1>계층형 게시판 전체 목록</h1>
<%-- 		<div>${boardList }</div> --%>
		<!--   테이블로 출력 -->
		<table class="table table-hover">
		    <thead>
		      <tr>
		        <th>#</th>
		        <th>글제목</th>
		        <th>작성자</th>
		        <th>작성일</th>
		        <th>조회수</th>

		      </tr>
		    </thead>
		    <tbody>
			    <c:forEach var="board" items="${boardList }">
				<c:choose>
					<c:when test="${board.isDelete == 'N' }">
						 <tr onclick="location.href='/hboard/viewBoard?boardNo=${board.boardNo}';">
					        <td>${board.boardNo }</td>
					        <td>
						        <c:if test="${board.step > 0 }">
						        	<img src="/resources/images/arrow.png" width="20px" style="margin-left: calc(20px * ${board.step - 1})"/>
						        </c:if>
					        ${board.title }</td>
					        
					        <td>${board.writer }</td>
					        <td class="postDate">${board.postDate }</td>
					        <td>${board.readCount }</td>

				      </tr>
					</c:when>
					<c:when test="${board.isDelete == 'Y' }">
						<tr>
							<td>${board.boardNo }</td>
							<td colspan="4" style="color : gray;"><i>삭제된 글입니다.</i></td>
						</tr>
					</c:when>
				</c:choose>
			     
			    </c:forEach>
		    </tbody>
  		</table>
  		
  		<div><button type="button" class="btn btn-success" onclick="location.href='/hboard/saveBoard';">글쓰기</button></div>
  		
  		<!-- The Modal -->
		<div class="modal" id="myModal" style="display : none;">
		  <div class="modal-dialog">
		    <div class="modal-content">
		
		      <!-- Modal Header -->
		      <div class="modal-header">
		        <h4 class="modal-title">MiniProject</h4>
		        <button type="button" class="btn-close modalCloseBtn" data-bs-dismiss="modal"></button>
		      </div>
		
		      <!-- Modal body -->
		      <div class="modal-body">
		       
		      </div>
		
		      <!-- Modal footer -->
		      <div class="modal-footer">
		        <button type="button" class="btn btn-danger modalCloseBtn" data-bs-dismiss="modal">Close</button>
		      </div>
		
		    </div>
		  </div>
		</div>
	</div>
	
	<div>
<!-- 	<img src="data:image/jpeg;base64, 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"/> -->
	</div>
<jsp:include page="./../footer.jsp"></jsp:include>	
</body>
</html>
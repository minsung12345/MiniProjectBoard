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
		
// 		testAjax(1, 20);
		
		// pagingSize
		let pagingSize = '${param.pagingSize}';
		if (pagingSize == ''){
			pagingSize = 10;
		} else {
			pagingSize = parseInt(pagingSize); 
		}
		
		$("#pagingSize").val(pagingSize);
		
		// 유저가 페이징사이즈를 선택하면
		$(".pagingSize").change(function(){
			console.log($(this).val());
			
			let pageNo = '${param.pageNo}';
			console.log("pageNo : " + pageNo);
			if (pageNo == ''){
				pageNo = 1;
			} else {
				pageNo = parseInt(pageNo);
			}
			
			location.href = '/hboard/listAll?pagingSize=' + $(this).val() + "&pageNo=" + pageNo;
		});
		
		showModal();
		
		$(".modalCloseBtn").click(function(){
			$("#myModal").hide(); // 모달창 닫기
		});
		
	});
	
	
	function showModal(){
		let status = '${param.status}'; // url주소창에서 status쿼리스트링의 값을 가져와 변수 저장
// 		console.log(status);
		
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
// 		console.log(i + "번째 : " + $(e).html());
		
		let postDate = new Date($(e).html()); // 글 작성일 저장 (Date객체로 변환)
		let curDate = new Date(); // 현재 날짜 시간 객체 생성
// 		console.log(curDate - postDate); // 밀리초 단위
		
		let diff = (curDate - postDate) / 1000 / 60 / 60; // 시간단위
// 		console.log(diff);
		
		let title = $(e).prev().prev().html();
// 		console.log(title);
		
		if (diff < 2) { // 2시간 이내에 작성한 글이라면
			let output = "<span><img src='/resources/images/new.png' width='26px;' /></span>";
			$(e).prev().prev().html(title + output);
		}
	});
	
	}
	
	function searchValid(){
	      let sw = $("#searchWord").val(); 
	       if (sw.length == 0){
	          alert("검색어를 입력하세요");
	          return false;
	       }
	      
	      let expText = /[%=><]/; // 데이터베이스에서 조건 연산자
	      if (expText.test(sw) == true) {
	         alert("특수문자를 입력할 수 없습니다.");
	         return false;   
	      }
	      
	      let sql = new Array("or", "select", "insert", "update", "delete", "create", "alter", "drop", "exec", "union", 
	            "fetch", "declare", "truncate" );
	      
	      let regEx = "";
	      
	      for (let i = 0; i < sql.length; i++){
	         regEx = new RegExp(sql[i], "gi");
	         
	         if (regEx.test(sw) == true){
	            alert("특정 문자로 검색할 수 없습니다.");
	            return false;
	         }
	      }
	      
	      return true;
	   }
	
</script>
<style>
	.boardControl {
		width : 200px;
		}
</style>
</head>
<body>
<jsp:include page="./../header.jsp"></jsp:include>
	<div class="container">
		<h1>계층형 게시판 전체 목록</h1>
		
		<div class="boardControl">
			<select class="form-select pagingSize" id="pagingSize">
			  <option value="10">10개씩 보기</option>
			  <option value="20">20개씩 보기</option>
			  <option value="40">40개씩 보기</option>
			  <option value="80">80개씩 보기</option>
			</select>
		</div>
		
<%-- 		<div>${boardList }</div> --%>
	<c:choose>
		<c:when test="${boardList.size() > 0 }">
		
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
  		
		</c:when>
	<c:otherwise>
			<div style="font-size: 150px; font-weight: bold; text-align: center;"> 텅~!</div>
		</c:otherwise>
		</c:choose>
	
  		<div><button type="button" class="btn btn-success" onclick="location.href='/hboard/saveBoard';">글쓰기</button></div>
  		
<!--   		검색어 -->
  		<form action="/hboard/listAll" method="post">
	  		<div class="input-group mb-3 mt-3">
				<select class="form-select" id="searchType" name="searchType">
				  <option value="">--검색타입--</option>
				  <option value="title">제목</option>
				  <option value="writer">작성자</option>
				  <option value="content">내용</option>
				</select>
				
		        <input type="text" class="form-control" placeholder="검색어를 입력하세요..." id="searchWord" name="searchWord">
	  
	  			<button type="submit" class="btn btn-success" onclick="return searchValid();" >검색</button>
			</div>
  		</form>
  		
<%--   		<div>${boardList }</div> --%>
  		
<!--   		페이지네이션 -->
  		<div>${pagingInfo }</div>
  		<div>${search }</div>
  		<div class="paging">
  			<ul class="pagination justify-content-center" style="margin:20px 0">
<!--   PREVIOUS 버튼 -->
			    <c:if test="${pagingInfo.pageNo == 1 and boardList.size() > 0 }">
			    	<li class="page-item disabled"><a class="page-link" href="#">Previous</a></li>
			    </c:if>
			    <c:if test="${pagingInfo.pageNo > 1 }">
			    	<li class="page-item"><a class="page-link" href="/hboard/listAll?pageNo=${pagingInfo.pageNo - 1 }&pagingSize=${param.pagingSize}&searchType=${search.searchType}&searchWord=${search.searchWord}">Previous</a></li>
			    </c:if>
			    
<!-- 	페이지 번호	     -->
			    <c:forEach var="i" begin="${pagingInfo.startPageNoCurBlock }" end="${pagingInfo.endPageNoCurBlock }">
			    	<c:choose>
			    		<c:when test="${pagingInfo.pageNo == i }">
			    			<li class="page-item active"><a class="page-link" href="/hboard/listAll?pageNo=${i }&pagingSize=${param.pagingSize}&searchType=${search.searchType}&searchWord=${search.searchWord}">${i }</a></li>		
			    		</c:when>
			    		<c:otherwise>
					    	<li class="page-item"><a class="page-link" href="/hboard/listAll?pageNo=${i }&pagingSize=${param.pagingSize}&searchType=${search.searchType}&searchWord=${search.searchWord}">${i }</a></li>
			    		</c:otherwise>
			    	</c:choose>
			    </c:forEach>
			    
<!--   NEXT 버튼 -->
			    <c:if test="${pagingInfo.pageNo < pagingInfo.totalPageCnt }">
			    	<li class="page-item"><a class="page-link" href="/hboard/listAll?pageNo=${pagingInfo.pageNo + 1 }&pagingSize=${param.pagingSize}&searchType=${search.searchType}&searchWord=${search.searchWord}">Next</a></li>
			    </c:if>
			    <c:if test="${pagingInfo.pageNo == pagingInfo.totalPageCnt }">
			    	<li class="page-item disabled"><a class="page-link" href="#">Next</a></li>
			    </c:if>
			    
			  </ul>
  		</div>
  		
  		
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

<jsp:include page="./../footer.jsp"></jsp:include>	

<script type="text/javascript">
function testAjax(pageNo, pagingSize){
	$.ajax({
	    url: "/hboard/listAllAjax", // 데이터가 송수신될 서버의 주소
	    type: "GET", // 통신 방식 (GET, POST, PUT, DELETE)
	    dataType: "json", // 수신 받을 데이터 타입 (MIME TYPE)
	    data: { // 보내는 데이터
	  	 	pageNo : pageNo,
	  	 	pagingSize : pagingSize
	    },
	    async: false, // 동기통신방식
	    success: function (data) {  // 통신이 성공하면 수행할 함수
	      console.log(data);
	    },
	    error: function () {},
	    complete: function () {},
	  });
}

</script>




</body>
</html>
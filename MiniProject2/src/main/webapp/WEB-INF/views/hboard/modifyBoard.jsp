<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
<title>게시글 수정</title>
<script type="text/javascript">
	$(function(){
		
	});
	
	function removeFileCheckBx(fileId) {
		// 체크박스를 클릭할 때 호출되는 함수
// 		console.log(fileId);
		let chkCount = countCheckBoxChecked();
		if (chkCount > 0) {
			$(".removeUpFileBtn").removeAttr("disabled");
			$(".removeUpFileBtn").val(chkCount + " 개 파일을 삭제합니다");
		} else if (chkCount == 0){
			$(".removeUpFileBtn").attr('disabled', true);
			$(".removeUpFileBtn").val("선택된 파일이 없습니다");
		}
		
	}
	
	function countCheckBoxChecked() {
		// 체크된 체크박스 갯수 
		let result = 0;
		$(".fileCheck").each(function (i, item) {
// 			console.log($(item).is(":checked")); // true
			if ($(item).is(":checked")){
				result++;
			}
		});
		
		console.log(result + "개가 체크됨");
		return result;
	}
	
	function removeFile() {
		// 비활성화해놓은 [선택된파일삭제]버튼 클릭시 호출
		let removeFileArr = [];
		
		$(".fileCheck").each(function(i, item) {
			if ($(item).is(":checked")){ // 파일 삭제하려고 체크박스를 체크했다면
				let tmp = $(item).attr('id'); // 선택된 파일의 id값을 얻어옴 (pk)
				removeFileArr.push(tmp); // 배열에 저장
			}
		});
		
		console.log("삭제될 파일 : " + removeFileArr);
		
		$.each(removeFileArr, function(i, item){
			$.ajax({
		          url: "/hboard/modifyRemoveFileCheck", // 데이터가 송수신될 서버의 주소
		          type: "POST", // 통신 방식 (GET, POST, PUT, DELETE)
		          dataType: "json", // 수신 받을 데이터 타입 (MIME TYPE)
		          data: { // 보내는 데이터
		        	  "removeFileNo" : item
		          },
		          async: false, // 동기통신방식
		          success: function (data) {  // 통신이 성공하면 수행할 함수
		            console.log(data);
		          	if (data.msg == 'success') {
		          		$('#'+item).parent().parent().css('opacity', 0.2);
		          	}
		          },
		          error: function () {},
		          complete: function () {},
		        });
			
		});
		
		function cancelRemFiles() {
		      $.ajax({
		         url : "/hboard/cancelRemFiles",
		         type : "POST",
		         dataType : "json",
		         async : false,
		         success : function(data) {
		            if (data.msg == 'success') {
		               $(".fileCheck").each(function(i, item) {
		                  $(item).prop('checked', false);
		                  $(item).parent().parent().css('opacity', 1);
		               });

		               $(".removeUpFileBtn").attr("disabled", true);
		               $(".removeUpFileBtn").val("선택된 파일이 없습니다.");
		            }

		         },
		         error : function() {
		         },
		         complete : function() {
		         }
		      });
		   }
	
		
		//파일 추가
		function addRows(obj){
			let rowCnt = ${'.fileListTable tr'}.length;
			let row = $(obj).parent().parent()
			let inputFileTag = `<tr><td colspan='2'><input type ='file' class= 'form-control' id='newFile_\${rowCnt}' name="modifyNewFile" onchange='showPreview(this);'/></td>
			<td><input type='button' class='btn btn-danger modalCloseBtn' vlaue='파일저장취소' onclick='cancleAddFile(this);'></td></tr>`;
			
			$(inputFileTag).insertBefore(row);
			
		}
		
		function showPreview(obj){
			if(obj.files[0].size> 1024*1024*10){
				alert("10MB이하의 파일만 업로드할 수 있습니다")
				obj.value="";
				return;
			}
			//파일 타입 확인
			let imageType = ["image/jpeg", "image/png","image/gif","image/jpg" ]
			let fileType = obj.files[0].type;
			let fileName = obj.files[0].name;
			
			if(imageType.indexOf(fileType) != -1){ //이미지 파일이라면
				let reader = new FileRead(); // FileReader객체 생성
				reader.readAsDataURL(obj.files[0]); //업로드된 파일을 읽어온다.
				reader.onload = function(e){
					//reader객체에 의해 파일을 읽기 완료하면 실행되믄 콜백함수
					console.log(e.target);
					let imaTag = `<div style='padding: 6px;'><img src='\${e.target.result}' width='50px' /><span>\${fileName}</span></div>`;
					$(imgTag).insertAfter(obj);
				}
			}else{//이미지 파일이 아니라면
				let imgTag = `<div style='padding: 6px;'><img src='/resources/images/noimage.png/' width='50px' /><span>\${fileName}</span></div>`;
				}
			}
		function cancleAddFile(obj){
			let fileTag = $(obj).parent().prev().children().eq(0);
			$(fileTag).val('');
			$(fileTag).parent().parent().remove();
			
		}}
			
</script>
<style type="text/css">
.fileBtns {
	display: flex;
	justify-content: flex-end;
}
</style>
</head>
<body>
	<jsp:include page="./../header.jsp"></jsp:include>
	<div class="container">
		<div>${boardDetailInfo }</div>


		<h1>게시글 수정</h1>
		<c:forEach var="board" items="${ boardDetailInfo}">
			<form action="/hboard/modifyBoardSave" method="post" enctype="multipart/form-data">
				<div class="boardInfo">
					<div class="input-group mb-3">
						<span class="input-group-text">글 번호</span> <input type="text"
							class="form-control" id="boardNo" name = "boardNo" value="${board.boardNo }"
							readonly />
					</div>

					<div class="input-group mb-3">
						<span class="input-group-text">글 제목</span> <input type="text"
							class="form-control" id="title" name="title" value="${board.title }" />
					</div>

					<div class="input-group mb-3">
						<span class="input-group-text">작성자(이메일)</span> <input type="text"
							class="form-control" id="writer" 
							value="${board.writer }(${board.email })" readonly />
					</div>

					<div class="input-group mb-3">
						<span class="input-group-text">작성일</span> <input type="text"
							class="form-control" id="postDate" value="${board.postDate }"
							readonly />
					</div>

					<div class="input-group mb-3">
						<span class="input-group-text">조회수</span> <input type="text"
							class="form-control" id="readCount" value="${board.readCount }"
							readonly />
					</div>

					<div class="mb-3">
						<label for="content">내용:</label>
						<textarea class="form-control" rows="5" name="content" id="content">${board.content }</textarea>
					</div>
				</div>
				<div class="fileList">
					<table class="table table-hover">
						<thead>
							<tr>
								<th>#</th>
								<th>uploadedFiles</th>
								<th>fileName</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="file" items="${board.fileList }">
								<c:if test="${file.boardUpFileNo != '0' }">
									<tr>
										<td><input type="checkbox"
											class="form-check-input fileCheck"
											id="${file.boardUpFileNo }"
											onclick="removeFileCheckBx(this.id);"></td>
										<td><c:choose>
												<c:when test="${file.thumbFileName != null }">
													<img alt=""
														src="/resources/boardUpFiles/${file.thumbFileName }" />
												</c:when>
												<c:when test="${file.thumbFileName == null }">
													<a href="/resources/boardUpFiles/${file.newFileName }">
														<img src="/resources/images/noimage.png" />${file.newFileName }</a>
												</c:when>
											</c:choose></td>
										<td>${file.originFileName }</td>
									</tr>
								</c:if>
							</c:forEach>
							<tr>
								<td colspan="3" style="text-align: center;"><img alt=""
									src="/resources/images/add.png" /></td>
							</tr>
						</tbody>
					</table>
					<div class="fileBtns">
						<input type="button" class="btn btn-danger removeUpFileBtn"
							disabled value="선택한 파일 삭제" onclick="removeFile();" /> <input
							type="button" class="btn btn-secondary cancelRemove"
							value="파일 삭제 취소" onclick="cancelRemFiles();" />
					</div>
				</div>

				<div class="btns">
					<button type="submit" class="btn btn-primary">저장</button>
					<button type="button" class="btn btn-warning"
						onclick="location.href='/hboard/viewBoard?boardNo=${board.boardNo}'">취소</button>
				</div>
			</form>
		</c:forEach>
	</div>
	<jsp:include page="./../footer.jsp"></jsp:include>
	<!-- The Modal -->
	<div class="modal" id="myModal">
		<div class="modal-dialog">
			<div class="modal-content">

				<!-- Modal Header -->
				<div class="modal-header">
					<h4 class="modal-title">Mini-Project</h4>
					<button type="button" class="btn-close modalCloseBtn"
						data-bs-dismiss="modal"></button>
				</div>

				<!-- Modal body -->
				<div class="modal-body">Modal body..</div>

				<!-- Modal footer -->
				<div class="modal-footer">
					<button type="button" class="btn btn-info" data-bs-dismiss="modal"
						onclick="location.href='/hboard/removeBoard?boardNo=${param.boardNo}'">삭제</button>
					<button type="button" class="btn btn-danger modalCloseBtn"
						data-bs-dismiss="modal">취소</button>
				</div>

			</div>
		</div>
	</div>
</body>
</html>
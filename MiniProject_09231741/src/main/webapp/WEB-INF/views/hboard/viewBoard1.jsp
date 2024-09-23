<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>게시글 조회</title>
</head>
<body>
<jsp:include page="./../header.jsp"></jsp:include>
	<div class="container">
	<div>${board }</div>
	<div>${fileList }</div>
	
		<h1>게시글 조회</h1>
			<div class="input-group mb-3">
			  <span class="input-group-text">글 번호</span>
			  <input type="text" class="form-control" name="title" id="title" value="${board.boardNo }" readonly />
			</div>
		
			<div class="input-group mb-3">
			  <span class="input-group-text">글 제목</span>
			  <input type="text" class="form-control" name="title" id="title" value="${board.title }" readonly />
			</div>

			<div class="input-group mb-3">
			  <span class="input-group-text">작성자</span>
			  <input type="text" class="form-control" name="writer" value="${board.writer }" readonly />
			</div>
			
			<div class="mb-3">
				<label for="content">내용:</label>
				<textarea class="form-control" rows="5" id="content" name="content"  readonly >${board.content }</textarea>
			</div>
		
		<div class="fileList">
			<c:forEach var="file" items="${fileList }">
				<c:choose>
					<c:when test="${file.thumbFileName != null }">
						<div><img src="/resources/boardUpFiles/${file.thumbFileName }" /></div>
					</c:when>
					<c:otherwise>
						<div><img src="/resources/images/noimage.png"/>${file.originFileName }</div>
					</c:otherwise>
				</c:choose>
			</c:forEach>
		
		</div>
		
			<button type="button"  class="btn btn-primary" onclick="">수정</button>
			<button type="button"  class="btn btn-warning" onclick="">삭제</button>
			<button type="button"  class="btn btn-secondary" onclick="">목록보기</button>
		
	</div>
<jsp:include page="./../footer.jsp"></jsp:include>	
</body>
</html>
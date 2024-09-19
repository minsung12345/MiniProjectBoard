<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<title>Insert title here</title>
<style>
	.topHeader {
		background-image: url("/resources/images/headerBackground.jpg");
		background-size : cover;
		background-position: center;
		height : 300px;
	}
</style>
</head>
<body>
<div class="p-5 bg-primary text-white text-center topHeader" >
  <h1>Spring Mini-Project</h1>
  <p>2024</p> 
</div>

<nav class="navbar navbar-expand-sm bg-dark navbar-dark">
  <div class="container-fluid">
    <ul class="navbar-nav">
      <li class="nav-item">
        <a class="nav-link active" href="/">Webkms</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="/hboard/listAll">계층형게시판</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="/member/register">회원가입</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="#">로그인</a>
      </li>
      <li class="nav-item">
        <a class="nav-link disabled" href="#">Disabled</a>
      </li>
    </ul>
  </div>
</nav>
</body>
</html>
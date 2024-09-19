-- 회원테이블 생성
CREATE TABLE `sky`.`member` (
  `userId` VARCHAR(8) NOT NULL,
  `userPwd` VARCHAR(200) NOT NULL,
  `userName` VARCHAR(12) NULL,
  `mobile` VARCHAR(13) NULL,
  `email` VARCHAR(50) NULL,
  `registerDate` DATETIME NULL DEFAULT now(),
  `userImg` VARCHAR(50) NOT NULL DEFAULT 'avatar.png',
  `userPoint` INT NULL DEFAULT 100,
  PRIMARY KEY (`userId`),pointdef
  UNIQUE INDEX `mobile_UNIQUE` (`mobile` ASC) VISIBLE,
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE);
  
  -- 계층형 게시판 생성
  CREATE TABLE `sky`.`hboard` (
  `boardNo` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(20) NOT NULL,
  `content` VARCHAR(2000) NULL,
  `writer` VARCHAR(8) NULL,
  `postDate` DATETIME NULL DEFAULT now(),
  `readCount` INT NULL DEFAULT 0,
  `ref` INT NULL DEFAULT 0,
  `step` INT NULL DEFAULT 0,
  `refOrder` INT NULL DEFAULT 0,
  PRIMARY KEY (`boardNo`),
  INDEX `hboard_member_fk_idx` (`writer` ASC) VISIBLE,
  CONSTRAINT `hboard_member_fk`
    FOREIGN KEY (`writer`)
    REFERENCES `sky`.`member` (`userId`)
    ON DELETE SET NULL
    ON UPDATE NO ACTION)
COMMENT = '계층형게시판';

-- 회원 가입 
insert into member(userId, userPwd, userName, mobile, email) 
values('tosimi', sha1(md5('1234')), '토심이', '010-2222-6666', 'tosimi@abc.com' );

insert into member(userId, userPwd, userName, mobile, email) 
values('tomoong', sha1(md5('1234')), '토뭉이', '010-2222-8888', 'tomoong@abc.com' );

select * from member;

-- 게시판에 게시글 등록
insert into hboard (title, content, writer)
values ('아싸~~ 1등이다~~', '내용 무', 'tosimi');

insert into hboard (title, content, writer)
values ('에이~', '1등 놓쳤네', 'tomoong');

select * from hboard order by boardNo desc;

-- insert into hboard (title, content, writer) values (#{title}, #{content}, #{writer} );

-- pointdef 테이블 생성
CREATE TABLE `sky`.`pointdef` (
  `pointWhy` VARCHAR(20) NOT NULL,
  `pointScore` INT NULL,
  PRIMARY KEY (`pointWhy`))
COMMENT = '유저에게 적립할 포인트에 대한 정책 정의 테이블\n어떤 사유로 몇 포인트를 지급하는지에 대해 정의';

INSERT INTO `sky`.`pointdef` (`pointWhy`, `pointScore`) VALUES ('회원가입', '100');
INSERT INTO `sky`.`pointdef` (`pointWhy`, `pointScore`) VALUES ('로그인', '1');
INSERT INTO `sky`.`pointdef` (`pointWhy`, `pointScore`) VALUES ('글작성', '10');
INSERT INTO `sky`.`pointdef` (`pointWhy`, `pointScore`) VALUES ('댓글작성', '2');

-- pointlog 테이블
CREATE TABLE `sky`.`pointlog` (
  `pointLogNo` INT NOT NULL AUTO_INCREMENT,
  `pointWho` VARCHAR(8) NOT NULL,
  `pointWhen` DATETIME NULL DEFAULT now(),
  `pointWhy` VARCHAR(20) NOT NULL,
  `pointScore` INT NOT NULL,
  PRIMARY KEY (`pointLogNo`),
  INDEX `pointlog_member_fk_idx` (`pointWho` ASC) VISIBLE,
  CONSTRAINT `pointlog_member_fk`
    FOREIGN KEY (`pointWho`)
    REFERENCES `sky`.`member` (`userId`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
COMMENT = '어떤 멤버에게 어떤 사유로 몇 포인트가 언제 지급되었는지 기록 테이블';

-- 글 작성시 멤버에게 포인트 로그를 저장하는 쿼리문
insert into pointlog(pointWho, pointWhy, pointScore) 
values('tosimi', '글작성', (select pointScore from pointdef where pointWhy = '글작성'));

select * from pointlog;

-- 멤버에게 지급된 point를 더해서 수정하는 쿼리문
update member set userPoint = userPoint + (select pointScore from pointdef 
where pointWhy = '글작성')
where userId = 'tosimi';

select * from member;
select * from hboard; 
rollback;
commit;


-- hboard게시글에 업로드하는 파일정보 저장
CREATE TABLE `sky`.`boardupfiles` (
  `boardUpFileNo` INT NOT NULL AUTO_INCREMENT,
  `newFileName` VARCHAR(100) NOT NULL,
  `originFileName` VARCHAR(100) NOT NULL,
  `thumbFileName` VARCHAR(100) NULL,
  `ext` VARCHAR(20) NULL,
  `size` INT NULL,
  `boardNoboardupfiles` INT NULL,
  `base64Img` TEXT NULL,
  PRIMARY KEY (`boardUpFileNo`),
  INDEX `boardupfiles_boardNo_fk_idx` (`boardNo` ASC) VISIBLE,
  CONSTRAINT `boardupfiles_boardNo_fk`
    FOREIGN KEY (`boardNo`)
    REFERENCES `sky`.`hboard` (`boardNo`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
COMMENT = '게시판에 업로드되는 파일을 기록하는 테이블';

-- 게시글에 첨부한 파일정보를 저장
select max(boardNo) from hboard;

-- 업로드된 첨부파일을 저장하는 쿼리문
-- insert into boardupfiles(newFileName, originFileName, thumbFileName, ext, size, boardNo, base64Img) values(#{newFileName}, #{originFileName}, #{thumbFileName}, 
#{ext}, #{size}, #{boardNo}, #{base64Img})

-- 게시글 번호(boardNo)로 조회
select * from hboard where boardNo = 20;

-- 업로드 파일 조회
select * from boardupfiles where boardNo = 20;

-- 게시글 상세페이지
-- boardNo번째 글의 hboard 모든 컬럼과, 해당글의 모든 업로드파일과, 작성자의 이름과 이메일을 가져오기위한 쿼리문
select h.boardNo, h.title, h.content, h.writer, h.postDate, h.readCount, 
h.ref, h.step, h.refOrder, f.*, m.userName, m.email
from hboard h left outer join boardupfiles f
on h.boardNo = f.boardNo
inner join member m
on h.writer = m.userId
where h.boardNo = 19;

-- 
CREATE TABLE `sky`.`boardreadlog` (
  `boardReadLogNo` INT NOT NULL AUTO_INCREMENT,
  `readWho` VARCHAR(130) NOT NULL,
  `readWhen` DATETIME NULL DEFAULT now(),
  `boardNo` INT NULL,
  PRIMARY KEY (`boardReadLogNo`),
  INDEX `boardreadlog_boardno_fk_idx` (`boardNo` ASC) VISIBLE,
  CONSTRAINT `boardreadlog_boardno_fk`
    FOREIGN KEY (`boardNo`)
    REFERENCES `sky`.`hboard` (`boardNo`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
COMMENT = '조회수처리를 위한 클라이언트의 ip와 게시글 읽은 시간, 게시글번호를 저장하기 위한 테이블';

-- ipAddr의 유저가 boardNo글을 언제 조회했는지 날짜 차이 계산
-- 24시간 (1일)이 지났는지 안 지났는지?
SELECT datediff(now(), '2024-09-05 17:00:00');
select datediff(now(), readWhen) from boardreadlog 
where readWho = '127.0.0.1' and boardNo = 22;

-- 1) ipAddr의 유저가 22번 글을 언제 조회했는지 
-- 조회한적이 있다면 시간 반환
select readWhen from boardreadlog where readWho = '127.0.0.1' and boardNo = 22;

-- 한번도 2번글을 '127.0.0.1'가 조회한 적이 없음. (최초조회)
select readWhen from boardreadlog where readWho = '127.0.0.1' and boardNo = 2;

-- 2) 1)번에서 나온 결과가 null이면 insert
insert into boardreadlog(readwho, boardNo) values(?, ?)

-- 3) 1)번에서 나온 결과가 null이 아니면....현재시간과 이전에 읽은 날짜시간의 차이를 구해야 한다.
select readWhen from boardreadlog where readWho = ? and boardNo = ?

select ifnull(datediff(now(),
	(select readWhen from boardreadlog 
    where readWho = '127.0.0.1' and boardNo = 22)), -1)
    as datediff; 
    
-- 조회수 증가 (update)
update hboard set readCount = readCount + 1 where boardNo = #{boardNo}
-- ipAddr의 유저가 boardNo글을 언제 조회했는지 날짜차이(단, 조회한 적이 없다면 -1반환)

-- 조회한지 24시간이 지났다면, 아이피주소가 boardNo번 글을 읽은 시간을 테이블에 수정 (update) 
update boardreadlog set readWhen = now() 
where readWho = #{readWho} and boardNo = #{boardNo}

select ifnull(datediff(now(),
	(select readWhen from boardreadlog 
    where readWho = ? and boardNo = ?)), -1)
    as datediff


-- ---- 계층형 게시판 답글 ---------------------------------------------------
-- 1) 기존 게시글의 ref 컬럼 값을 boardNo값으로 update

-- 2) 앞으로 저장할 게시글에 ref컬럼 값을 boardNo 값으로 update
update hboard set ref = ? 
where boardNo = ?

-- 2-2) 부모글에 대한 다른 답글이 있는 상태에서, 부모글의 답글이 추가되는 경우,
-- (자리확보를 위해) 기존의 답글의 refOrder값을 수정(+1)해야 한다.
-- ref = pRef and pRefOrder < refOrder 를 찾는다
update hboard set refOrder = refOrder + 1
where ref = ? and refOrder > ?

-- 2-1) 답글을 입력받아서, 답글 저장
-- ref = pRef, step = pStep + 1, refOrder = pRefOrder + 1로 등록한다.
insert into hboard(title, content, writer, ref, step, refOrder)
values(?, ?, ?, ?, ?, ?)

-- 게시글 전체 목록을 가져올 때 정렬 방식 변경해야 한다.
-- ref 내림차순, refOrder 오름차순으로 정렬
select * from hboard order by ref desc, refOrder asc

-- 게시글 삭제
-- isDelete 컬럼추가 : 기본값 'N'
ALTER TABLE `sky`.`hboard` 
ADD COLUMN `isDelete` CHAR(1) NULL DEFAULT 'N' AFTER `refOrder`;

-- 게시글 내용 삭제 


-- 업로드 파일 삭제


-- 게시글 수정
-- 1) 순수게시글 update
update hboard set title = ?, content = ?
where boardNo = ?
			
-- 2) 업로드파일의 fileStatus = INSERT이면 insert, DELETE이면 delete
-- insert into boardupfiles(newFileName, originFileName, thumbFileName, ext, size, boardNo, base64Img) values(#{newFileName}, #{originFileName}, #{thumbFileName}, 
#{ext}, #hboard{size}, #{boardNo}, #{base64Img})

delete from boardupfiles 
where boardUpFileNo = ?

-- --------------- 페이징 처리 ------------------------------
-- 페이징 (pagination) : 많은 데이터를 일정 단위로 끊어서 출력

-- select * from hboard order by ref desc, refOrder asc limit 보여주기시작할 index번호, 1페이지당보여줄글의갯수
select * from hboard order by ref desc, refOrder asc limit 0, 10;
select * from hboard order by ref desc, refOrder asc limit 10, 10;
select * from hboard order by ref desc, refOrder asc limit 20, 10;

-- 1) 게시판의 전체 글의 수 
select count(*) from hboard;  -- 326

-- 2) 전체 페이지 수
-- 만약 1페이지당 보여줄 글의 갯수 10개라고 가정한다.
-- 전체 페이지 수 : 326 / 10 = 32...6 --> 33페이지
-- ==> 전체 페이지 수 : 전체 글의 수 / 1페이지당 보여줄 글의 갯수 
--                   -> 나누어 떨어지면 몫
--                   -> 나누어 떨어지지 않으면 몫 + 1

-- 
-- 3) ?번 페이지에서 보여줄 글의 시작index번호를 구하자
-- ==> (페이지번호 - 1) * (한페이지당 보여줄 글의 갯수)
-- 1페이지 : 0 = (1 - 1) * 10
-- 2페이지 : 10 = (2 - 1) * 10
-- 3페이지 : 20 = (3 - 1) * 10 

-- -------- 페이징 블럭 ---------
-- 1페이지 ~ 10페이지
-- 11페이지 ~ 20페이지
-- 1) 1개 페이징 블럭에서 보여줄 페이지 수 : 10

-- 1-2) 현재 페이지가 속한 페이징 블럭의 번호?
-- 7  --> 1번 블럭 : 7 / 10 => 나누어 떨어지지 않는 경우 : 0 + 1 번 블럭에 속함
-- 14 --> 2번 블럭 : 14 / 10 => 나누어 떨어지지 않는 경우 : 1 + 1  번 블럭에 속함
-- 30 --> 3번 블럭 : 30 / 10 => 나누어 떨어지는 경우 : 3번 블럭에 속함

-- ==> (현재페이지 번호) / (1개 페이징 블럭에서 보여줄 페이지 수) 
--     -> 나누어 떨어지면 몫
--     -> 나누어 떨어지지 않으면 + 1

-- 2) 현재 ?번 페이징 블럭에서 출력시작할 페이지 번호=?
-- ==> (현재페이징 블럭번호 - 1) * (1개 페이징 블럭에서 보여줄페이지 수) + 1
-- 7페이지  : 1번 블럭 -> (1 - 1) * 10 + 1 --> 시작페이지 = 1번페이지 ~ 10
-- 14페이지 : 2번 블럭 -> (2 - 1) * 10 + 1 --> 시작페이지 = 11번페이지 ~ 20
-- 30페이지 : 3번 블럭 -> (3 - 1) * 10 + 1 --> 시작페이지 = 21번페이지 ~ 30

-- 3) 현재 ?번 페이징 블럭에서 출력할 마지막 페이지 번호=?
-- 2)번에서 나온 값 + (1개 페이징 블럭에서 보여줄페이지 수 - 1)


-- 게시판 검색 ---------------------------------------- 
-- 검색어 : 'tosi'
-- title로 검색
select * from hboard where title like '%업로%';

-- writer로 검색
select * from hboard where writer like '%tosim%';

-- content로 검색
select * from hboard where content like '%저장%';

select * from hboard where isDelete = 'N' and writer like '%tomoon%';

-- 검색된 글의 갯수
select count(*) from hboard where isDelete = 'N' and writer like '%tomoon%';

select count(*) from hboard where writer like '%tosim%';
select count(*) from hboard;

select * from hboard where isDelete = 'N' order by readCount desc limit 5;


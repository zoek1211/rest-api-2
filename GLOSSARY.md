# 게시판

글과 그 글에 달린 댓글을 다루는 게시판 REST API의 용어집입니다.

## Language

**글 (Post)**:
게시판에 올린, 제목과 내용으로 이루어진 글 한 편.
_Avoid_: 게시물, Article, Board

**댓글 (PostComment)**:
하나의 글에 달린 짧은 글. 글 없이는 존재할 수 없고, 글이 삭제되면 함께 삭제된다.
_Avoid_: Comment, Reply, 답글

**샘플 데이터 (Init Data)**:
앱이 처음 시작될 때 저장된 글이 하나도 없으면 자동으로 넣는 글 3건과 댓글 5건.
_Avoid_: 더미 데이터, 시드, Fixture

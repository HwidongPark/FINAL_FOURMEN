# FINAL_FOURMEN
<hr>
<h3>팀 구성: 강동균, 김지호 권오중, 박휘동</h3>
<hr>
url: <a href="http://www.fourmenmedia.link">http://www.fourmenmedia.link</a>
<hr>

<p>점점 많아지는 OTT 플랫폼, 어디서 내가 원하는 영화, TV프로그램을 시청하지?</p>
<p style="color: blue; font-size: 1.1rem; font-weight: 700;">인기, 최신 영화/TV시리즈 및 배우, 감독을 한눈에 볼 수 있는 웹사이트</p>
<ul>
  <li>각 영화/TV프로그램이 어느 곳에서 시청 가능한지 알아보자</li>
  <li>각 영화/TV프로그램에 대해 토론 가능한 오픈채팅</li>
  <li>영화/TV/인물에 대해 자유롭게 토론할 수 있는 자유 게시판</li>
</ul>

<br>

<hr>

<hr>

<h3>내 역할</h3>
<ul>
  <li>
    데이터베이스 설계
  </li>
  <li>
    REST API호출로 데이터를 받아와 인기/최신/최고 평점 영화 리스트 구현
  </li>
  <li>IntersectionObserver를 사용하여 무한 스크롤 구현</li>
  <li>
    영화 디테일 페이지 구현
    <ul>
      <li>
        REST API호출로 데이터를 받아와 구현
      </li>
      <li>
        ColorThief를 사용하여 영화 포스터의 주된 색에 따라 background color 변경
      </li>
      <li>
        리뷰 및 평점 구현(스포일러를 포함한 리뷰일 시 숨기도록 함)
      </li>
      <li>
        플레이리스트 생성 및 추가
        <ul>
          <li>플레이리스트 나만보기/전체공개 설정</li>
          <li>마우스 드래그를 통한 플레이리스트 내 작품들 순서 변경</li>
          <li>플레이르스트 좋아요</li>
        </ul>
      </li>
      <li>
        작품 좋아요
      </li>
    </ul>
  </li>
  <li>
    Web Sockete을 사용한 영화별 오픈채팅방 구현
  </li>
  <li>
    영화/TV/인물 자유 게시판
    <ul>
      <li>CkEditor를 활용하여 사진, 동영상 첨부 가능한 게시판 구현</li>
      <li>
        댓글, 대댓글, 대댓글에 대댓글 구현
        <ul>
          <li>재귀 메서드 활용</li>
        </ul>
      </li>
      <li>
        게시물 댓글, 대댓글 좋아요
      </li>
      <li>
        게시판 페이징
      </li>
    </ul>
  </li>
  <li>
    반응형 nav
    <ul>
      <li>
        화면이 클 경우
        <ul>
          <li>
            메뉴 상세 항목들이 화면 위에서 아래로 나오도록 함
          </li>
        </ul>
      </li>
      <li>
        화면이 작을 경우
        <ul>
          <li>메뉴 항목들이 왼쪽에서 슬라이드 돼서 나오도록 함</li>
          <li>각 메뉴 항목들에 toggle button을 만들어, 클릭하면 그 항목에 해당하는 상세 항목들이 나오도록 함</li>
        </ul>
      </li>
    </ul>
  </li>
  <li>
    AWS EC2, S3, RDS, Bean Stalk활용한 배포
    <ul>
      <li>
        CodePipeline 구축을 통한 자동배포(CI/CD)
      </li>
    </ul>
  </li>
</ul>

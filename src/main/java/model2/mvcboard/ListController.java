package model2.mvcboard;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model1.board.BoardDTO;
import utils.BoardPage;

public class ListController extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		
		//커넥션 풀을 이용한 DB연결
		MVCBoardDAO dao = new MVCBoardDAO();
		
		//파라미터 및 View로 전달할 데이터 저장용 Map컬렉션 생성
		Map<String, Object> map = new HashMap<String, Object>();
		
		//검색어 관련 파라미터
		String searchField = req.getParameter("searchField");
		String searchWord = req.getParameter("searchWord");
		
		if(searchWord != null){
			//검색어를 입력한 경우 Model로 전달하기 위해 저장
			map.put("searchField", searchField);
			map.put("searchWord", searchWord);
		}
		//게시물 갯수 카운트
		int totalCount = dao.selectCount(map);


		/**** 페이지 처리 start ****/
		//Web.xml에 접근하기 위해 서블릿에서 application 내장객체를 얻어옴.
		ServletContext application = getServletContext();
		//컨텍스트 초기화 파라미터를 얻어옴
		int pageSize = Integer.parseInt(application.getInitParameter("POSTS_PER_PAGE"));
		int blockPage = Integer.parseInt(application.getInitParameter("PAGES_PER_BLOCK"));
		
		/*
		목록에 첫 진입시에는 무조건 1페이지로 가정한 후 게시물을 얻어옴.
		특정 페이지로 진입한 경우에는 파라미터를 받아서 구간을 계산해서 얻어옴
		*/
		int pageNum = 1;
		String pageTemp = req.getParameter("pageNum");
		if(pageTemp != null && !pageTemp.equals(""))
			pageNum = Integer.parseInt(pageTemp); //정수로 변경한 후 저장

		//게시물의 구간을 계산
		/*
		우선 한페이지에 출력할 게시물의 범위를 결정한다. MySQL에서는 limit를 
		사용하므로 start(시작위치)만 계산하면 된다.
			start =>
				1페이지일때 : (1-1) * 5 = 0
				2페이지일때 : (2-1) * 5 = 5
			end =>
				한 페이지 출력할 게시물의 갯수인 pageSize를 그대로 사용하면 된다.
				
		*/
		int start = (pageNum - 1) * pageSize;
		int end = pageSize;
		
		map.put("start", start); 
		map.put("end", end);
		/**** 페이지 처리 end ****/

		//현재 페이지에 출력할 게시물을 얻어온다.
		List<MVCBoardDTO> boardLists = dao.selecListPage(map);
		//커넥션풀에 자원반납
		dao.close();
		
		//페이지 번호를 생성하기 위해 메서드 호출
		String pagingImg = BoardPage.pagingStr(totalCount, pageSize,
				blockPage, pageNum, "../mvcboard/list.do");
		//View로 전달할 데이터를 Map커렉션에 저장
		map.put("pagingImg", pagingImg); // 페이지 번호
		map.put("totalCount", totalCount); //전체 게시물의 갯수
		map.put("pageSize", pageSize); //한 페이지에 출력할 게시물의 갯수
		map.put("pageNum", pageNum); //페이지 번호
		
		//View로 전달할 객체들을 request영역에 저장
		req.setAttribute("boardLists", boardLists);
		req.setAttribute("map", map);
		//View로 포워드를 걸어준다.
		req.getRequestDispatcher("/14MVCBoard/List.jsp").forward(req, resp);
	}
}

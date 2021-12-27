package model2.mvcboard;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import common.DBConnPool;
import membership.MemberDTO;
import model1.board.BoardDTO;

public class MVCBoardDAO extends DBConnPool{
	
	//기본생성자는 없어도 동작에는 영향이 없다.
	public MVCBoardDAO() {
		super();
	}
	
	//검색 조건에 맞는 게시물의 개수를 반환합니다.
	public int selectCount(Map<String, Object> map) {
		int totalCount = 0;
		String query = "SELECT COUNT(*) FROM mvcboard";
		if(map.get("searchWord") != null) {
			query += " WHERE " + map.get("searchField")+ " "
					+" LIKE '%" +map.get("searchWord")+ "%'";
		}
		
		try {
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			rs.next();
			totalCount = rs.getInt(1);
		}
		catch(Exception e) {
			System.out.println("게시물 카운트 중 예외 발생");
			e.printStackTrace();
		}
		return totalCount;
	}
	
	public List<MVCBoardDTO> selecListPage(Map<String, Object>map){
		List<MVCBoardDTO> board = new Vector<MVCBoardDTO>();
		
		String query = " SELECT * FROM mvcboard ";
		
		if(map.get("searchWord") != null) {
			query += " WHERE " + map.get("searchField")+" "
					+" LIKE '%" +map.get("searchWord")+ "%' ";
		}
		query += " ORDER BY idx DESC LIMIT ?, ? ";
		
		try {
			psmt = con.prepareStatement(query);
			psmt.setInt(1, Integer.parseInt(map.get("start").toString()));
			psmt.setInt(2, Integer.parseInt(map.get("end").toString()));
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				MVCBoardDTO dto = new MVCBoardDTO();
				
				dto.setIdx(rs.getString(1));
				dto.setName(rs.getString(2));
				dto.setTitle(rs.getString(3));
				dto.setContent(rs.getString(4));
				dto.setPostdate(rs.getDate(5));
				dto.setOfile(rs.getString(6));
				dto.setSfile(rs.getString(7));
				dto.setDowncount(rs.getInt(8));
				dto.setPass(rs.getString(9));
				dto.setVisitcount(rs.getInt(10));
				
				board.add(dto);
			}
		}
		catch(Exception e) {
			System.out.println("게시물 조회 중 예외 발생");
			e.printStackTrace();
		}
		return board;
	}
	//새로운 게시물에 대한 입력처리
	public int insertWrite(MVCBoardDTO dto) {
		int result = 0;
		try {
			String query = "INSERT INTO mvcboard ( "
						 + " name, title, content, ofile, sfile, pass) "
						 + " VALUES ( "
						 + " ?,?,?,?,?,?)";
		//동적쿼리문 실행을 위한 prepareStatement 객체 생성
		psmt = con.prepareStatement(query);
		//순서대로 인파라미터 설정
		psmt.setString(1, dto.getName());
		psmt.setString(2, dto.getTitle());
		psmt.setString(3, dto.getContent());
		psmt.setString(4, dto.getOfile());
		psmt.setString(5, dto.getSfile());
		psmt.setString(6, dto.getPass());
		//쿼리문 실행 : 입력에 성공한다면 1이 반환되고 실패시 0반환.
		result = psmt.executeUpdate();
		}
		catch(Exception e) {
			System.out.println("게시물 입력 중 예외 발생");
			e.printStackTrace();
		}
		return result;
	}
	// 주어진 일련번호에 해당하는 게시물을 DTO에 담아 반환한다.
	public MVCBoardDTO selectView(String idx) {
		
		MVCBoardDTO dto = new MVCBoardDTO();//DTO객체 생성
		String query = "SELECT * FROM mvcboard WHERE idx=?";
		try{
			psmt = con.prepareStatement(query);
			psmt.setString(1,idx);
			rs = psmt.executeQuery();
			//일련번호는 중복되지 않으므로 if문에서 처리하면 된다.
			if(rs.next()) {//결과를 DTO에 저장 
				//DTO객체에 레코드의 내용을 추가한다.
				dto.setIdx(rs.getString(1));
				dto.setName(rs.getString(2));
				dto.setTitle(rs.getString(3));
				dto.setContent(rs.getString(4));
				dto.setPostdate(rs.getDate(5));
				dto.setOfile(rs.getString(6));
				dto.setSfile(rs.getString(7));
				dto.setDowncount(rs.getInt(8));
				dto.setPass(rs.getString(9));
				dto.setVisitcount(rs.getInt(10));
			}
		}
		catch(Exception e) {
			System.out.println("게시물 상세보기 중 예외 발생");
			e.printStackTrace();
		}
		return dto;
	}
	//주어진 일련번호에 해당하는 게시물의 조회수를 1 증가시킨다.
	public void updateVisitCount(String idx) {
		//visitcount 컬럼은 number 타입임므로 덧셈(연산)이 가능하다.
		String query = "UPDATE mvcboard SET "
					 + " visitcount=visitcount+1 "
					 + " WHERE idx=?";
		try {
			psmt = con.prepareStatement(query);
			psmt.setString(1, idx);
			psmt.executeQuery();
		}
		catch(Exception e) {
			System.out.println("게시물 조회수 증가 중 예외 발생.");
			e.printStackTrace();
		}
	}
	//주어진 일련번호에 해당하는 게시물의 다운로드 수를 증가시킴
	public void downCountPlus(String idx) {
		//downcount 컬럼은 number 타입임므로 덧셈(연산)이 가능하다.
		String sql = "UPDATE mvcboard SET "
					 + " downcount=downcount+1 "
					 + " WHERE idx=?";
		try {
			psmt = con.prepareStatement(sql);
			psmt.setString(1, idx);
			psmt.executeQuery();
		}
		catch(Exception e) {}
	}
	//패스워드 검증을 위해 해당 게시물이 존재하는지 확인
	public boolean confirmPassword(String pass, String idx) {
		
		boolean isCorr = true;
		try {
			//패스워드와 일련번호를 통해 조건에 맞는 게시물이 있는지 확인
			String sql = "SELECT COUNT(*) FROM mvcboard WHERE pass=? AND idx=?";
			psmt = con.prepareStatement(sql);
			//인파라미터 설정
			psmt.setString(1, pass);
			psmt.setString(2, idx);
			rs = psmt.executeQuery();
			//커서 이동을 위한 next()호출. count()함수는 항상 결과를 반환하므로
			//if문은 별도로 필요하지 않다.
			rs.next();
			if(rs.getInt(1) == 0) { // 결과가 없을 때 false로 처리
				isCorr = false;
			}
		}
		catch(Exception e) {
			isCorr = false; //예외가 발생하면 확인이 안되기 때문에 false로 처리
			e.printStackTrace();
		}
		return isCorr;
	}
	//일련번호에 해당하는 게시물 삭제
	public int deletePost(String idx) {
		int result = 0;
		try {
			//쿼리문 작성
			String query = "DELETE FROM mvcboard WHERE idx=?";
			//prepared 객체 생성 및 인파라미터 설정
			psmt = con.prepareStatement(query);
			psmt.setString(1, idx);
			//쿼리실행.
			result = psmt.executeUpdate();
		}
		catch(Exception e) {
			System.out.println("게시물 삭제 중 예외 발생");
			e.printStackTrace();
		}
		return result;
	}
	//일련번호와 패스워드가 일치할때만 게시물 업데이트 처리
	public int updatePost(MVCBoardDTO dto) {
		int result = 0;
		try {
			String query = "UPDATE mvcboard"
						 + " SET title=?, name=?, content=?, ofile=?, sfile=? "
					 	 + " WHERE idx=? and pass=?";
			
			psmt = con.prepareStatement(query);
			psmt.setString(1, dto.getTitle());
			psmt.setString(2, dto.getName());
			psmt.setString(3, dto.getContent());
			psmt.setString(4, dto.getOfile());
			psmt.setString(5, dto.getSfile());
			psmt.setString(6, dto.getIdx());
			psmt.setString(7, dto.getPass());
			
			result = psmt.executeUpdate();
		}
		catch(Exception e) {
			System.out.println("게시물 수정 중 예외 발생");
			e.printStackTrace();
		}
		return result;
	}
}

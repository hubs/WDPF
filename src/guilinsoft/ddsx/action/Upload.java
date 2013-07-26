package guilinsoft.ddsx.action;


import guilinsoft.ddsx.core.MHttpServlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jzero.cache.C;
import com.jzero.util.MPrint;


public class Upload extends MHttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		begin(req);
		for(int i=0;i<10;i++){
			C.setCache("one_value_"+i, "one_"+i);
		}
		MPrint.print("内容上传");
		end();
	}
}

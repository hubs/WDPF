package guilinsoft.ddsx.action;

import guilinsoft.ddsx.core.MHttpServlet;
import guilinsoft.ddsx.util.MMsg;
import guilinsoft.ddsx.util.Tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jzero.cache.C;
import com.jzero.db.core.M;
import com.jzero.util.MCheck;


public class Taskselect extends MHttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		begin(req);
		String cacheKey=getDeviceid()+MMsg.INFO_REGETAD;
		Object regetad=C.getCacheObj(cacheKey);
		if(!MCheck.isNull(regetad)){//不为空,说明已执行Regetad,需要将所有文件下发
			resp.setIntHeader("taskFlag", 1);
			Tools.update_termstatetemp(getDeviceid(), 1);//1:在线,0:不在线
		}else{
			Object deviceObj=C.getCacheObj(getDeviceid());
			if(!MCheck.isNull(deviceObj)){
				int tasks=getTasks();
				if(tasks>0){
					resp.setIntHeader("taskFlag", 1);
				}else{
					resp.setIntHeader("taskFlag", 0);
				}
				Tools.update_termstatetemp(getDeviceid(), 1);//1:在线,0:不在线
			}else{
				timeOut(resp);
				Tools.update_termstatetemp(getDeviceid(), 0);//1:在线,0:不在线
			}
		}
		end();
	}
	//进行任务的选择
	private int getTasks(){
		/**
		 * 有新增,删除命令
		 */
		String sql="SELECT  cid  FROM "+MMsg.TB_PLAYLISTTASKITEM+"	WHERE  terminalid = '"+getDeviceid()+"'  AND (recvstate = '"+MMsg.ADD_COMMEND+"'  OR recvstate = '"+MMsg.DEL_COMMEND+"' OR recvstate='"+MMsg.TEMPLATE_COMMEND+"' )";
		return M.me().get_count_sql(sql);
	}
	//超时
	private void timeOut(HttpServletResponse resp) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream()));
			resp.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);//408 超时
			bw.write("");// //这里返回内容是body:"",如果省去这一段,会发出this http request
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
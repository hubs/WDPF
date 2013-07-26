package guilinsoft.ddsx.action;

import guilinsoft.ddsx.core.MHttpServlet;
import guilinsoft.ddsx.util.MMsg;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jzero.db.core.M;
import com.jzero.util.MCnt;
import com.jzero.util.MDate;
import com.jzero.util.MRecord;

public class Status extends MHttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		begin(req);
			//String taskid=req.getParameter("taskid");//任务ID,暂时没用到
			String cid=req.getParameter("cid");//取到的是playlisttaskitem的serialno(主键)
			String status=req.getParameter("status");
			/**
			 * status 01:下载成功
			 * 		  02:执行失败
			 * 		  03:任务失败
			 * 		  06:删除成功
			 */
			if(status.equals("01")||status.equals("1")){
				MRecord mRecord=M.me().one_t(MMsg.TB_PLAYLISTTASKITEM, MCnt.me().and_eq("serialno", cid).toStr());
				String type=mRecord.getStr("recvstate");//判断当前是资料下载还是模板下载
				if(type.equals(MMsg.TEMPLATE_COMMEND)){//如果是模板,则更新模板状态
					M.me().update(MMsg.TB_PLAYLISTTASKITEM, new MRecord().set("recvstate", MMsg.TEMPLATE_ED_COMMEND).set("recvtime", MDate.get_ymd_hms_join()), MCnt.me().first_eq("serialno", cid).toStr());
				}else if(type.equals(MMsg.ADD_COMMEND)){//更新状态为下载成功
					M.me().update(MMsg.TB_PLAYLISTTASKITEM, new MRecord().set("recvstate", MMsg.ADD_COMMENDED).set("recvtime", MDate.get_ymd_hms_join()), MCnt.me().first_eq("serialno", cid).toStr());
				}
			}else if(status.equals("06")||status.equals("6")){//更新状态为删除成功
				M.me().update(MMsg.TB_PLAYLISTTASKITEM, new MRecord().set("recvstate", MMsg.DEL_ED_COMMEND).set("recvtime", MDate.get_ymd_hms_join()), MCnt.me().first_eq("serialno", cid).toStr());
			}else if(status.equals("2")||status.equals("02")){
				//更新状态为任务失败,这样在Taskselect时就会过滤这条数据
				M.me().update(MMsg.TB_PLAYLISTTASKITEM, new MRecord().set("recvstate", MMsg.TAST_ERROR).set("recvtime", MDate.get_ymd_hms_join()), MCnt.me().first_eq("serialno", cid).toStr());
			}else if(status.equals("3")||status.equals("03")){
				//更新状态为任务取消
				M.me().update(MMsg.TB_PLAYLISTTASKITEM, new MRecord().set("recvstate",MMsg.TAST_CANEL).set("recvtime", MDate.get_ymd_hms_join()), MCnt.me().first_eq("serialno", cid).toStr());
			}
		end();
	}
}

package guilinsoft.ddsx.quartz;


import guilinsoft.ddsx.api.G3Manage;
import guilinsoft.ddsx.util.MMsg;
import guilinsoft.ddsx.util.Tools;

import java.util.List;
import java.util.Map.Entry;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jzero.db.core.M;
import com.jzero.util.MCheck;
import com.jzero.util.MCnt;
import com.jzero.util.MDate;
import com.jzero.util.MRecord;

public class MCron implements Job {
	/**
	 * 	到期开始的内容：从playtask表读取starttime等于当天的内容导入到playlisttaskitem中，并创建新的xml等待终端读取；
		过期终止的内容：从playtask表读取endtime等于当天的内容taskid->playlisttaskitem中，修改recstate=5，并创建新的xml等待终端读取。
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		insert_starttime();
		delete_endtime();
		
	}
	
	/**
	 * 将playlist当天的数据保存进playlistitem表中
	 * 创建XML
	 */
	private void insert_starttime() {
		String today=MDate.getSimpleDateFormat("yyyyMMdd").format(MDate.newDate());
		/**
		 * 2013-4-25:
		 * 1、将playlisttaskitem中未存在的数据查找出来(为了不重复查询)
		 */
		String check_not_in_playlistitem="SELECT  *  FROM "+MMsg.TB_PLAYTASK+" WHERE  starttime >= '"+today+"'  AND approvestatus = 'P'  AND STATUS = 'R' AND taskid NOT IN (SELECT taskid FROM "+MMsg.TB_PLAYLISTTASKITEM+" )";
		List<MRecord> not_in_playlistLst=M.me().sql(check_not_in_playlistitem);
//		List<MRecord> lst=M.me().get_where_n(MMsg.TB_PLAYTASK, MCnt.me().first("starttime",MEnum.GT_E, today).and_eq("approvestatus", "P").and_eq("status", "R").toStr());
		if(!MCheck.isNull(not_in_playlistLst)){
			//1、插入到playlistitem表中
			//2、生成XML
			MRecord distinctGroup=new MRecord();
			for(MRecord row:not_in_playlistLst){
				distinctGroup.set(row.getStr("groupid"), row.getStr("groupid"));
				//查找出当前群组下的所有终端
				String sql="select t.deviceid from "+MMsg.TB_TERMINAL+" t,"+MMsg.TB_TERMGROUP+" g where g.locationid=t.location and g.groupid='"+row.getStr("groupid")+"'";
				List<MRecord> terminalLst=M.me().sql(sql);
				if(!MCheck.isNull(terminalLst)){
					//查找到资料信息
					MRecord contentMR=M.me().one_t_n(MMsg.TB_CONTENT, MCnt.me().and_eq("contentid", row.getStr("contentid")).toStr());
					//一些统一信息
					MRecord mRecord=new MRecord().set("cid", row.getStr("tasklevel")).set("cname",getFileName(contentMR.getStr("path")))
						.set("csize", contentMR.getStr("timelength"))//这个size不算了随便一个值
						.set("path", contentMR.getStr("path"))
						.set("recvstate", "0")
						.set("taskid", row.getStr("taskid"))
						.set("url", contentMR.getStr("path"))
						.set("type", contentMR.getStr("type"));	
					for(MRecord terminal:terminalLst){
						//2013-4-25:因为前面已经过滤掉了playlisttaskitem中存在的数据,所以在此不用再进行查询判断
//						String where=MCnt.me().and_eq("taskid", row.getStr("taskid")).and_eq("terminalid", terminal.getStr("deviceid")).and_eq("path", contentMR.getStr("path")).toStr();
//						boolean isExist=M.me().check(MMsg.TB_PLAYLISTTASKITEM, where);
//						if(!isExist){
							mRecord.set("serialno", Tools.generate_id()).set("terminalid", terminal.getStr("deviceid"));//终端号
							M.me().insert(MMsg.TB_PLAYLISTTASKITEM, mRecord);
//						}
					}
				}
			}
			for(Entry<String, Object> entry: distinctGroup.entrySet()) {
				 G3Manage.me().releasetask(entry.getKey());
			}
		}
	}
 	private String getFileName(String path){
		return path.substring(path.lastIndexOf("/")+1); 
	}
	
	/**
	 * endtime 超过当天的,则将playlistitem表中的状态改为5
	 */
	private void delete_endtime() {
		String today=MDate.getSimpleDateFormat("yyyyMMdd").format(MDate.newDate());
		String playtasklistitem_sql="select recvstate,serialno from "+MMsg.TB_PLAYLISTTASKITEM+" where taskid in (select  taskid  from "+MMsg.TB_PLAYTASK+"	where  approvestatus = 'p' and ( endtime < '"+today+"'  or status = 'C') )";
		List<MRecord> lst=M.me().sql(playtasklistitem_sql);
		/**
		 * 1、根据taskid修改playlistitem中的接收状态,改为5(删除)
		 * 2、重新生成xml文件
		 */
		if(!MCheck.isNull(lst)){
			MRecord in=new MRecord();
			for(MRecord terminal:lst){
				String recvstate=terminal.getStr("recvstate");
				if("1".equals(recvstate)){//已经下载下去,将命令改成5
					in.set("recvstate", "5");
				}else if("0".equals(recvstate)){//还未下载下去,则直接变更为3
					in.set("recvstate", "3");
				}
				String where=MCnt.me().and_eq("serialno", terminal.getStr("serialno")).toStr();
				M.me().update(MMsg.TB_PLAYLISTTASKITEM,in, where);
			}
			//生成XML文件
			String playtask_sql="select  distinct groupid  from "+MMsg.TB_PLAYTASK+"	where  approvestatus = 'p' and ( endtime < '"+today+"'  or status = 'C')  ";
			List<MRecord> distinctGroup=M.me().sql(playtask_sql);
			for(MRecord groupid:distinctGroup){
				G3Manage.me().releasetask(groupid.getStr("groupid"));
			}
		}
	}
}

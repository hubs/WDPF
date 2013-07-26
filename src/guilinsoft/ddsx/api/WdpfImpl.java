package guilinsoft.ddsx.api;

import guilinsoft.ddsx.util.BuilderXml;
import guilinsoft.ddsx.util.MMsg;
import guilinsoft.ddsx.util.Tools;

import java.util.List;
import java.util.Map;

import com.jzero.cache.C;
import com.jzero.cache.Cache;
import com.jzero.db.core.M;
import com.jzero.util.MCheck;
import com.jzero.util.MCnt;
import com.jzero.util.MPrint;
import com.jzero.util.MRecord;

public class WdpfImpl implements G3API {
	/**
	 * 根据群组ID,生成当前群组所拥有的资源,在这里生成image.xml,movie.xml,text.xml
	 */
	public void releasetask(String groupId) {
		MPrint.print("远程执行:releasetask 方法");
		MRecord groupRe=M.me().one_t(MMsg.TB_TERMGROUP,MCnt.me().and_eq("groupid", groupId).toStr());
		if(!MCheck.isNull(groupRe)){
			generateXML(groupRe, groupId);
		}
	}
	
	private void generateXML(MRecord groupRe,String groupId){
		//image.xml
		List<MRecord> imageLst=M.me().sql(getXmlSql(groupId, "P"));
		if(!MCheck.isNull(imageLst)){
			BuilderXml.me().xml_image(groupRe, imageLst);
		}
		//text.xml
		List<MRecord> textLst=M.me().sql(getXmlSql(groupId, "W"));
		if(!MCheck.isNull(textLst)){
			BuilderXml.me().xml_text(groupRe, textLst);
		}
		
		//movie.xml
		List<MRecord> movieLst=M.me().sql(getXmlSql(groupId, "V"));
		if(!MCheck.isNull(movieLst)){
			BuilderXml.me().xml_movie(groupRe, movieLst);	
		}
		
		//scrollingtext.xml
		List<MRecord> scrollLst=M.me().sql(getXmlSql(groupId, "S"));
		if(!MCheck.isNull(scrollLst)){
			BuilderXml.me().xml_scrollingtext(groupRe, scrollLst);	
		}
	}
	
	private String getXmlSql(String groupid,String type){
		return "SELECT DISTINCT cname,path FROM playlisttaskitem WHERE EXISTS (SELECT taskid FROM playtask WHERE approvestatus='P' AND groupid='"+groupid+"' AND  endtime >=DATE_FORMAT(NOW(),'%Y%m%d')) AND TYPE='"+type+"' and recvstate!=3";
	}
	/**
	 * 生成布局模板
	 */
	public boolean saveTemplateToXML(String templateid) {
		MPrint.print("远程执行:saveTemplateToXML 方法");
		MRecord temlate=M.me().one_t(MMsg.TB_TEMPLATE, MCnt.me().and_eq("templateid", templateid).toStr());			//主模板
		List<MRecord> sub_template=M.me().get_where("subtemplate",MCnt.me().first_eq("templateid", templateid).toStr());//子模板
		BuilderXml.me().xml_program(temlate,sub_template);
		return true;
	}
	/**
	 * 群组下广告内容同步到终端  synchroGroupTaskToTerminal
	 * 生成XML
	 */
	public void importad(String groupId, String terminalid) {
		MPrint.print("远程执行:importad 方法");
		MRecord groupRe=M.me().one_t(MMsg.TB_TERMGROUP,MCnt.me().and_eq("groupid", groupId).and_eq("templateid", terminalid).toStr());
		generateXML(groupRe, groupId);
	}	
	/**
	 * 终端是否在线 ,返回记录的时间ymd his
	 * shortnum:设备码,返回最后更新时间
	 */
	public String getselecttime(String deviceId) {
		Object device=C.getCacheObj(deviceId);
		String reStr=null;
		if(!MCheck.isNull(device)){
			MRecord mRecord=(MRecord) device;
			reStr=mRecord.getStr(MMsg.INFO_UPDATE_TIME);//取更新时间
		}
		return reStr;
	}

	/**
	 * 按地市统计其下终端的连接壮态 SAByLocation2
	 * 在这里更新: termstatetemp表,state:0:不在线,1:在线
	 */
	public void orderbystate() {
		Map<String,Cache> mRecord=C.get();
		for (Map.Entry<String, Cache> entry : mRecord.entrySet()) {
			String k = entry.getKey();
			if (k.equals(MMsg.INFO_DEVICEID)) {
				Tools.update_termstatetemp(k, 1);
			}
		}
	}	
	/**
	 * 修改模板,使用在变更模板的时候　
	 */
	public void changeTemplate(String groupid) {
		/**
		 *  在调用此方法之前调用　save_xml(创建主模板)
		 *  1、根据groupid 从termgroup取出LOCATIONID(区域ID)
		 *  2、根据locationid(区域ID)从terminal表中取出所有终端(deviceid)
		 *  3、将deviceid等数据存入playlisttemp中
		 */
		MRecord groupMr=M.me().one_t(MMsg.TB_TERMGROUP,MCnt.me().and_eq("groupid", groupid).toStr());
		MRecord resourceMR=M.me().one_t(MMsg.TB_RESOURCE, MCnt.me().and_eq("ftype", "T").and_eq("templateid", groupMr.getStr("templateid")).toStr());
		List<MRecord> terminalLst=M.me().get_where(MMsg.TB_TERMINAL, MCnt.me().first_eq("location", groupMr.getStr("locationid")).toStr());
		if(!MCheck.isNull(terminalLst)){
			String path=resourceMR.getStr("path")+"/program.xml";
			String imgpath=resourceMR.getStr("path")+"/"+resourceMR.getStr("fimg");
			/**
			 * 在这里,我将taskid保存 图片的名称
			 * 			将url保存图片的地址
			 */
			MRecord playlistMr=new MRecord().set("cname", "program.xml").set("recvstate", MMsg.TEMPLATE_COMMEND).set("type", "T").set("path",path).set("url", imgpath).set("taskid", resourceMR.getStr("fimg"));
			String where=null;
			for(MRecord row:terminalLst){
				//判断表中是否有数据,有则不插入
				where=MCnt.me().and_eq("cname", "program.xml").and_eq("path", path).and_eq("terminalid", row.getStr("deviceid")).and_eq("recvstate",  MMsg.TEMPLATE_COMMEND).toStr();
				boolean isExist=M.me().check(MMsg.TB_PLAYLISTTASKITEM, where);
				if(!isExist){//不存在则插入
					playlistMr.set("serialno", Tools.generate_id()).set("terminalid", row.getStr("deviceid")).set("cid", "0").set("csize", 1);	
					M.me().insert(MMsg.TB_PLAYLISTTASKITEM, playlistMr);
				}
			}
		}
		
		//2013-4-11:只需要下发主模板,program.xml,其它文件在有新资源时会自动生成.
		/**
		 *
		//生成子模板
		List<MRecord> subTemplate=M.me().get_where(MMsg.TB_SUBTEMPLATE, MCnt.me().first_eq("templateid", groupMr.getStr("templateid")).toStr());
		if(!MCheck.isNull(subTemplate)){
			for(MRecord row:subTemplate){
				build_subtemplate(groupMr,row);
			}
		}
		**/
	}
	/**
	//读取文件类型
	private void build_subtemplate(MRecord groupRe,MRecord record){
		String type=record.getStr("type");
		if(type.equalsIgnoreCase("V")){				//视频
			BuilderXml.me().xml_movie(groupRe, null);	
		}else if(type.equalsIgnoreCase("P")){		//图片
			BuilderXml.me().xml_image(groupRe, null);
		}else if(type.equalsIgnoreCase("W")){		//文本
			BuilderXml.me().xml_text(groupRe, null);
		}else if(type.equalsIgnoreCase("S")){		//滚动文本
			BuilderXml.me().xml_scrollingtext(groupRe, null);
		}
	}**/
	public static void main(String[] args) {
		WdpfImpl impl=new WdpfImpl();
		String groupid="03";
		impl.changeTemplate(groupid);
	}
	
	/*********************暂时还未用到***************************/
	

	public String crossValidation(String groupid, String playlistid,
			String startDate, String endDate) {
		MPrint.print("调用     crossValidation 方法");
		return "OK";
	}



	public void release(String playlistId) {
		MPrint.print("调用    release 方法");	
		Map<String,Cache> mRecord=C.get();
		for (Map.Entry<String, Cache> entry : mRecord.entrySet()) {
			String k = entry.getKey();
			MPrint.print("name = "+k+", and value = "+entry.getValue().getValue());
		}
	}

	public boolean timeLineReleasetask(String groupid) {
		MPrint.print("调用    timeLineReleasetask 方法");
		return false;
	}


	
	
}

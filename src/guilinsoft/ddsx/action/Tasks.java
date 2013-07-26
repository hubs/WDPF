package guilinsoft.ddsx.action;
import guilinsoft.ddsx.core.MHttpServlet;
import guilinsoft.ddsx.util.MMsg;
import guilinsoft.ddsx.util.Tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jzero.cache.C;
import com.jzero.db.core.M;
import com.jzero.log.Log;
import com.jzero.util.MCheck;
import com.jzero.util.MMD5;
import com.jzero.util.MPrint;
import com.jzero.util.MRecord;


public class Tasks extends MHttpServlet {
	private static final long serialVersionUID = 1L;
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		begin(req);
		String cacheKey=getDeviceid()+MMsg.INFO_REGETAD;
		Object regetad=C.getCacheObj(cacheKey);
		if(!MCheck.isNull(regetad)){
			replayAll(resp);//格式化,下发所有数据
			C.setCache(null, cacheKey);
			MPrint.print("------格式化操作.");
		}else{
			replay(resp);//针对性下发.
			MPrint.print("-----非格式化");
		}
		end();
	}
	//下发所有资源文件与XML
	private void replayAll(HttpServletResponse resp) {
		try{
			//xml资源文件
			String xml_sql="SELECT path,name,fimg FROM "+MMsg.TB_RESOURCE+" m,(SELECT g.groupid,g.templateid FROM "+MMsg.TB_TERMINAL+" t,"+MMsg.TB_TERMGROUP+" g WHERE t.location=g.locationid AND  deviceid='"+getDeviceid()+"') b WHERE m.templateid=b.templateid AND ( m.groupid=b.groupid or m.fimg is not null)";
			//文件资源文件(因为模板 (T)没有terminalid ,所以检索不出)
			String resource_sql="SELECT  distinct serialno,taskid ,cname,recvstate,csize,url  FROM "+MMsg.TB_PLAYLISTTASKITEM+"	WHERE    terminalid = '"+getDeviceid()+"'  AND (recvstate = '"+MMsg.ADD_COMMEND+"'  OR recvstate='"+MMsg.ADD_COMMENDED+"')";
			List<MRecord> xml_lst=M.me().sql(xml_sql);
			List<MRecord> resource_lst=M.me().sql(resource_sql);
			int count=xml_lst.size()+resource_lst.size()+1;
			//XML 内容
			StringBuilder body=new StringBuilder();
			body.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
			   	.append("<tasklist>")
				   .append("<taskcount>"+count+"</taskcount>")
				   .append("<taskitem><tasktype>06</tasktype>")
				   .append("<taskid>"+Tools.generate_id()+"</taskid>")
				   .append("<servicecode>901</servicecode>")
				   .append("<expire>20991230093129</expire>")
				   .append("<contents>");
					//循环xml 资源文件
					if(!MCheck.isNull(xml_lst)){
						for(MRecord xml:xml_lst){
							body.append("<content>")
								.append("<cid>"+Tools.generate_id()+"</cid>")//因为是下载所有数据,所以有些模板文件不存在数据库中,所以这里不对cid进行处理
								.append("<cname>"+xml.getStr("name")+"</cname>")
								.append("<cmd>0</cmd>")//0:下载, 1:删除
								.append("<ctype>2</ctype>")//mpl
								.append("<link>"+Tools.getBase()+xml.getStr("path")+"/"+xml.getStr("name")+"</link>")
								.append("<csize>200</csize>")
								.append("</content>");
							if(!MCheck.isNull(xml.getStr("fimg"))){//不為空,說明是主模板的圖片
								body.append("<content>")
									.append("<cid>"+Tools.generate_id()+"</cid>")
									.append("<cname>"+xml.getStr("fimg")+"</cname>")
									.append("<cmd>0</cmd>")//0:下载, 1:删除
									.append("<ctype>1</ctype>")//img
									.append("<link>"+Tools.getBase()+xml.getStr("path")+"/"+xml.getStr("fimg")+"</link>")
									.append("<csize>200</csize>")
									.append("</content>");
							}
						}
					}
					//循环显示文件
					if(!MCheck.isNull(resource_lst)){
						for(MRecord row:resource_lst){
							String name=row.getStr("cname");
								body.append("<content>")
									.append("<cid>"+row.getStr("serialno")+"</cid>")
									.append("<cname>"+name+"</cname>");//video/"+name+"
									if(row.getInt("recvstate")==5){//0:新增,5:删除
										body.append("<cmd>1</cmd>"); //0:下载, 1:删除
									}else{
										body.append("<cmd>0</cmd>");
									}
								body.append("<ctype>1</ctype>")//"+ext+"
									.append("<csize>"+row.getStr("csize")+"</csize>")
									.append("<link>"+Tools.getTargetBase()+"/"+row.getStr("url")+"</link>")
									.append("</content>");
						}					
					}
					body.append("</contents>");
				body.append("</taskitem>");
			body.append("</tasklist>");
			String au = "qop=\"auth-int\",nextnonce=\"" + MMD5.toMD5ByJAVA(System.currentTimeMillis()+"")+ "\",rspauth=\"" + MMD5.toMD5ByJAVA(System.nanoTime()+"") + "\" nc="+getNc()+" cnonce="+getCnonce();
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setHeader("Authentication-Info", au);
			BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(resp.getOutputStream()));
			bw.write("");
			bw.write(body.toString());
			bw.flush();
			bw.close();
			MPrint.print("XML:"+body.toString());
			Log.me().write("XML:"+body.toString());
			//2013-7-26:交给status类处理
//			if(!MCheck.isNull(resource_lst)){
//				M.me().update(MMsg.TB_PLAYLISTTASKITEM, new MRecord().set("recvstate", 1).set("recvtime", MDate.get_ymd_hms_join()), MCnt.me().first_eq("terminalid", getDeviceid()).and_eq("recvstate", MMsg.ADD_COMMEND).toStr());
//				M.me().update(MMsg.TB_PLAYLISTTASKITEM, new MRecord().set("recvstate", 3).set("recvtime", MDate.get_ymd_hms_join()), MCnt.me().first_eq("terminalid", getDeviceid()).and_eq("recvstate", MMsg.DEL_COMMEND).toStr());
//			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * ctype ==1 ======> vcache  资源文件
	   ctype ==2  ======> cache XML
	   ctype ==其它 ====> web 
	 */
	private Map<String,String> getTaskItem(){
		Map<String,String> xmlMap=new HashMap<String, String>();
		StringBuilder body=new StringBuilder();
		body.append("<taskitem><tasktype>06</tasktype>")
				.append("<taskid>"+Tools.generate_id()+"</taskid>")
				.append("<servicecode>901</servicecode>")
				.append("<expire>20991230093129</expire>")
				.append("<contents>");
			//根据终端取出群组与模板,再从m_resource中取出它的xml文件路径
		/**
		 * 1、取出所有增加与删除指令
		 * 2、取出未下载的模板指令
		 */
//		String sql="SELECT path,name,fimg,ftype FROM m_resource m,(SELECT g.groupid,g.templateid FROM terminal t,termgroup g WHERE t.location=g.locationid AND  deviceid='"+getDeviceid()+"') b WHERE m.templateid=b.templateid AND  m.ftype IN (SELECT TYPE FROM playlisttaskitem WHERE (terminalid ='"+getDeviceid()+"' AND (recvstate = '"+MMsg.ADD_COMMEND+"'  OR recvstate = '"+MMsg.DEL_COMMEND+"'  )) OR  (m.groupid is null and terminalid in ( SELECT g.templateid FROM terminal t,termgroup g WHERE t.location = g.locationid AND deviceid = '"+getDeviceid()+"')))";
		String sql="SELECT  path,name,fimg,ftype FROM m_resource m,(SELECT g.groupid,g.templateid FROM terminal t,termgroup g WHERE t.location=g.locationid AND  deviceid='"+getDeviceid()+"') b WHERE m.templateid=b.templateid AND  m.groupid=b.groupid AND  m.ftype IN (SELECT distinct type FROM playlisttaskitem WHERE (terminalid ='"+getDeviceid()+"' AND (recvstate = '"+MMsg.ADD_COMMEND+"'  OR recvstate = '"+MMsg.DEL_COMMEND+"'  )))";
		List<MRecord> xmlLst=M.me().sql(sql);
		int size=0;
		if(!MCheck.isNull(xmlLst)){
				for(MRecord xml:xmlLst){
					String name=xml.getStr("name");
					if("program.xml".equals(name)){//如果是主模板,因为它带有图片,所以需要+1;
						size=1;
					}
					body.append("<content>")
						.append("<cid>"+Tools.generate_id()+"</cid>")
						.append("<cname>"+xml.getStr("name")+"</cname>")
						.append("<cmd>0</cmd>")//0:下载, 1:删除
						.append("<ctype>2</ctype>")//mpl
						.append("<link>"+Tools.getBase()+xml.getStr("path")+"/"+xml.getStr("name")+"</link>")
						.append("<csize>200</csize>")
						.append("</content>");
				}
				size+=xmlLst.size();
			xmlMap.put("size", size+"");
		}
		xmlMap.put("body", body.toString());
		return xmlMap;
	}
	/**
	 * 视频文件进行断点续传
	 * @param resp
	 */
	private void replay(HttpServletResponse resp){
		try{
			//模板更换后需要下载的资源
//			List<MRecord> template_lst=M.me().get_data("", table, where, orderStr).get_where(MMsg.TB_PLAYLISTTASKITEM, MCnt.me().first_eq("type", "T").and_eq("recvstate", MMsg.TEMPLATE_COMMEND).and_eq("terminalid", getDeviceid()).toStr());
			List<MRecord> template_lst=M.me().sql("SELECT distinct serialno,path,url,taskid FROM playlisttaskitem WHERE TYPE='T' AND recvstate='"+MMsg.TEMPLATE_COMMEND+"' AND terminalid='"+getDeviceid()+"'");
			//未下载的资源文件 taskid ,
			String sql="SELECT  distinct serialno,cname,recvstate,csize,url  FROM "+MMsg.TB_PLAYLISTTASKITEM+"	WHERE  terminalid = '"+getDeviceid()+"'  AND (recvstate = '"+MMsg.ADD_COMMEND+"'  OR recvstate = '"+MMsg.DEL_COMMEND+"')";
			List<MRecord> lst=M.me().sql(sql);
			Map<String,String> xmlMap=getTaskItem();//有问题
			int count=lst.size()+Integer.parseInt(xmlMap.get("size"));//资源文件+xml文件
			if(!MCheck.isNull(template_lst)){//当更换模板后
				count=count+2;
			}
			StringBuilder sb=new StringBuilder();
						  sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
						   	.append("<tasklist>")
							   .append("<taskcount>"+count+"</taskcount>")
				   			   .append(xmlMap.get("body"));
						  		if(!MCheck.isNull(template_lst)){
						  		//循环更换模板后的资源
									for(MRecord row:template_lst){
										/**
										 * 如果是每个终端对应一个模板,则可以选择  ftype="T" and recevid=7(未接收) TEST <a>
										 */
	//									if(row.getStr("type").equals("T")){//不為空,說明是主模板的圖片
												  sb.append("<content>")
													.append("<cid>"+row.getStr("serialno")+"</cid>")
													.append("<cname>program.xml</cname>")
													.append("<cmd>0</cmd>")//0:下载, 1:删除
													.append("<ctype>2</ctype>")//mpl
													.append("<link>"+Tools.getBase()+row.getStr("path")+"</link>")
													.append("<csize>200</csize>")
													.append("</content>");
												if(!MCheck.isNull(row.getStr("url"))){//不為空,說明是主模板的圖片
													  sb.append("<content>")
														.append("<cid>"+Tools.generate_id()+"</cid>")
														.append("<cname>"+row.getStr("taskid")+"</cname>")//哈哈,这里看不懂taskid了吧,我在changeTemplate时将taskid保存着图片的名称,将url保存着图片的路径
														.append("<cmd>0</cmd>")//0:下载, 1:删除
														.append("<ctype>1</ctype>")//img
														.append("<link>"+Tools.getBase()+row.getStr("url")+"</link>")
														.append("<csize>200</csize>")
														.append("</content>");
												}
									}
						  		}
								//		}else{
								//Foreach循环资源
						  		if(!MCheck.isNull(lst)){
									for(MRecord row:lst){
											  sb.append("<content>")
												.append("<cid>"+row.getStr("serialno")+"</cid>")
												.append("<cname>"+row.getStr("cname")+"</cname>");//video/"+name+"
												if(row.getInt("recvstate")==5){//0:新增,5:删除
													sb.append("<cmd>1</cmd>"); //0:下载, 1:删除
												}else{
													sb.append("<cmd>0</cmd>");
												}
											 sb.append("<ctype>1</ctype>")//"+ext+"
												.append("<csize>"+row.getStr("csize")+"</csize>");
	//											if(row.getStr("type").equalsIgnoreCase("V")){//如果是视频文件,则要求续传
	//												sb.append("<link>"+Tools.getBase()+"/content?serialno="+row.getStr("serialno")+"</link>");
	//											}else{
													sb.append("<link>"+Tools.getTargetBase()+"/"+row.getStr("url")+"</link>");
	//											}
												
											 sb.append("</content>");
										}
						  		}
								sb.append("</contents>");
								sb.append("</taskitem>");
							sb.append("</tasklist>");
			String au = "qop=\"auth-int\",nextnonce=\"" + MMD5.toMD5ByJAVA(System.currentTimeMillis()+"")+ "\",rspauth=\"" + MMD5.toMD5ByJAVA(System.nanoTime()+"") + "\" nc="+getNc()+" cnonce="+getCnonce();
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setHeader("Authentication-Info", au);
			BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(resp.getOutputStream()));
			bw.write("");
			bw.write(sb.toString());
			bw.flush();
			bw.close();
			MPrint.print("XML:"+sb.toString());
			Log.me().write("XML:"+sb.toString());
//			if(!MCheck.isNull(lst)){
//				M.me().update(MMsg.TB_PLAYLISTTASKITEM, new MRecord().set("recvstate", MMsg.ADD_COMMENDED).set("recvtime", MDate.get_ymd_hms_join()), MCnt.me().first_eq("terminalid", getDeviceid()).and_eq("recvstate", MMsg.ADD_COMMEND).toStr());
//				M.me().update(MMsg.TB_PLAYLISTTASKITEM, new MRecord().set("recvstate", MMsg.DEL_ED_COMMEND).set("recvtime", MDate.get_ymd_hms_join()), MCnt.me().first_eq("terminalid", getDeviceid()).and_eq("recvstate", MMsg.DEL_COMMEND).toStr());
//			}	
//			if(!MCheck.isNull(template_lst)){
//				M.me().update(MMsg.TB_PLAYLISTTASKITEM, new MRecord().set("recvstate", MMsg.TEMPLATE_ED_COMMEND).set("recvtime", MDate.get_ymd_hms_join()), MCnt.me().first_eq("terminalid", getDeviceid()).and_eq("recvstate", MMsg.TEMPLATE_COMMEND).toStr());
//			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}

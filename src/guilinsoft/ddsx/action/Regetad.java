package guilinsoft.ddsx.action;


import guilinsoft.ddsx.core.MHttpServlet;
import guilinsoft.ddsx.util.MMsg;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jzero.cache.C;
import com.jzero.util.MPrint;

/**
 *  初始化 U盘
 */
public class Regetad extends MHttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		begin(req);
//		build_xml();
//		build_other();
		//2013-2-19 格式化,在tasks里判断,如果为true,则下载全部,否则只针对下载
		String cacheKey=getDeviceid()+MMsg.INFO_REGETAD;
		C.setCache(true,cacheKey);
		MPrint.print("格式化U盘!");
		end();
	}
	
//	private void build_xml(){
//		MPrint.print("生成主模板.");
//		String templateid="74beca8fa770e5d6ca98e3d1b380c209";
//		MRecord temlate=M.me().one_t(MMsg.TB_TEMPLATE, MCnt.me().and_eq("templateid", templateid).toStr());			//主模板
//		List<MRecord> sub_template=M.me().get_where("subtemplate",MCnt.me().first_eq("templateid", templateid).toStr());//子模板
//		BuilderXml.me().xml_program(temlate,sub_template);
//	}
//	private void build_other(){
//		MPrint.print("生成其它模板.");
//		String groupId="B46601BA0D53D5BE69CF9702C60F3BD0";
//		MRecord groupRe=M.me().one_t(MMsg.TB_TERMGROUP,MCnt.me().and_eq("groupid", groupId).toStr());
//		//image.xml
//		List<MRecord> imageLst=M.me().sql(getXmlSql(groupId, "P"));
//		if(!MCheck.isNull(imageLst)){
//			BuilderXml.me().xml_image(groupRe, imageLst);
//		}
//		
//		//text.xml
//		List<MRecord> textLst=M.me().sql(getXmlSql(groupId, "W"));
//		if(!MCheck.isNull(textLst)){
//			BuilderXml.me().xml_text(groupRe, textLst);
//		}
//		
//		//movie.xml
//		List<MRecord> movieLst=M.me().sql(getXmlSql(groupId, "V"));
//		if(!MCheck.isNull(movieLst)){
//			BuilderXml.me().xml_movie(groupRe, movieLst);	
//		}
//		
//		//scrollingtext.xml
//		List<MRecord> scrollLst=M.me().sql(getXmlSql(groupId, "S"));
//		if(!MCheck.isNull(scrollLst)){
//			BuilderXml.me().xml_scrollingtext(groupRe, scrollLst);	
//		}
//	}
//	private String getXmlSql(String groupid,String type){
//		return "SELECT distinct cname,path FROM playlisttaskitem WHERE EXISTS (SELECT taskid FROM playtask WHERE approvestatus='P' AND groupid='"+groupid+"' AND  endtime >NOW()) AND TYPE='"+type+"'";
//	}
}

package guilinsoft.ddsx.util;

import java.io.FileWriter;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.jzero.db.cache.MFile;
import com.jzero.db.core.M;
import com.jzero.upload.MDown;
import com.jzero.util.MCheck;
import com.jzero.util.MCnt;
import com.jzero.util.MPath;
import com.jzero.util.MRecord;
import com.jzero.util.MTool;

/**
 * 成生XML模板
 */
public class BuilderXml {
	private BuilderXml() {
	}

	private static BuilderXml xml = new BuilderXml();

	public static BuilderXml me() {
		return xml;
	}
	/***********************************************************布局文件XML开始******************************************************/
	/**
	 * 布局文件,主在项目
	 * @param subTemplate 
	 */
	public synchronized void xml_program(MRecord row, List<MRecord> subTemplate) {
		Document doc=DocumentHelper.createDocument();
	
		Element root=doc.addElement("program").addAttribute("id", Tools.generate_id()).addAttribute("issue", "G3media").addAttribute("templateid", row.getStr("templateid")).addAttribute("starttime", "20100101").addAttribute("endtime", "20991010");
		Element view=root.addElement("view").addAttribute("background",getFileName(row.getStr("path"))).addAttribute("width", row.getStr("width")).addAttribute("height", row.getStr("height"));
		Element viewport=null;//"bg.png"
		String type_name = null;
		String xywh[]=null;
		if(!MCheck.isNull(subTemplate)){
			for(MRecord record:subTemplate){
				type_name=get_type_name(record);//读取文件类型
				xywh=get_XYWH(record.getStr("position"));////读取坐标
				viewport=view.addElement("viewport").addAttribute("id", Tools.generate_id()).addAttribute("type", type_name).addAttribute("mpl", record.getStr("mplname"));
			 	viewport.addElement("layout").addAttribute("x", MTool.get(xywh, 0)).addAttribute("y", MTool.get(xywh, 1)).addAttribute("width", MTool.get(xywh, 2)).addAttribute("height", MTool.get(xywh, 3));
			}
		}
		save_root_xml(row,doc);
	}
	/**
	 * 保存布局文件
	 * @throws Exception 
	 */
	private void save_root_xml(MRecord tempRe,Document doc){
		//保存格式: xml/templateid/..xml
		String tempateid=tempRe.getStr("templateid");
		String path="/xml/"+tempateid;
		String filename ="program.xml";
		String full_path=MFile.createFile(MPath.me().getWebRoot()+path,"program.xml").getPath();
		builder(full_path,doc);//寫入XML
		
		String imgUrl = Tools.getTargetBase()+"/"+tempRe.getStr("path");
		String imagename =getFileName(imgUrl);
		full_path=MFile.createFile(MPath.me().getWebRoot()+path,imagename).getPath();
		try {
			MDown.saveUrlAs(imgUrl,full_path);
		} catch (Exception e) {
			e.printStackTrace();
		}//保存圖片
		
		//保存到表中
		MRecord inData=new MRecord().set("templateid", tempateid).set("path", path).set("ftype", "T").set("name", filename).set("fimg", imagename);
		String where=MCnt.me().and_eq("templateid", tempateid).and_eq("name",filename ).and_eq("ftype", "T").and_eq("fimg",imagename).toStr();
		insert(inData,where);
	}
	//读取坐标
	private String[] get_XYWH(String position){
		return position.split(",");
	}
	
	//读取文件类型
	private String get_type_name(MRecord record){
		String type_name = null;
		String type=record.getStr("type");
		if(type.equalsIgnoreCase("V")){				//视频
			type_name="video";
		}else if(type.equalsIgnoreCase("P")){		//图片
			type_name="image";
		}else if(type.equalsIgnoreCase("W")){		//文本
			type_name="text";
		}else if(type.equalsIgnoreCase("S")){		//滚动文本
			type_name="scrollingtext";	
		}
		return type_name;
	}
	/***********************************************************布局文件XML结束******************************************************/
	
	
	/***********************************************************其它XML开始**********************************************************/
	/**
	 * 图片xml
	 */
	public synchronized void xml_image(MRecord groupRe,List<MRecord> imageLst) {
		Document doc=DocumentHelper.createDocument();
		
		Element root=doc.addElement("mpl").addAttribute("id", Tools.generate_id()).addAttribute("type", "image");
		Element seq=root.addElement("seq").addAttribute("id", Tools.generate_id()).addAttribute("starttime", "0000").addAttribute("endtime", "2359");
		if(!MCheck.isNull(imageLst)){
			for(MRecord row:imageLst){
				seq.addElement("media").addAttribute("id", Tools.generate_id()).addAttribute("name", row.getStr("cname")).addAttribute("src", getFileName(row.getStr("path")));	
			}
		}
		save_xml(groupRe,"image.xml",doc);
	}		

	/**
	 * 静态文字
	 */
	public synchronized void xml_text(MRecord groupRe,List<MRecord> textLst) {
		Document doc=DocumentHelper.createDocument();
		
		Element root=doc.addElement("mpl").addAttribute("id", Tools.generate_id()).addAttribute("type", "text");
		Element seq=root.addElement("seq").addAttribute("id", Tools.generate_id()).addAttribute("starttime", "0000").addAttribute("endtime", "2359");
		if(!MCheck.isNull(textLst)){
			for(MRecord row:textLst){
				seq.addElement("media").addAttribute("id", Tools.generate_id()).addAttribute("name", row.getStr("cname")).addAttribute("src", getFileName(row.getStr("path")));	
			}
		}
		save_xml(groupRe,"text.xml",doc);
	}	
	
	/**
	 * 滚动文字
	 */
	public synchronized void xml_scrollingtext(MRecord groupRe,List<MRecord> scrollLst) {
		Document doc=DocumentHelper.createDocument();
		
		Element root=doc.addElement("mpl").addAttribute("id", Tools.generate_id()).addAttribute("type", "scrollingtext");
		Element seq=root.addElement("seq").addAttribute("id", Tools.generate_id()).addAttribute("starttime", "0000").addAttribute("endtime", "2359");
		if(!MCheck.isNull(scrollLst)){
			for(MRecord row:scrollLst){
				seq.addElement("media").addAttribute("id", Tools.generate_id()).addAttribute("name", row.getStr("cname")).addAttribute("src", getFileName(row.getStr("path")));	
			}
		}
		save_xml(groupRe,"scrollingtext.xml",doc);
	}	
	
	/**
	 * 视频xml
	 */
	public synchronized void xml_movie(MRecord groupRe,List<MRecord> movieLst) {
		Document doc=DocumentHelper.createDocument();
		
		Element root=doc.addElement("mpl").addAttribute("id", Tools.generate_id()).addAttribute("type", "video");
		Element seq=root.addElement("seq").addAttribute("id", Tools.generate_id()).addAttribute("starttime", "0000").addAttribute("endtime", "2359");
		if(!MCheck.isNull(movieLst)){
			for(MRecord row:movieLst){
				seq.addElement("media").addAttribute("id", Tools.generate_id()).addAttribute("name", row.getStr("cname")).addAttribute("src", getFileName(row.getStr("path")));	
			}
		}
		save_xml(groupRe,"movie.xml",doc);		
	}
	/***********************************************************其它XML结束**********************************************************/
	
	
	/***********************************************************帮助方法开始**********************************************************/
	private String  save_xml(MRecord groupRe,String filename,Document doc){
		//保存格式: xml/templateid/groupid/..xml
		String tempateid=groupRe.getStr("templateid");
		String groupid=groupRe.getStr("groupid");
		String path="/xml/"+tempateid+"/"+groupid;
		String full_path=MFile.createFile(MPath.me().getWebRoot()+path,filename).getPath();
		builder(full_path,doc);
		
		//保存到表中
		MRecord inData=new MRecord().set("templateid", tempateid).set("groupid", groupid).set("path", path).set("name", filename);
		if(filename.equals("movie.xml")){
			inData.set("ftype", "V");
		}else if(filename.equals("scrollingtext.xml")||filename.equals("text.xml")){
			inData.set("ftype", "W");
		}else if(filename.equals("image.xml")){
			inData.set("ftype", "P");
		}	
		
		String where=MCnt.me().and_eq("templateid", tempateid).and_eq("groupid", groupid).and_eq("name",filename ).toStr();
		insert(inData,where);
		return full_path;
	}
 	private String getFileName(String path){
		return path.substring(path.lastIndexOf("/")+1); 
	}
	private void insert(MRecord inData,String where){
		boolean exist=M.me().check(MMsg.TB_RESOURCE, where);
		if(!exist){
			M.me().insert(MMsg.TB_RESOURCE, inData.set("id", Tools.generate_id()));
		}
	}
	/**
	 * @param filepath 文件保存的路径
	 * @param document 创建的文档
	 */
	private void builder(String filepath, Document doc) {
		try {
			XMLWriter writer = new XMLWriter(new FileWriter(filepath));
			writer.write(doc);
			writer.close();
			printXML(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printXML(Document doc) {
		// 设置了打印的格式,将读出到控制台的格式进行美化
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(System.out, format);
			writer.write(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/***********************************************************帮助方法结束**********************************************************/	

	
}

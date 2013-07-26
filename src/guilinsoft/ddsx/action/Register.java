package guilinsoft.ddsx.action;

import guilinsoft.ddsx.core.MHttpServlet;
import guilinsoft.ddsx.util.MMsg;
import guilinsoft.ddsx.util.Tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jzero.cache.C;
import com.jzero.util.MCheck;
import com.jzero.util.MDate;
import com.jzero.util.MMD5;
import com.jzero.util.MPrint;
import com.jzero.util.MRecord;

/**
 * 终端第一次连接上来进行注册
 */
public class Register extends MHttpServlet {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unchecked")
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		begin(req);
		MRecord record = new MRecord();
		MRecord tempRecord=new MRecord();
		String temp[]=null;
		Enumeration<String> ene = req.getHeaderNames();
		while (ene.hasMoreElements()) {
			String key = ene.nextElement();
			String v = req.getHeader(key);
			if(key.equals("authorization")){
				v=v.substring(v.indexOf("Digest")+6).replaceAll("\"", "");
				String[] auths=v.split(",");
				for(String s:auths){
					temp=s.split("=");
					if(temp.length>1){
						if(temp[0].equalsIgnoreCase("uri")){
							tempRecord.set(temp[0], s.substring(s.indexOf("=")+1));
						}else{
							tempRecord.set(temp[0].trim(), temp[1]);
						}
					}
				}
			}
			record.set(key, v);
		}
		String audit = record.getStr("authorization");
		MRecord inDatas=new MRecord();
		if (MCheck.isNull(audit)) {
			first_visit(resp);
		} else {
			boolean isOk=Tools.validateRegister(tempRecord);
			if(isOk){
				second_visit(resp);
				inDatas.set(MMsg.INFO_UPDATE_TIME, MDate.get_ymd_hms());
				inDatas.set(MMsg.INFO_DEVICEID, record.getStr(MMsg.INFO_DEVICEID));
				C.setCache(record, record.getStr(MMsg.INFO_DEVICEID));	 //用于取缓存,登录时间
				Tools.update_termstatetemp(getDeviceid(), 0);
			}else{
				first_visit(resp);
			}
		}
		end();
	}
	private void second_visit(HttpServletResponse resp) {
		try {
			// 更新当前设备的在线状态
			// 假设这里验证成功,则返回200
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(resp
					.getOutputStream()));
			String au = "qop='auth',nextnonce='"
					+ MMD5.toMD5ByJAVA(System.currentTimeMillis() + "")
					+ "',rspauth='" + MMD5.toMD5ByJAVA(System.nanoTime() + "")
					+ "' ,nc=" + getNc() +" ,cnonce="+getCnonce();
			MPrint.print("第二次返回:" + au);
			resp.setHeader("Authentication-Info", au);
			resp.setStatus(HttpServletResponse.SC_OK);
			bw.write("");// 这里返回内容是body:"",如果省去这一段,会发出this http request
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 第一次回复
	 */
	private void first_visit(HttpServletResponse resp) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(resp
					.getOutputStream()));
			String au = "Digest realm=\"wdpf@service.cmcc.cn\",qop=\"auth\",nonce=\""
					+ Tools.random(32)
					+ "\",opaque=\""
					+ MMD5.toMD5ByJAVA(Tools.generate_id() + "") + "\"";
			MPrint.print("第一次回复: " + au);
			resp.setHeader("WWW-Authenticate", au);
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			bw.write("");// //这里返回内容是body:"",如果省去这一段,会发出this http request
			// Authentication
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package guilinsoft.ddsx.core;

import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import com.jzero.util.MDate;
import com.jzero.util.MPro;
import com.jzero.util.Msg;

public class MHttpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static {
		MPro.me().load_file(Msg.DB_CONFIG).load_file(Msg.CONFIG).load_file(Msg.OTHER_CONFIG);
	}
	private String deviceid;
	private String nc;
	private String cnonce;
	@SuppressWarnings("unchecked")
	protected void begin(HttpServletRequest req){
		System.out.println("----------------------------------------"+getClass().getSimpleName()+" 开始 ----------------------"+req.getMethod()+","+MDate.get_ymd_hms());
		System.out.println("来源: "+req.getRequestURL()+"?"+req.getQueryString());
		Enumeration<String>  ene=req.getHeaderNames();
		while(ene.hasMoreElements()){
			String key=ene.nextElement();
			String v=req.getHeader(key);
			if(key.equals("x-device-id")){
				setDeviceid(v);
			}
			if(key.equals("authorization")){
				String[] auths=v.split(",");
				for(String str:auths){
					if(str.startsWith("nc")||str.startsWith("cnonce")){
						String[] kv = str.split("=");
						setNc(kv[0]);
						setCnonce(kv[1]);
					}
				}
			}
			System.out.println(key+" = "+v);
		}
	}
	protected void end(){
		System.out.println("----------------------------------------"+getClass().getSimpleName()+" 结束-----------------------");
	}
	public String getDeviceid() {
		return deviceid;
	}
	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}
	public String getNc() {
		return nc;
	}
	public void setNc(String nc) {
		this.nc = nc;
	}
	public String getCnonce() {
		return cnonce;
	}
	public void setCnonce(String cnonce) {
		this.cnonce = cnonce;
	}
	
}

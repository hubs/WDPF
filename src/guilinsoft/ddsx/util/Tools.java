package guilinsoft.ddsx.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import com.jzero.db.core.M;
import com.jzero.log.Log;
import com.jzero.util.MCheck;
import com.jzero.util.MCnt;
import com.jzero.util.MDate;
import com.jzero.util.MMD5;
import com.jzero.util.MPrint;
import com.jzero.util.MPro;
import com.jzero.util.MRecord;

public class Tools {
	public static String generate_id() {
		return MDate.getSimpleDateFormat("yyyyMMddHHmmssSSS").format(
				MDate.newDate())
				+ random(15);
	}

	public static String random(int num) {
		Random random = new Random();
		char[] c = new char[num];
		for (int i = 0; i < num; i++) {
			int choice = random.nextInt(2) % 2 == 0 ? 65 : 97; // 取得大写还是小写
			c[i] = (char) (choice + random.nextInt(26));
		}
		return new String(c);
	}

	// 要加"/"
	public static String getTargetBase() {
		return MPro.me().getStr("target");
	}

	// 要加"/"
	public static String getBase() {
		return MPro.me().getStr("base");
	}

	/**
	 * 在登录时或orderbystate 时调用 登录时,将deviceid设备的状态全部制为0:表示不存在
	 * 在调用orderbystate时将存在的状态制为1:表示存在
	 * 
	 * @param deviceid
	 */
	public static void update_termstatetemp(String deviceid, int state) {
		MRecord mRecord = M.me().one_t(MMsg.TB_TERMSTATETEMP,
				MCnt.me().and_eq("deviceid", deviceid).toStr());
		if (!MCheck.isNull(mRecord)) {
			if (!mRecord.getStr("state").equals(state)) { // 不相等的时候修改状态
				M.me().update(MMsg.TB_TERMSTATETEMP,new MRecord().set("state", state).set("selecttime", MDate.get_ymd_hms()),MCnt.me().first_eq("deviceid", deviceid).toStr());
			}
		} else {
			M.me().insert(MMsg.TB_TERMSTATETEMP,new MRecord().set("state", state).set("deviceid",deviceid).set("selecttime", MDate.get_ymd_hms()));
		}
	}

	public static boolean validateRegister(MRecord record) {
		String username =record.getStr("username").trim();
		String realm =  record.getStr("realm").trim();
		String nonce= record.getStr("nonce").trim();
		String cnonce = record.getStr("cnonce").trim();
		String password ="a79352ec7f1946ebaaec2ceb3db64f98";
		String nc = record.getStr("nc").trim();
		String method = "GET".trim();
		String qop = record.getStr("qop").trim();
		String uri =record.getStr("uri").trim();
		String client_response=record.getStr("response");
		
		String secret = MMD5.toMD5ByJAVA(username + ":" + realm + ":"+ password);
		String A2 = MMD5.toMD5ByJAVA(method + ":" + uri);
		String data = nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + A2;// +":"+opaque;
		String server_response = MMD5.toMD5ByJAVA(secret + ":" + data);
		MPrint.print(client_response.length()+"="+server_response.length());
		MPrint.print(client_response+"="+server_response);
		return server_response.equals(client_response);
	}
	public static boolean checkFileExist(String file){
		File fpath=new File(file);
		return fpath.exists();
	}
	/**
	 * 要使用绝对路径
	 * @param sourceFile
	 * @param destFile
	 */
	public static  void copyFile(String sourceFile,String destFile) {
	    FileInputStream in =null;
	    FileOutputStream out =null;
	    byte[] buffer = new byte[102400];
	    try {
	       in = new FileInputStream(sourceFile);
	       File dest = new File(destFile);
	       if(!dest.exists()){//目标文件对应的目录不存在，创建新的目录
	            int index = new String(destFile).lastIndexOf("/");
	            String path = destFile.substring(0, index);
	            new File(path).mkdirs();
	       }
	       out = new FileOutputStream(destFile);
	       int num =0;
	       while((num=in.read(buffer))!=-1){
	             out.write(buffer,0,num);
	       }
	    } catch (Exception e){
	    	Log.me().write_error(e);
	    } finally{
	        try {
	             if(in!=null)in.close();
	             if(out!=null)out.close();
	            } catch (IOException ex) {
	         }   
	     }
	 }
	    
	    public static void main(String[] args){
	        long startTime =System.currentTimeMillis();
	        System.out.println("start to copy");
	        long endTime = System.currentTimeMillis();
	        long time = (endTime-startTime)/1000;
	        System.out.println("copy end;cost:"+time);
	    }
}

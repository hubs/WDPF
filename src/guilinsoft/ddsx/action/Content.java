package guilinsoft.ddsx.action;

import guilinsoft.ddsx.core.MHttpServlet;
import guilinsoft.ddsx.util.MMsg;
import guilinsoft.ddsx.util.Tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jzero.db.core.M;
import com.jzero.util.MCheck;
import com.jzero.util.MCnt;
import com.jzero.util.MPrint;
import com.jzero.util.MRecord;

public class Content extends MHttpServlet {
	private static final long serialVersionUID = 1L;
	private String range = null;

	@SuppressWarnings("unchecked")
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		begin(req);
		Enumeration<String> ene = req.getHeaderNames();
		while (ene.hasMoreElements()) {
			String key = (String) ene.nextElement();
			String v = req.getHeader(key);
			if (key.equals("range")) {
				range = v;
			}
		}
		MPrint.print("下载任务");
		try {
			down(resp,req);
		} catch (Exception e) {
			e.printStackTrace();
		}
		end();
	}
	//断点下载
	public void down(HttpServletResponse resp,HttpServletRequest request) throws Exception {
		String serno=request.getParameter("serialno");
		MRecord reObj=M.me().one_t(MMsg.TB_PLAYLISTTASKITEM, MCnt.me().and_eq("serialno", serno).toStr());
		String uri=Tools.getTargetBase()+"/"+reObj.getStr("url");
		MPrint.print("断点 uri= "+uri);
		URL url = new URL(uri);
		File file =new File("");
		long fileLen =url.openConnection().getContentLength();//file.length();
		long begin = 0;
		long end = fileLen - 1;
		String contentRange = null;
		if (!MCheck.isNull(range)) {
			// 设置状态
			resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
			// 得到请求byte范围
			String rangeBytes = range.replace("bytes=", "");
			String[] rangeArr = rangeBytes.trim().split("-");
			begin = Long.parseLong(rangeArr[0]);
			// 如果请求有结束范围 eg:1024000-2058220
			if (rangeArr.length > 1) {
				end = Long.parseLong(rangeArr[1]);
			}
			contentRange = new StringBuffer("bytes ").append(begin).append("-").append(end).append("/").append(fileLen).toString();
		}
		resp.setContentType("application/x-msdownload");// 设备下载的编码方式
		resp.setHeader("Accept-Ranges", "bytes");
		resp.setHeader("Content-Range", contentRange);
		resp.addHeader("Content-Length", String.valueOf(end + 1 - begin)); // 设置下载内容的大小
		resp.setCharacterEncoding("UTF-8");
		RandomAccessFile randomf = null;
		try {
			BufferedOutputStream bw = new BufferedOutputStream(resp
					.getOutputStream());
			randomf = new RandomAccessFile(file, "r");
			byte[] bt = new byte[1024 * 1024];
			int i = -1;
			try {
				// 读取数据
				randomf.seek(begin);
				while ((i = randomf.read(bt)) != -1) {
					if (randomf.getFilePointer() >= end) {
						bw.write(bt, 0, (int) (i + end- randomf.getFilePointer() + 1));
						break;
					} else
						bw.write(bt, 0, i);
				}
				bw.flush();
			} catch (Exception e) {
				System.out.println("下载报错：" + e.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

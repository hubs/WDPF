<%@ page language="java" import="java.util.*,com.jzero.util.*,com.jzero.core.*,com.jzero.log.*,guilinsoft.ddsx.quartz.*,guilinsoft.ddsx.util.MMsg" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <style>
    table{width:100%;margin-bottom:18px;padding:0;border-collapse:separate;*border-collapse:collapse;font-size:13px;border:1px solid #ddd;-webkit-border-radius:4px;-moz-border-radius:4px;border-radius:4px;text-align:center}
    .btn-primary {color: white;text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.25);background-color: #006DCC;background-image: -moz-linear-gradient(top, #08C, #04C);background-image: -webkit-gradient(linear, 0 0, 0 100%, from(#08C), to(#04C));background-image: -webkit-linear-gradient(top, #08C, #04C);background-image: -o-linear-gradient(top, #08C, #04C);background-image: linear-gradient(to bottom, #08C, #04C);background-repeat: repeat-x;border-color: #04C #04C #002A80;filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#ff0088cc', endColorstr='#ff0044cc', GradientType=0);filter: progid:DXImageTransform.Microsoft.gradient(enabled=false);}
	.btn-large {padding: 11px 19px;font-size: 17.5px;-webkit-border-radius: 6px;-moz-border-radius: 6px;border-radius: 6px;cursor: pointer;}
    table tr + tr td {border-top: 1px solid #DDD;}
    </style>
  </head>
  <body>
  <%
  		MPro.me().load_file(Msg.OTHER_CONFIG);
  		boolean bool_run=MPro.me().getBool("bool_start");
  		String subText=bool_run?"已运行":"运行定时器";
  		String disabled=bool_run?"disabled='disabled'":"";
   %>
<form action="" method="post" onsubmit="return confirm('是否开启定时器?')">
<input type="hidden" name="action" value="submit"/>
<table>
	<tr><td><input type="submit" class="btn-large btn-primary" <%=disabled %> value="<%=subText %>"/></td></tr>
</table>
</form>
<%
	String action=request.getParameter("action");
	if(!MCheck.isNull(action)&&action.equals("submit")){
			if(!bool_run){
				MPro.me().setValue("bool_start","true");
				try{
					MTrigger.me().run();
				}catch(Exception e){
					Log.me().write(e.getLocalizedMessage());
				}
			}
	}
 %>

  </body>
</html>

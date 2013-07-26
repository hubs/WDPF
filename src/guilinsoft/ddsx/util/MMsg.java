package guilinsoft.ddsx.util;

public class MMsg {
	
	
	//模板
	public static final String TB_TEMPLATE			=		"template";
	
	
	//子模板
	public static final String TB_SUBTEMPLATE		=		"subtemplate";
	
	//存放模板位置
	public static final String TB_RESOURCE			=		"m_resource";
	
	//播放列表
	public static final String TB_PLAYLISTTASKITEM	=		"playlisttaskitem";
	
	//群组表
	public static final String TB_TERMGROUP			=		"termgroup";
	
	//终端临时状态
	public static final String TB_TERMSTATETEMP		=		"termstatetemp";
	
	//终端表
	public static final String TB_TERMINAL			=		"terminal";
	
	public static final String TB_PLAYTASK			=		"PlayTask";
	public static final String TB_CONTENT			=		"Content";	
	//终端ID
	public static final String INFO_DEVICEID		=		"x-device-id";
	
	//更新时间
	public static final String INFO_UPDATE_TIME		=		"updatetime";
	
	public static final String INFO_REGETAD			=		"-regetad";
	
	public static final String INFO_TEMPLATE_DOWN	=		"-template";
	
	/**
	 * 2013-2-21
	 * 0:未接收,1:已接收
	 * 5:删除,3:已删除
	 * 6:自定义的,模板ID,7:已下发
	 */
	public static final int ADD_COMMEND				=		0;
	public static final int ADD_COMMENDED			=		1;
	public static final int DEL_ED_COMMEND			=		3;
	public static final int DEL_COMMEND				=		5;
	public static final int TEMPLATE_COMMEND		=		6;
	public static final int TEMPLATE_ED_COMMEND		=		7;
	/**
	 * 2013-7-26：在接收状态时使用
	 * 8:执行失败
	 * 9:任务取消
	 */
	public static final int TAST_ERROR				=		8;
	public static final int TAST_CANEL				=		9;
}

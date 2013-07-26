package guilinsoft.ddsx.api;

public interface G3API {
	/**
	 *  审批成功, 更新任务状态 actionAuditAdSche
	 */
	void releasetask(String groupId);
	
	/**
	 * 发布播放列表
	 */
	void release(String playlistId);
	
	/**
	 * 群组下广告内容同步到终端  synchroGroupTaskToTerminal
	 */
	void importad(String groupId,String terminalid);
	
	/**
	 * 更新群组模板 editGroupTemplate
	 */
	void changeTemplate(String groupid);
	
	/**
	 * 更新播放列表发送状态 updateSendPlayListStatus
	 */
	boolean timeLineReleasetask(String groupid);
	
	/**
	 * 检查播放列表发送状态 checkSendPlayList,结果值用 | 分隔
	 */
	String crossValidation(String groupid,String playlistid,String startDate,String endDate);

	/**
	 * 按地市统计其下终端的连接壮态 SAByLocation2
	 */
	void orderbystate();
	
	/**
	 * 终端是否在线 ,返回记录的时间ymd his
	 * shortnum:设备码
	 */
	String getselecttime(String deviceId);
	
	/**
	 * 生成模板文件 makeTemplateXML
	 */
	boolean saveTemplateToXML(String templateid);
	
	
}

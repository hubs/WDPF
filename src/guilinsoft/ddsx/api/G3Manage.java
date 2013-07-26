package guilinsoft.ddsx.api;


public class G3Manage {
	private G3Manage() {
	}
	private static G3API g3api = new WdpfImpl();
	private static G3Manage instanct=new G3Manage();
	public static  G3Manage me(){
		return instanct;
	}
	public void changeTemplate(String groupid) {
		g3api.changeTemplate(groupid);
	}

	public String crossValidation(String groupid, String playlistid,
			String startDate, String endDate) {
		return g3api.crossValidation(groupid, playlistid, startDate, endDate);
	}

	public String getselecttime(String deviceId) {
		return g3api.getselecttime(deviceId);
	}

	public void importad(String groupId, String terminalid) {
		g3api.importad(groupId, terminalid);
	}

	public void orderbystate() {
		g3api.orderbystate();
	}

	public void release(String playlistId) {
		g3api.release(playlistId);
	}

	public void releasetask(String groupId) {
		g3api.releasetask(groupId);
	}

	public boolean saveTemplateToXML(String templateid) {
		return g3api.saveTemplateToXML(templateid);
	}

	public boolean timeLineReleasetask(String groupid) {
		return g3api.timeLineReleasetask(groupid);
	}

}

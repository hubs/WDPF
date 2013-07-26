package guilinsoft.ddsx.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.jzero.util.MPrint;
import com.jzero.util.MPro;

public class MTrigger {
	private static MTrigger instanct=new MTrigger();
	private static	SchedulerFactory sf = new StdSchedulerFactory();
	private MTrigger(){}
	public static MTrigger me(){
		return instanct;
	}
	public void run() throws Exception {
		MPrint.print(" 已执行定时任务 ...");
		Scheduler sched = sf.getScheduler();
	    JobDetail job = JobBuilder.newJob(MCron.class).withIdentity("job1", "group1").build();
	  //0 59 23 * * ?	 每天23点59分触发
	    CronTrigger trigger = (CronTrigger)TriggerBuilder.newTrigger().withIdentity("trigger1", "group1").withSchedule(CronScheduleBuilder.cronSchedule(MPro.me().getStr("cron"))).build();

	    sched.scheduleJob(job, trigger);
		sched.start();
	}
}

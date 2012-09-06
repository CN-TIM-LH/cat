package com.dianping.cat.report.task.database;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.task.AbstractReportBuilder;
import com.dianping.cat.report.task.ReportBuilder;
import com.dianping.cat.report.task.TaskHelper;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class DatabaseReportBuilder extends AbstractReportBuilder implements ReportBuilder {
	
	@Inject
	private DatabaseMerger m_databaseMerger;

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			Dailyreport report = getdailyReport(reportName, reportDomain, reportPeriod);
			m_dailyReportDao.insert(report);
			return true;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private Dailyreport getdailyReport(String reportName, String reportDatabase, Date reportPeriod) throws DalException {
		Date endDate = TaskHelper.tomorrowZero(reportPeriod);
		Set<String> domainSet = new HashSet<String>();
		getDomainSet(domainSet, reportPeriod, endDate);
		List<Report> reports = m_reportDao.findAllByDomainNameDuration(reportPeriod, endDate, reportDatabase, reportName,
		      ReportEntity.READSET_FULL);
		String content = m_databaseMerger.mergeForDaily(reportDatabase, reports, domainSet).toString();

		Dailyreport report = m_dailyReportDao.createLocal();
		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(reportDatabase);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(reportName);
		report.setPeriod(reportPeriod);
		report.setType(1);
		return report;
	}
	
	@Override
	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod){
		throw new RuntimeException("Database report don't support HourReport!");
	}

	@Override
	public boolean redoDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		try {
			Dailyreport report = getdailyReport(reportName, reportDomain, reportPeriod);
			clearDailyReport(report);
			m_dailyReportDao.insert(report);
			return true;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean redoHourReport(String reportName, String reportDomain, Date reportPeriod) {
		throw new RuntimeException("Database report don't support redo HourReport!");
	}

}

package org.wso2.carbon.bam.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.ClientUtil;
import org.wso2.carbon.bam.common.dataobjects.dimensions.*;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.BAMSummaryGenerationDSStub;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.types.*;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;

import java.util.Calendar;


public class SummaryDimensionDSClient {
    private static final String BAM_SUMMARY_GENERATION_DS = "BAMSummaryGenerationDS";
    private static final Log log = LogFactory.getLog(SummaryDimensionDSClient.class);

    private BAMSummaryGenerationDSStub summaryGenerationDSStub;

      public SummaryDimensionDSClient(String backendServerURL,
                                          ConfigurationContext configCtx) throws BAMException {
          try {
              String serviceURL = ClientUtil.getBackendEPR(backendServerURL, BAM_SUMMARY_GENERATION_DS);
              summaryGenerationDSStub = new BAMSummaryGenerationDSStub(configCtx, serviceURL);
          } catch (Exception e) {
              throw new BAMException(e.getMessage(), e);
          }
      }

      public SummaryDimensionDSClient(String cookie, String backendServerURL,
                                          ConfigurationContext configCtx) throws BAMException {
          try {
              String serviceURL = ClientUtil.getBackendEPR(backendServerURL, BAM_SUMMARY_GENERATION_DS);
              summaryGenerationDSStub = new BAMSummaryGenerationDSStub(configCtx, serviceURL);
          } catch (Exception e) {
              throw new BAMException(e.getMessage(), e);
          }
          ServiceClient client = summaryGenerationDSStub._getServiceClient();
          Options option = client.getOptions();
          option.setManageSession(true);
          option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
      }

    public void cleanup() {
        try {
            summaryGenerationDSStub._getServiceClient().cleanupTransport();
//            summaryGenerationDSStub._getServiceClient().cleanup();
//            summaryGenerationDSStub.cleanup();
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Stub cleanup failed: " + this.getClass().getName(), axisFault);
            }
        }
    }

    private void populateHourDimension(HourDimension hd, HourDim thd) {
          hd.setId(Integer.parseInt(thd.getBamId()));
          hd.setDayDim(Integer.parseInt(thd.getDayId()));
          hd.setHour(Integer.parseInt(thd.getHourNo()));
          hd.setStartTimestamp(thd.getStartTime());
      }

      public HourDimension getHourDimension(int hourId) throws BAMException {
          HourDimension hd = null;
          try {
              HourDim[] hdArr = summaryGenerationDSStub.getHourDimFromId(hourId);

              if (hdArr != null && hdArr[0] != null) {
                  hd = new HourDimension();
                  populateHourDimension(hd, hdArr[0]);
              }
          } catch (Exception e) {
              throw new BAMException("getHourDimension failed", e);
          }
          return hd;
      }

      public HourDimension getHourDimension(int hour, int dayId) throws BAMException {
          HourDimension hd = null;
          try {
              HourDim[] hdArr = summaryGenerationDSStub.getHourDim(hour, dayId);
              if (hdArr != null && hdArr[0] != null) {
                  hd = new HourDimension();
                  populateHourDimension(hd, hdArr[0]);
              }
          } catch (Exception e) {
              throw new BAMException("getHourDimension failed", e);
          }
          return hd;
      }

      private void pupulateDayDimension(DayDimension dayDimension, DayDim dayDim) {
          dayDimension.setId(Integer.parseInt(dayDim.getBamId()));
          dayDimension.setName(dayDim.getName());
          dayDimension.setMonthDim(Integer.parseInt(dayDim.getMonthId()));
          dayDimension.setDayOfMonth(Integer.parseInt(dayDim.getDayOfMonth()));
          dayDimension.setDayOfWeek(Integer.parseInt(dayDim.getDayOfWeek()));
          dayDimension.setDayOfYear(Integer.parseInt(dayDim.getDayOfYear()));
          dayDimension.setStartTimestamp(dayDim.getStartTime());
      }

      public DayDimension getDayDimension(int day, int monthId) throws BAMException {
          DayDimension dd = null;
          try {
              DayDim[] ddArr = summaryGenerationDSStub.getDayDim(day, monthId);

              if (ddArr != null && ddArr[0] != null) {
                  dd = new DayDimension();
                  pupulateDayDimension(dd, ddArr[0]);
              }
          } catch (Exception e) {
              throw new BAMException("getDayDim failed", e);
          }
          return dd;
      }

      public DayDimension getDayDimension(int dayId) throws BAMException {
          DayDimension dd = null;
          try {
              DayDim[] ddArr = summaryGenerationDSStub.getDayDimFromId(dayId);
              if (ddArr != null && ddArr[0] != null) {

                  dd = new DayDimension();
                  pupulateDayDimension(dd, ddArr[0]);
              }
          } catch (Exception e) {
              throw new BAMException("getDayDimFromId failed", e);
          }
          return dd;
      }

      private void populateMonthDimension(MonthDimension md, MonthDim tmd) {
          md.setId(Integer.parseInt(tmd.getBamId()));
          md.setMonth(Integer.parseInt(tmd.getMonthNo()));
          md.setName(tmd.getName());
          md.setQuarterDim(Integer.parseInt(tmd.getQuarterId()));
          md.setStartTimestamp(tmd.getStartTime());
      }

      public MonthDimension getMonthDimension(int month, int quarterId) throws BAMException {
          MonthDimension md = null;
          try {
              MonthDim[] mdArr = summaryGenerationDSStub.getMonthDim(month, quarterId);
              if (mdArr != null && mdArr[0] != null) {
                  md = new MonthDimension();
                  populateMonthDimension(md, mdArr[0]);
              }
          } catch (Exception e) {
              throw new BAMException("getMonthDim failed", e);
          }
          return md;
      }

      public MonthDimension getMonthDimension(int monthId) throws BAMException {
          MonthDimension md = null;
          try {
              MonthDim[] mdArr = summaryGenerationDSStub.getMonthDimFormId(monthId);

              if (mdArr != null && mdArr[0] != null) {
                  md = new MonthDimension();
                  populateMonthDimension(md, mdArr[0]);
              }
          } catch (Exception e) {
              throw new BAMException("getMonthDimFormId failed", e);
          }
          return md;
      }

      private void populateQuarterDimension(QuarterDimension qd, QuarterDim tqd) {
          qd.setId(Integer.parseInt(tqd.getBamId()));
          qd.setName(tqd.getName());
          qd.setQuarter(Integer.parseInt(tqd.getQuarterNo()));
          qd.setYearDim(Integer.parseInt(tqd.getYearId()));
          qd.setStartTimestamp(tqd.getStartTime());
      }

      public QuarterDimension getQuarterDimension(int quarter, int yearId) throws BAMException {
          QuarterDimension qd = null;
          try {
              //TODO fix the DS to Quarter
              QuarterDim[] qdArr = summaryGenerationDSStub.getQuarterDim(quarter, yearId);

              if (qdArr != null && qdArr[0] != null) {
                  qd = new QuarterDimension();
                  populateQuarterDimension(qd, qdArr[0]);
              }
          } catch (Exception e) {
              throw new BAMException("getQuarterDim failed", e);
          }
          return qd;
      }

      public QuarterDimension getQuarterDimension(int quarterId) throws BAMException {
          QuarterDimension qd = null;
          try {
              //TODO fix the DS to Quarter
              QuarterDim[] qdArr = summaryGenerationDSStub.getQuarterDimFromId(quarterId);

              if (qdArr != null && qdArr[0] != null) {
                  qd = new QuarterDimension();
                  populateQuarterDimension(qd, qdArr[0]);
              }
          } catch (Exception e) {
              throw new BAMException("getQuarterDimFromId failed", e);
          }
          return qd;
      }

      private void populateYearDimension(YearDimension yd, YearDim tyd) {
          yd.setId(Integer.parseInt(tyd.getBamId()));
          yd.setYear(Integer.parseInt(tyd.getYearNo()));
          yd.setStartTimestamp(tyd.getStartTime());
      }

      public YearDimension getYearDimension(int year) throws BAMException {
          YearDimension yd = null;
          try {
              YearDim[] ydArr = summaryGenerationDSStub.getYearDim(year);

              if (ydArr != null && ydArr[0] != null) {
                  yd = new YearDimension();
                  populateYearDimension(yd, ydArr[0]);
              }
          } catch (Exception e) {
              throw new BAMException("getYearDim failed", e);
          }
          return yd;
      }

      public YearDimension getYearDimensionFromId(int yearId) throws BAMException {
          YearDimension yd = null;
          try {
              YearDim[] ydArr = summaryGenerationDSStub.getYearDimFromId(yearId);
              if (ydArr != null && ydArr[0] != null) {
                  yd = new YearDimension();
                  populateYearDimension(yd, ydArr[0]);
              }
          } catch (Exception e) {
              throw new BAMException("getYearDimFromId failed", e);
          }
          return yd;
      }

      public void addHourDimension(BAMCalendar startTime, int dayId) throws BAMException {
          try {
              summaryGenerationDSStub.addHourDim(startTime, startTime.get(Calendar.HOUR_OF_DAY), dayId);
          } catch (Exception e) {
              throw new BAMException("addHourDim failed", e);
          }
      }

      public void addDayDimension(BAMCalendar startTime, String name, int monthId) throws BAMException {
          try {
              summaryGenerationDSStub.addDayDim(startTime, name, startTime.get(Calendar.DAY_OF_WEEK_IN_MONTH),
                      startTime.get(Calendar.DAY_OF_MONTH), startTime.get(Calendar.DAY_OF_YEAR), monthId);
          } catch (Exception e) {
              throw new BAMException("addDayDim failed", e);
          }
      }

      public void addMonthDimension(BAMCalendar startTime, String name, int quarterId) throws BAMException {
          try {
              summaryGenerationDSStub.addMonthDim(startTime, name, startTime.get(Calendar.MONTH), quarterId);
          } catch (Exception e) {
              throw new BAMException("addMonthDim failed", e);
          }
      }

      public void addQuarterDimension(BAMCalendar startTime, String name, int yearId) throws BAMException {
          try {
              //TODO fix BAMCalendar.QUATER --> this should become QUARTER
              summaryGenerationDSStub.addQuarterDim(startTime, name, startTime.get(BAMCalendar.QUATER), yearId);
          } catch (Exception e) {
              throw new BAMException("addQuarterDim failed", e);
          }
      }

      public void addYearDimension(BAMCalendar startTime) throws BAMException {
          try {
              summaryGenerationDSStub.addYearDim(startTime, startTime.get(BAMCalendar.YEAR));
          } catch (Exception e) {
              throw new BAMException("addYearDim failed", e);
          }
      }


}

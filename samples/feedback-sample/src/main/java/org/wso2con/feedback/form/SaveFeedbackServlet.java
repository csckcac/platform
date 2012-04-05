package org.wso2con.feedback.form;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.wso2con.feedback.db.FeedbackResultStat;

public class SaveFeedbackServlet extends HttpServlet {

	private String path;
	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	HashMap<Integer, FeedbackResultStat> results;

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		OutputStream out = null;
		try {
			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-Disposition",
					"attachment; filename=wso2ConFeedback.xls");
			WritableWorkbook w = Workbook.createWorkbook(response
					.getOutputStream());
			WritableSheet s1 = w.createSheet("Ratings", 0);
			WritableSheet s2 = w.createSheet("Comment", 1);
			
			ServletContext context = getServletContext();
			results = (HashMap<Integer, FeedbackResultStat>) context.getAttribute("feedbackMap");
			
			// Lets create a times font
			WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
			// Define the cell format
			times = new WritableCellFormat(times10pt);
			// Lets automatically wrap the cells
			times.setWrap(true);

			// Create create a bold font with unterlines
			WritableFont times10ptBoldUnderline = new WritableFont(
					WritableFont.TIMES, 10, WritableFont.BOLD, false,
					UnderlineStyle.SINGLE);
			timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
			// Lets automatically wrap the cells
			timesBoldUnderline.setWrap(true);

			CellView cv = new CellView();
			cv.setFormat(times);
			cv.setFormat(timesBoldUnderline);
			cv.setAutosize(true);



			//s1.addCell(new Label(0, 0, "Hello World"));
			addCaption(s1,0,0,"Question");
			addCaption(s1,1,0,"Not Answered");
			addCaption(s1,2,0,"Very Poor");
			addCaption(s1,3,0,"Poor");
			addCaption(s1,4,0,"Good");
			addCaption(s1,5,0,"Very good");
			addCaption(s1,6,0,"Excellent");
			if (results != null) {
				for (int i = 0; i < results.size(); i++) {
					FeedbackResultStat answerCounts = results.get(i);
					
					if (answerCounts != null && !answerCounts.getQuestion().equals("Any Other Comment")) {
						HashMap<Integer, Integer> answerMap = answerCounts
								.getAnswerMap();
						addLabel (s1,0,(i+1),answerCounts.getSession());
						int count =0;
	           		 for (int x=0;x<answerMap.size();x++) {
	           			addNumber(s1,(x+1),(i+1), answerMap.get(x));
	           			 count = count+answerMap.get(x);
	           		 }
					}
				}
				
			} else {
				System.out.println("Results null cannot be!!!");
			}
			
			int index =1;
			for (int i = 0; i < results.size(); i++) {
				FeedbackResultStat answerCounts = results.get(i);

				if (answerCounts != null) {
					ArrayList<String> comments = answerCounts.getComments();
					addCaption (s2,0,index,answerCounts.getSession());
					for (int x = 0; x < comments.size(); x++) {
						index = index+1;
						addLabel(s2,0,index, comments.get(x));
	           		 }
				}
			}
			w.write();
			w.close();
		} catch (Exception e) {
			throw new ServletException("Exception in Excel Sample Servlet", e);
		} finally {
			if (out != null)
				out.close();
		}

	}

	

	private void addCaption(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, timesBoldUnderline);
		sheet.addCell(label);
	}

	private void addNumber(WritableSheet sheet, int column, int row,
			Integer integer) throws WriteException, RowsExceededException {
		Number number = new Number(column, row, integer, times);
		sheet.addCell(number);
	}

	private void addLabel(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, times);

		sheet.addCell(label);
	}

}

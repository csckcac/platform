<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2con.feedback.db.FeedbackDO" %>
<%@ page import="org.wso2con.feedback.db.FeedbackAppDAO" %>
<%@ page import="org.wso2con.feedback.db.QuestionDO" %>
<%@ page import="org.wso2con.feedback.db.SessionDO" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="java.util.ArrayList" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<fmt:bundle basename="org.wso2con.feedback.form.i18n.Resources">

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Feedback - WSO2Con</title>
    <link rel="stylesheet" href="../css/feedback.css">
    <link rel="stylesheet" href="../css/carbonFormStyles.css" type="text/css">

    <script type="text/javascript" src="../js/jquery/jquery-1.5.1.min.js"></script>

    <link rel="stylesheet" href="../js/jquery/themes/ui-lightness/jquery.ui.core.css">


    <script type="text/javascript">
        jQuery(document).ready(
                function() {
                    function initSections(initHidden) {
                        jQuery(".togglebleTitle").click(
                                function() {
                                    if (jQuery(this).next().is(":visible")) {
                                        jQuery(this).addClass("contentHidden");
                                    } else {
                                        jQuery(".togglebleTitle").next().hide();
                                        jQuery(".togglebleTitle").addClass("contentHidden");

                                        jQuery(this).removeClass("contentHidden");
                                    }
                                    jQuery(this).next().toggle("fast");
                                }
                                );
                    }
                    initSections("");
                }

                );
        function popup(message) {

            // get the screen height and width
            var maskHeight = jQuery(document).height();
            var maskWidth = jQuery(window).width();

            // calculate the values for center alignment
            var dialogTop = (maskHeight / 3) - (jQuery('#dialog-box').height());
            var dialogLeft = (maskWidth / 2) - (jQuery('#dialog-box').width() / 2);

            // assign values to the overlay and dialog box
            jQuery('#dialog-overlay').css({height:maskHeight, width:maskWidth}).show();
            jQuery('#dialog-box').css({top:dialogTop, left:dialogLeft}).show();

            // display the message
            jQuery('#dialog-message').html(message);

        }

        function submitFeedBack(buttonId) {

            var commentId = "qc" + buttonId;
            var ratingId = "qr" + buttonId;
            var comment = document.getElementById(commentId).value;
            var ratings = document.getElementsByName(ratingId);
            var ratingValue;
            for (var a = 0; a < ratings.length; a++) {
                if (ratings[a].checked) {
                    ratingValue = ratings[a].value;
                    break;
                }
            }
	    var tmpLoadingImage = document.createElement("SPAN");
	    tmpLoadingImage.id=buttonId+"tmpImg";
	    tmpLoadingImage.innerHTML = '<img src="../images/loading-small.gif" align="left" style="margin-top:9px;margin-right:10px" />';
	    document.getElementById(buttonId).parentNode.appendChild(tmpLoadingImage);	
	    document.getElementById(buttonId).value ="Submitting your feedback .. ";
	    document.getElementById(buttonId).className = "button buttonLoading";
	
		
            jQuery.ajax({
            	type: 'POST',
                url: 'feedbackForm-ajaxprocessor.jsp',
                data: 'comment=' + " " + "&rating=" +rating+ "&buttonId=" + buttonId,
                success: function(result) {
		    document.getElementById(buttonId).parentNode.removeChild(document.getElementById(buttonId+"tmpImg"));
                    document.getElementById(buttonId).value = "Resubmit";
		    document.getElementById(buttonId).className = "button";
	    		

                },
                error:function (xhr, ajaxOptions, thrownError) {
                    jQuery(document).ready(function () {

                        jQuery('a.btn-ok, #dialog-overlay, #dialog-box').click(function () {
                            jQuery('#dialog-overlay, #dialog-box').hide();
                            return false;
                        });


                        popup('Feedback form Sucessfully Updated');

                    });
                }
            });

        }
	var globalQuactionCount = 0;
	function submitOverallEvaluation() {

        var questions = new Array()  ;

        var q1Ratings = document.getElementsByName("qr1s1");


        for (var q1 = 0; q1 < q1Ratings.length; q1++) {
            if (q1Ratings[q1].checked) {
                questions[0] = q1Ratings[q1].value;
                break;
            }
        }

        var q2Ratings = document.getElementsByName("qr2s1");


        for (var q2 = 0; q2 < q2Ratings.length; q2++) {
            if (q2Ratings[q2].checked) {
                questions[1] = q2Ratings[q2].value;
                break;
            }
        }


        var q3Ratings = document.getElementsByName("qr3s1");

        for (var q3 = 0; q3 < q2Ratings.length; q3++) {
            if (q3Ratings[q3].checked) {
                questions[2] = q3Ratings[q3].value;
                break;
            }
        }

        var q4Comment ='';
        //getting overall comments
        if(document.getElementById("qc4s1") != null){
        q4Comment = document.getElementById("qc4s1").value;
        }


        //setting the button styles
        var buttonIdOverall = "button0";
        var tmpLoadingImage = document.createElement("SPAN");
        tmpLoadingImage.id = buttonIdOverall + "tmpImg";
        tmpLoadingImage.innerHTML = '<img src="../images/loading-small.gif" align="left" style="margin-top:9px;margin-right:10px" />';
        document.getElementById(buttonIdOverall).parentNode.appendChild(tmpLoadingImage);
        document.getElementById(buttonIdOverall).value = "Submitting your feedback .. ";
        document.getElementById(buttonIdOverall).className = "button buttonLoading";


        for (var a = 0; a < 4; a++) {

            var questionNum = a + 1;

            var buttonId = questionNum + "s1";

            var comment = '';
            var rating = 0;

            if (questionNum <= 3) {
                comment = '';
                rating = questions[a];
            } else if (questionNum == 4) {
                comment = q4Comment;
                rating = 0;
            }

            jQuery.ajax({
                type: 'POST',
                url: 'feedbackForm-ajaxprocessor.jsp',
                data: 'comment=' + comment.replace(/&/g, "%26") + "&rating=" + rating + "&buttonId=" + buttonId,
                success: function(result) {
                    if (globalQuactionCount == 2) {
                        document.getElementById("button0").parentNode.innerHTML = '<input type="button" value="Resubmit" class="button" id="button0" onclick="submitOverallEvaluation()"/>';
                        globalQuactionCount = 0;
                    } else {
                        globalQuactionCount++;
                    }


                },
                error:function (xhr, ajaxOptions, thrownError) {

                }
            });

        }


    }
    </script>


</head>
<body>
<div id="dialog-overlay"></div>
<div id="dialog-box">
    <div class="dialog-content">
        <div id="dialog-message"></div>
        <a href="#" class="button">Close</a>
    </div>
</div>
<div class="pageSizer">
<div class="header"></div>
<img src="../images/logo.png"/>
    <%-- 		<h2 class="mainTitle"> <fmt:message key="wso2.con.feedback.form"/></h2> --%>


<script type="text/javascript" src="ui-validations.js"></script>

<%!private String getCommentForQuestion(ArrayList<String> answerList, int qNo, int sId) {

        if (answerList != null) {
            for (String answer : answerList) {
                String arr[] = answer.split(":");
                if (qNo == Integer.parseInt(arr[0]) && (sId == Integer.parseInt(arr[1]))) {
                    if (arr.length < 3) {
                        return "";
                    }
                    return arr[3];
                }
            }
            return "";
        }
        return "";
    }

    private String getSessionTime(int sessionId, SessionDO[] sessions) {
        for (SessionDO session : sessions) {
            if (session.getSessionId() == sessionId) {
                return session.getDuration();
            }
        }
        return "";
    }

    private ArrayList<QuestionDO> getQuestionsForDay(QuestionDO[] questions, int day) {
        ArrayList<QuestionDO> questionObjs = new ArrayList<QuestionDO>();
        if (day == 1) {
            for (QuestionDO question : questions) {
                if (question.getSessionId() >= 2 && question.getSessionId() <= 14) {
                    questionObjs.add(question);
                }
            }
        } else if (day == 2) {
            for (QuestionDO question : questions) {
                if (question.getSessionId() >= 15 && question.getSessionId() <= 27) {
                    questionObjs.add(question);
                }
            }
        } else if (day == 3) {
            for (QuestionDO question : questions) {
                if (question.getSessionId() >= 28 && question.getSessionId() <= 39) {
                    questionObjs.add(question);
                }
            }
        }
        return questionObjs;
    }

    private ArrayList<QuestionDO> getOverallQuestions(QuestionDO[] questions) {
        ArrayList<QuestionDO> questionObjs = new ArrayList<QuestionDO>();
        for (QuestionDO question : questions) {
            if (question.getSessionId() == 1) {
                questionObjs.add(question);
            }
        }

        return questionObjs;
    }

    private String getRatingForQuestion(ArrayList<String> answerList, int qNo, int sId) {
        if (answerList != null) {
            for (String answer : answerList) {
                String arr[] = answer.split(":");
                if (qNo == Integer.parseInt(arr[0]) && (sId == Integer.parseInt(arr[1]))) {
                    return arr[2];
                }
            }
            return "";
        }
        return "";
    }%>

<%
    //	String user = request.getParameter("user");
        String user = (String) session.getAttribute("user");
        if (user == null) {
%>
<script type="text/javascript">
    location.href = "index.jsp";
</script>
<%
    }
        String updated = request.getParameter("updated");
        String propertyValue = "";
        String propertyValueRating = "";
        // 					int numberOfQuestions = 48;
        boolean isUserFilled = false;
        ArrayList<String> answerList = null;
        int userId = -1;
        QuestionDO[] questions = null;
        SessionDO[] sessions = null;
        ServletContext context = getServletContext();

        String jdbcURL = context.getInitParameter("jdbcURL");
        String username = context.getInitParameter("username");
        String password = context.getInitParameter("password");
        String welcomeStatus ="";
        FeedbackDO[] answers = null;
        try {
            FeedbackAppDAO.initialize(jdbcURL, username, password);

            FeedbackAppDAO dao = FeedbackAppDAO.getInstance();
            questions = dao.getQuestions();
            sessions = dao.getSessions();

            userId = dao.getUser(user);
            isUserFilled = (userId != -1);
            welcomeStatus = isUserFilled?" back ":"";
            answerList = new ArrayList<String>();
            if (isUserFilled) {
                answers = dao.getFeedbacks(user);
                for (int i = 0; i < answers.length; i++) {
                    answerList.add(answers[i].getqId() + ":" + answers[i].getSessionId() +
                                   ":" + answers[i].getRating() + ":" +
                                   answers[i].getComment());

                }
            }
            if (updated != null) {
%>
<script type="text/javascript">
    jQuery(document).ready(function () {

        jQuery('a.btn-ok, #dialog-overlay, #dialog-box').click(function () {
            jQuery('#dialog-overlay, #dialog-box').hide();
            return false;
        });


        popup('Feedback form Sucessfully Updated');

    });
</script>
<%
    }
        } catch (Exception e) {
%>
<script type="text/javascript">
    location.href = "timeout_error.jsp";
</script>
<%
    }
%>

<div id="middle">
<label class="logoutButton" >welcome <%=welcomeStatus%> <%=user%>  <a href="logout.jsp">Logout</a> </label>

<form method="POST">
<input type="hidden" id="user"
       name="user" value="<%=user%>"/> <input type="hidden"
                                              id="isUserFilled" name="isUserFilled" value="<%=isUserFilled%>"/>

<div id="workArea">
<h2><fmt:message key="overall.feedback.title"/></h2>
<div class="loginBox overrideClass">

<table id="overall" class="styledLeft noBorders" cellspacing="0" width="100%">

<tr id="overallQs">
    <td colspan="2">
        <table class="formTable">


            <%
                ArrayList<QuestionDO> qs = getOverallQuestions(questions);
                    for (QuestionDO q : qs) {

                        String questionText = q.getText();
                        String sessionTime = getSessionTime(q.getSessionId(), sessions);
            %>
            <tr>
                <td class="leftCell"><%=questionText%>
                </td>
                <td style="text-align:right"><%
                    int questionType = q.getType();
                            String inputIdC = "qc" + q.getQuestionId() + "s" + q.getSessionId();
                            String inputIdR = "qr" + q.getQuestionId() + "s" + q.getSessionId();

                            propertyValueRating =
                                                  getRatingForQuestion(answerList, q.getQuestionId(),
                                                                       q.getSessionId());

                            propertyValue =
                                            getCommentForQuestion(answerList, q.getQuestionId(),
                                                                  q.getSessionId());

                            if (questionType == 1) {//Comment
                %>
                    <textarea  rows="3" id="<%=inputIdC%>"
                              name="<%=inputIdC%>"><%=(propertyValue != null && !propertyValue.equals("N/A"))
                                                                                         ? propertyValue
                                                                                         : ""%>
                    </textarea>
                    <nobr>
                        <%
                            } else if (questionType == 4) { //rating
                        %>

                        <%
                            if ("1".equals(propertyValueRating)) {
                        %>
                        <label>(Poor)<input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="1"
                                            checked/>1</label>
                        <%
                            } else {
                        %>
                        <label>(Poor)<input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio"
                                            value="1"/>1</label>
                        <%
                            }
                        %>
                        <%
                            if ("2".equals(propertyValueRating)) {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="2" checked/>2</label>
                        <%
                            } else {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="2"/>2</label>
                        <%
                            }
                        %>
                        <%
                            if ("3".equals(propertyValueRating)) {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="3" checked/>3</label>
                        <%
                            } else {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio"  value="3"/>3</label>
                        <%
                            }
                        %>
                        <%
                            if ("4".equals(propertyValueRating)) {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="4" checked/>4</label>
                        <%
                            } else {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="4"/>4</label>
                        <%
                            }
                        %>
                        <%
                            if ("5".equals(propertyValueRating)) {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="5" checked/>5(Excellent)</label>
                        <%
                            } else {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio"
                                      value="5"/>5(Excellent)</label>
                        <%
                            }
                        %></nobr>
                    <%
                        } else if (questionType == 0) { //yes/no
                    %>
                    <%
                        if ("Yes".equals(propertyValue)) {
                    %>
                    <label><input id="<%=inputIdC%>" name="<%=inputIdC%>" type="radio" value="Yes" checked/>Yes</label>
                    <%
                        } else {
                    %>
                    <label><input id="<%=inputIdC%>" name="<%=inputIdC%>" type="radio" value="Yes"/>Yes</label>
                    <%
                        }
                    %>
                    <%
                        if ("No".equals(propertyValue)) {
                    %>
                    <label><input id="<%=inputIdC%>" name="<%=inputIdC%>" type="radio" value="No"
                                  checked/>No</label>
                    <%
                        } else {
                    %>
                    <label><input id="<%=inputIdC%>" name="<%=inputIdC%>" type="radio" value="No"/>No</label>
                    <%
                        }
                    %><%
                        } else if (questionType == 3) { //Comment/Rating
                    %>

                    <nobr>
                        <%
                            if ("1".equals(propertyValueRating)) {
                        %>
                        <label>(Poor)<input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="1"
                                            checked/>1</label>
                        <%
                            } else {
                        %>
                        <label>(Poor)<input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio"
                                            value="1"/>1</label>
                        <%
                            }
                        %>
                        <%
                            if ("2".equals(propertyValueRating)) {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="2" checked/>2</label>
                        <%
                            } else {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="2"/>2</label>
                        <%
                            }
                        %>
                        <%
                            if ("3".equals(propertyValueRating)) {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="3" checked/>3</label>
                        <%
                            } else {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio"  value="3"/>3</label>
                        <%
                            }
                        %>
                        <%
                            if ("4".equals(propertyValueRating)) {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="4" checked/>4</label>
                        <%
                            } else {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="4"/>4</label>
                        <%
                            }
                        %>
                        <%
                            if ("5".equals(propertyValueRating)) {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio" value="5" checked/>5(Excellent)</label>
                        <%
                            } else {
                        %>
                        <label><input id="<%=inputIdR%>" name="<%=inputIdR%>" type="radio"
                                      value="5"/>5(Excellent)</label>
                        <%
                            }
                        %></nobr>
                    <%%> <textarea  rows="3" id="<%=inputIdC%>"
                                 name="<%=inputIdC%>">
                        <%=(propertyValue != null && !propertyValue.equals("N/A"))
                                                                                         ? propertyValue
                                                                                         : ""%>
                    </textarea>
                </td>
                <%
                    }
                        }
                %>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td>
        <div class="buttonRow"><input type="button" value="Submit" class="button" id="button0" onclick="submitOverallEvaluation()"/></div>
    </td>
</tr>
</table>
</div>
<!-- ending the loginBox -->
<h2><fmt:message key="title2"/></h2>

<div class="loginBox overrideClass" style="margin-bottom:80px;">

<%
        String[] comments = new String[40];
		String[] radio1Value = new String[40];
		String[] radio2Value = new String[40];
		String[] radio3Value = new String[40];
		String[] radio4Value = new String[40];
		String[] radio5Value = new String[40];


        if (userId == -1) {
            //initialize empty strings
            for (int i = 2; i < 40; i++) {
				comments[i] = "";
            }
            radio1Value = comments;
            radio2Value = comments;
            radio3Value = comments;
            radio4Value = comments;
            radio5Value = comments;
        } else {
            for (FeedbackDO fdbk : answers) {
                int i = fdbk.getSessionId();
                comments[i] = ( fdbk.getComment() == null || fdbk.getComment().equals("N/A")) ? "" : fdbk.getComment();
                int rating = fdbk.getRating();
                switch (rating) {
                    case 1 : radio1Value[i] = "checked"; radio2Value[i] = ""; radio3Value[i] = ""; radio4Value[i] = ""; radio5Value[i] = "";break;
                    case 2 : radio1Value[i] = ""; radio2Value[i] = "checked"; radio3Value[i] = ""; radio4Value[i] = ""; radio5Value[i] = "";break;
                    case 3 : radio1Value[i] = ""; radio2Value[i] = ""; radio3Value[i] = "checked"; radio4Value[i] = ""; radio5Value[i] = "";break;
                    case 4 : radio1Value[i] = ""; radio2Value[i] = ""; radio3Value[i] = ""; radio4Value[i] = "checked"; radio5Value[i] = "";break;
                    case 5 : radio1Value[i] = ""; radio2Value[i] = ""; radio3Value[i] = ""; radio4Value[i] = ""; radio5Value[i] = "checked";break;
                    default : radio1Value[i] = ""; radio2Value[i] = ""; radio3Value[i] = ""; radio4Value[i] = ""; radio5Value[i] = "";break;
                }
            }

        }
%>
<div class="date togglebleTitle"><a name="day2">
</a><span>Conference - Day 1</span> Tuesday, September 13
</div>
<table cellspacing="0" cellpadding="0" class="sessions" style="display:none">
    <tbody>
    <tr class="main">
        <td class="time">09:30</td>
        <td colspan="2">
            <div class="topic"><a class="session-title">WSO2: Disrupting the middleware industry</a>
                <a>Dr Sanjiva Weerawarana(WSO2)</a>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s2" name="qr1s2" type="radio" value="1"<%=radio1Value[2]%> />1</label>
                    <label><input id="qr1s2" name="qr1s2" type="radio" value="2" <%=radio2Value[2]%> />2</label>
                    <label><input id="qr1s2" name="qr1s2" type="radio" value="3" <%=radio3Value[2]%> />3</label>
                    <label><input id="qr1s2" name="qr1s2" type="radio" value="4" <%=radio4Value[2]%>/>4</label>
                    <label><input id="qr1s2" name="qr1s2" type="radio" value="5" <%=radio5Value[2]%>/>5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s2" name="qc1s2"><%=comments[2]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s2" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr class="main">
        <td class="time">10:00</td>
        <td colspan="2">
            <div class="topic"><a class="session-title">Keynote: IBM Global Technology Outlook - 2011</a>
                <a>Dr. C Mohan</a>,
                <span class="company">Fellow, <strong>IBM Research</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s3" name="qr1s3" type="radio" value="1" <%=radio1Value[3]%>/>1</label>
                    <label><input id="qr1s3" name="qr1s3" type="radio" value="2" <%=radio2Value[3]%>/>2</label>
                    <label><input id="qr1s3" name="qr1s3" type="radio" value="3" <%=radio3Value[3]%>/>3</label>
                    <label><input id="qr1s3" name="qr1s3" type="radio" value="4" <%=radio4Value[3]%>/>4</label>
                    <label><input id="qr1s3" name="qr1s3" type="radio" value="5" <%=radio5Value[3]%>/>5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s3" name="qc1s3"><%=comments[3]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s3" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr class="tracks">
        <td class="time">&nbsp;</td>
        <td class="track1">Track 01</td>
        <td>Track 02</td>
    </tr>
    <tr>
        <td class="time">11:30</td>
        <td class="track-01 enterprise">
            <div class="topic"><a class="session-title">Using WSO2 ESB with SAP ERP (Retail)</a><a
                    >Harsha Senanayake</a>,<span class="company">Head of Enterprise Solutions, <strong>John
                Keells Holdings</strong>.</span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s4" name="qr1s4" type="radio" value="1" <%=radio1Value[4]%>/>1</label>
                    <label><input id="qr1s4" name="qr1s4" type="radio" value="2" <%=radio2Value[4]%>/>2</label>
                    <label><input id="qr1s4" name="qr1s4" type="radio" value="3" <%=radio3Value[4]%>/>3</label>
                    <label><input id="qr1s4" name="qr1s4" type="radio" value="4" <%=radio4Value[4]%>/>4</label>
                    <label><input id="qr1s4" name="qr1s4" type="radio" value="5" <%=radio5Value[4]%>/>5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s4" name="qc1s4"><%=comments[4]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s4" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
        <td class="track-02 enterprise">
            <div class="topic"><a class="session-title">Focusing your effort on strategy and vision, not
                infrastructure</a><a>Philippe Camus</a>,<span
                    class="company">Enterprise SOA Architect, <strong>Atlantic Lottery Corporation</strong></span></div>

            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s5" name="qr1s5" type="radio" value="1"<%=radio1Value[5]%>/>1</label>
                    <label><input id="qr1s5" name="qr1s5" type="radio" value="2" <%=radio2Value[5]%>/>2</label>
                    <label><input id="qr1s5" name="qr1s5" type="radio" value="3" <%=radio3Value[5]%>/>3</label>
                    <label><input id="qr1s5" name="qr1s5" type="radio" value="4" <%=radio4Value[5]%>/>4</label>
                    <label><input id="qr1s5" name="qr1s5" type="radio" value="5" <%=radio5Value[5]%>/>5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s5" name="qc1s5"><%=comments[5]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s5" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
    </tr>
    <tr>
        <td class="time">12:15</td>
        <td class="track-01 enterprise">
            <div class="topic"><a class="session-title">Delivering the Goods: Automated Quote to Cash</a><a
                    >Brad Svee</a>,<span class="company">Sr. Manager of IT Development &amp; Engineering</span>
            </div>
             <table>
            <tr><td><p>
                <label>(Poor)<input id="qr1s6" name="qr1s6" type="radio" value="1"  <%=radio1Value[6]%> />1</label>
                <label><input id="qr1s6" name="qr1s6" type="radio" value="2" <%=radio2Value[6]%> />2</label>
                <label><input id="qr1s6" name="qr1s6" type="radio" value="3" <%=radio3Value[6]%> />3</label>
                <label><input id="qr1s6" name="qr1s6" type="radio" value="4" <%=radio4Value[6]%>/>4</label>
                <label><input id="qr1s6" name="qr1s6" type="radio" value="5" <%=radio5Value[6]%>/>5(Excellent)</label>
            </p></td></tr>
            <tr><td><textarea  rows="2" id="qc1s6" name="qc1s6"><%=comments[6]%></textarea></td></tr>
            <tr><td><input type="button" value="Submit" class="button" id="1s6" onclick="submitFeedBack(this.id)"/></td></tr>
        </table>
        </td>
        <td class="track-02 enterprise">
            <div class="topic"><a class="session-title">Open Source Adoption in the Enterprise</a><a
                    >Prajod Vettiyattil</a>,<span class="company">Lead Architect, <strong>Wipro
                Technologies</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s7" name="qr1s7" type="radio" value="1" <%=radio1Value[7]%> />1</label>
                    <label><input id="qr1s7" name="qr1s7" type="radio" value="2" <%=radio2Value[7]%> />2</label>
                    <label><input id="qr1s7" name="qr1s7" type="radio" value="3" <%=radio3Value[7]%> />3</label>
                    <label><input id="qr1s7" name="qr1s7" type="radio" value="4" <%=radio4Value[7]%> />4</label>
                    <label><input id="qr1s7" name="qr1s7" type="radio" value="5" <%=radio5Value[7]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s7" name="qc1s7"><%=comments[7]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s7" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
    </tr>
    <tr>
        <td class="time">14:00</td>
        <td class="track-01 soaplatform">
            <div class="topic"><a class="session-title">ESB: The Swiss Army Knife of SOA </a><a
                   >Hiranya Jayathilake</a>,<span class="company">Associate Technical Lead, <strong>WSO2</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s8" name="qr1s8" type="radio" value="1" <%=radio1Value[8]%>/>1</label>
                    <label><input id="qr1s8" name="qr1s8" type="radio" value="2" <%=radio2Value[8]%> />2</label>
                    <label><input id="qr1s8" name="qr1s8" type="radio"  value="3" <%=radio3Value[8]%> />3</label>
                    <label><input id="qr1s8" name="qr1s8" type="radio" value="4"  <%=radio4Value[8]%>/>4</label>
                    <label><input id="qr1s8" name="qr1s8" type="radio" value="5" <%=radio5Value[8]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s8" name="qc1s8"><%=comments[8]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s8" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
        <td class="track-02 cloud">
            <div class="topic"><a class="session-title">BSS Platform-as-a-Service for Communications: Utilizing WSO2
                Middleware</a><a>Ken Anderson</a>,<span
                    class="company">CTO, <strong>Datatel Solutions</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s9" name="qr1s9" type="radio" value="1" <%=radio1Value[9]%>/>1</label>
                    <label><input id="qr1s9" name="qr1s9" type="radio" value="2" <%=radio2Value[9]%> />2</label>
                    <label><input id="qr1s9" name="qr1s9" type="radio"  value="3" <%=radio3Value[9]%> />3</label>
                    <label><input id="qr1s9" name="qr1s9" type="radio" value="4"  <%=radio4Value[9]%>/>4</label>
                    <label><input id="qr1s9" name="qr1s9" type="radio" value="5" <%=radio5Value[9]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s9" name="qc1s9"><%=comments[9]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s9" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr>
        <td class="time">14:45</td>
        <td class="track-01 soaplatform">
            <div class="topic"><a class="session-title">Data in your SOA: From SQL to NoSQL and Beyond </a><a
                    >Sumedha Rubasighe</a>,<span class="company">Architect and Senior Manager, <strong>WSO2</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s10" name="qr1s10" type="radio" value="1" <%=radio1Value[10]%>/>1</label>
                    <label><input id="qr1s10" name="qr1s10" type="radio" value="2" <%=radio2Value[10]%> />2</label>
                    <label><input id="qr1s10" name="qr1s10" type="radio"  value="3" <%=radio3Value[10]%> />3</label>
                    <label><input id="qr1s10" name="qr1s10" type="radio" value="4"  <%=radio4Value[10]%>/>4</label>
                    <label><input id="qr1s10" name="qr1s10" type="radio" value="5" <%=radio5Value[10]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s10" name="qc1s10"><%=comments[10]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s10" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
        <td class="track-02 cloud">
            <div class="topic"><a class="session-title">WSO2 at Connected Car</a><a>Andreas Wichmann, Ph. D,</a><span
                    class="company">Systems Architect, <strong>T-Systems</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s11" name="qr1s11" type="radio" value="1" <%=radio1Value[11]%>/>1</label>
                    <label><input id="qr1s11" name="qr1s11" type="radio" value="2" <%=radio2Value[11]%> />2</label>
                    <label><input id="qr1s11" name="qr1s11" type="radio"  value="3" <%=radio3Value[11]%> />3</label>
                    <label><input id="qr1s11" name="qr1s11" type="radio" value="4"  <%=radio4Value[11]%>/>4</label>
                    <label><input id="qr1s11" name="qr1s11" type="radio" value="5" <%=radio5Value[11]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s11" name="qc1s11"><%=comments[11]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s11" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
    </tr>
    <tr>
        <td class="time">16:00</td>
        <td class="track-01 soaplatform">
            <div class="topic"><a class="session-title">Open Service Federation Framework</a><a
                    >Dipanjan Sengupta</a>,<span class="company">Principal Architect, <strong>Cognizant
                Technology Solutions</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s12" name="qr1s12" type="radio" value="1" <%=radio1Value[12]%>/>1</label>
                    <label><input id="qr1s12" name="qr1s12" type="radio" value="2" <%=radio2Value[12]%> />2</label>
                    <label><input id="qr1s12" name="qr1s12" type="radio"  value="3" <%=radio3Value[12]%> />3</label>
                    <label><input id="qr1s12" name="qr1s12" type="radio" value="4"  <%=radio4Value[12]%>/>4</label>
                    <label><input id="qr1s12" name="qr1s12" type="radio" value="5" <%=radio5Value[12]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s12" name="qc1s12"><%=comments[12]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s12" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
        <td class="track-02 soaplatform">
            <div class="topic"><a class="session-title">Users: The SOA Last Mile</a><a
                    >Nuwan Bandara</a>,<span class="company">Senior Software Engineer and Product Manager, <strong>WSO2</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s13" name="qr1s13" type="radio" value="1" <%=radio1Value[13]%>/>1</label>
                    <label><input id="qr1s13" name="qr1s13" type="radio" value="2" <%=radio2Value[13]%> />2</label>
                    <label><input id="qr1s13" name="qr1s13" type="radio"  value="3" <%=radio3Value[13]%> />3</label>
                    <label><input id="qr1s13" name="qr1s13" type="radio" value="4"  <%=radio4Value[13]%>/>4</label>
                    <label><input id="qr1s13" name="qr1s13" type="radio" value="5" <%=radio5Value[13]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s13" name="qc1s13"><%=comments[13]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s13" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr class="main">
        <td class="time">16:45</td>
        <td colspan="2">
            <div class="topic"><span class="session-title">Panel: <strong>Data, data everywhere: big, small, private,
                shared, public and more</strong></span><span class="company">Moderated by Dr. Srinath Perera</span>

                <p>Panelists: Dr. C. Mohan, Sumedha Rubasinghe, Gregor Hohpe</p>

                </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s14" name="qr1s14" type="radio" value="1" <%=radio1Value[14]%>/>1</label>
                    <label><input id="qr1s14" name="qr1s14" type="radio" value="2" <%=radio2Value[14]%> />2</label>
                    <label><input id="qr1s14" name="qr1s14" type="radio"  value="3" <%=radio3Value[14]%> />3</label>
                    <label><input id="qr1s14" name="qr1s14" type="radio" value="4"  <%=radio4Value[14]%>/>4</label>
                    <label><input id="qr1s14" name="qr1s14" type="radio" value="5" <%=radio5Value[14]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s14" name="qc1s14"><%=comments[14]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s14" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    </tbody>
</table>


<div class="date togglebleTitle">
	<span>Conference - Day 2</span> Wednesday, September 14
</div>
<table cellspacing="0" cellpadding="0" class="sessions" style="display:none">
    <tbody>
    <tr class="main">
        <td class="time">9:00</td>
        <td colspan="2">
            <div class="topic"><a class="session-title">Keynote:Service Orientation &ndash; Why is it good for your
                business</a><a>Sastry Malladi</a>,<span
                    class="company">Distinguished Architect, <strong>eBay</strong></span></div>
             <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s15" name="qr1s15" type="radio" value="1" <%=radio1Value[15]%>/>1</label>
                    <label><input id="qr1s15" name="qr1s15" type="radio" value="2" <%=radio2Value[15]%> />2</label>
                    <label><input id="qr1s15" name="qr1s15" type="radio"  value="3" <%=radio3Value[15]%> />3</label>
                    <label><input id="qr1s15" name="qr1s15" type="radio" value="4"  <%=radio4Value[15]%>/>4</label>
                    <label><input id="qr1s15" name="qr1s15" type="radio" value="5" <%=radio5Value[15]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s15" name="qc1s15"><%=comments[15]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s15" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr class="main">
        <td class="time">10:00</td>
        <td colspan="2">
            <div class="topic"><span class="session-title">Fireside chat with Sastry Malladi</span>Hosted by Rebecca
                Hurst
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s16" name="qr1s16" type="radio" value="1" <%=radio1Value[16]%>/>1</label>
                    <label><input id="qr1s16" name="qr1s16" type="radio" value="2" <%=radio2Value[16]%> />2</label>
                    <label><input id="qr1s16" name="qr1s16" type="radio"  value="3" <%=radio3Value[16]%> />3</label>
                    <label><input id="qr1s16" name="qr1s16" type="radio" value="4"  <%=radio4Value[16]%>/>4</label>
                    <label><input id="qr1s16" name="qr1s16" type="radio" value="5" <%=radio5Value[16]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s16" name="qc1s16"><%=comments[16]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s16" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr class="tracks">
        <td class="time">&nbsp;</td>
        <td class="track1">Track 01</td>
        <td>Track 02</td>
    </tr>
    <tr>
        <td class="time">11:00</td>
        <td class="track-01 cloud">
            <div class="topic"><a class="session-title">Open Source Middleware for the Cloud: WSO2 Stratos</a><a
                    >Afkham Azeez</a>,<span class="company">Director of Architecture, <strong>WSO2</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s17" name="qr1s17" type="radio" value="1" <%=radio1Value[17]%>/>1</label>
                    <label><input id="qr1s17" name="qr1s17" type="radio" value="2" <%=radio2Value[17]%> />2</label>
                    <label><input id="qr1s17" name="qr1s17" type="radio"  value="3" <%=radio3Value[17]%> />3</label>
                    <label><input id="qr1s17" name="qr1s17" type="radio" value="4"  <%=radio4Value[17]%>/>4</label>
                    <label><input id="qr1s17" name="qr1s17" type="radio" value="5" <%=radio5Value[17]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s17" name="qc1s17"><%=comments[17]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s17" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
        <td class="track-02 egov">
            <div class="topic"><a class="session-title">Electronic claim flow platform for a government orchestrated
                process</a><a>Guillaume Devianne</a>,<span
                    class="company">Independent Consultant</span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s18" name="qr1s18" type="radio" value="1" <%=radio1Value[18]%>/>1</label>
                    <label><input id="qr1s18" name="qr1s18" type="radio" value="2" <%=radio2Value[18]%> />2</label>
                    <label><input id="qr1s18" name="qr1s18" type="radio"  value="3" <%=radio3Value[18]%> />3</label>
                    <label><input id="qr1s18" name="qr1s18" type="radio" value="4"  <%=radio4Value[18]%>/>4</label>
                    <label><input id="qr1s18" name="qr1s18" type="radio" value="5" <%=radio5Value[18]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s18" name="qc1s18"><%=comments[18]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s18" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
    </tr>
    <tr>
        <td class="time">11:45</td>
        <td class="track-01 cloud">
            <div class="topic"><a class="session-title">Building Cool Applications with WSO2 StratosLive</a><a
                    >Selvaratnam Uthaiyashankar,</a><br><span
                    class="company">Senior Software Architect,<strong>WSO2</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s19" name="qr1s19" type="radio" value="1" <%=radio1Value[19]%>/>1</label>
                    <label><input id="qr1s19" name="qr1s19" type="radio" value="2" <%=radio2Value[19]%> />2</label>
                    <label><input id="qr1s19" name="qr1s19" type="radio"  value="3" <%=radio3Value[19]%> />3</label>
                    <label><input id="qr1s19" name="qr1s19" type="radio" value="4"  <%=radio4Value[19]%>/>4</label>
                    <label><input id="qr1s19" name="qr1s19" type="radio" value="5" <%=radio5Value[19]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s19" name="qc1s19"><%=comments[19]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s19" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
        <td class="track-02 egov">
            <div class="topic"><a class="session-title">SOA for Citizen Centric Government Service Delivery of Sri
                Lanka</a><a>Sanjaya Karunasena</a>,<span
                    class="company">CTO, <strong>ICTA</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s20" name="qr1s20" type="radio" value="1" <%=radio1Value[20]%>/>1</label>
                    <label><input id="qr1s20" name="qr1s20" type="radio" value="2" <%=radio2Value[20]%> />2</label>
                    <label><input id="qr1s20" name="qr1s20" type="radio"  value="3" <%=radio3Value[20]%> />3</label>
                    <label><input id="qr1s20" name="qr1s20" type="radio" value="4"  <%=radio4Value[20]%>/>4</label>
                    <label><input id="qr1s20" name="qr1s20" type="radio" value="5" <%=radio5Value[20]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s20" name="qc1s20"><%=comments[20]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s20" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
    </tr>
    <tr>
        <td class="time">13:30</td>
        <td class="track-01 soaplatform">
            <div class="topic"><a class="session-title">SOA Governance with WSO2 Products</a><a
                    >Senaka Fernando</a>,<span class="company">Associate Technical Lead, <strong>WSO2</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s21" name="qr1s21" type="radio" value="1" <%=radio1Value[21]%>/>1</label>
                    <label><input id="qr1s21" name="qr1s21" type="radio" value="2" <%=radio2Value[21]%> />2</label>
                    <label><input id="qr1s21" name="qr1s21" type="radio"  value="3" <%=radio3Value[21]%> />3</label>
                    <label><input id="qr1s21" name="qr1s21" type="radio" value="4"  <%=radio4Value[21]%>/>4</label>
                    <label><input id="qr1s21" name="qr1s21" type="radio" value="5" <%=radio5Value[21]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s21" name="qc1s21"><%=comments[21]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s21" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
        <td class="track-02 banking">
            <div class="topic"><a class="session-title">Open Source Adoption in a Mexican Bank</a><a
                    >Nelson Raimond</a>,<span class="company">IT Manager, <strong>SHF</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s22" name="qr1s22" type="radio" value="1" <%=radio1Value[22]%>/>1</label>
                    <label><input id="qr1s22" name="qr1s22" type="radio" value="2" <%=radio2Value[22]%> />2</label>
                    <label><input id="qr1s22" name="qr1s22" type="radio"  value="3" <%=radio3Value[22]%> />3</label>
                    <label><input id="qr1s22" name="qr1s22" type="radio" value="4"  <%=radio4Value[22]%>/>4</label>
                    <label><input id="qr1s22" name="qr1s22" type="radio" value="5" <%=radio5Value[22]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s22" name="qc1s22"><%=comments[22]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s22" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
    </tr>
    <tr>
        <td class="time">14:15</td>
        <td class="track-01 soaplatform">
            <div class="topic"><a class="session-title">Business Activity Monitoring in your SOA Environment</a><a
                    >Tharindu Mathew</a>,<span class="company">Senior Software Engineer and Product Manager, <strong>WSO2</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s23" name="qr1s23" type="radio" value="1" <%=radio1Value[23]%>/>1</label>
                    <label><input id="qr1s23" name="qr1s23" type="radio" value="2" <%=radio2Value[23]%> />2</label>
                    <label><input id="qr1s23" name="qr1s23" type="radio"  value="3" <%=radio3Value[23]%> />3</label>
                    <label><input id="qr1s23" name="qr1s23" type="radio" value="4"  <%=radio4Value[23]%>/>4</label>
                    <label><input id="qr1s23" name="qr1s23" type="radio" value="5" <%=radio5Value[23]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s23" name="qc1s23"><%=comments[23]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s23" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
        <td class="track-02 banking">
            <div class="topic"><a class="session-title">WSO2 in Action in Alfa Bank</a>Dmitry Lukyanov,<br><span
                    class="company">Head of Integration Solutions Dept, <strong>Alfa-Bank Ukraine</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s24" name="qr1s24" type="radio" value="1" <%=radio1Value[24]%>/>1</label>
                    <label><input id="qr1s24" name="qr1s24" type="radio" value="2" <%=radio2Value[24]%> />2</label>
                    <label><input id="qr1s24" name="qr1s24" type="radio"  value="3" <%=radio3Value[24]%> />3</label>
                    <label><input id="qr1s24" name="qr1s24" type="radio" value="4"  <%=radio4Value[24]%>/>4</label>
                    <label><input id="qr1s24" name="qr1s24" type="radio" value="5" <%=radio5Value[24]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s24" name="qc1s24"><%=comments[24]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s24" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr>
        <td class="time">15:30</td>
        <td class="track-01 enterprise">
            <div class="topic"><a class="session-title">Building a Mobile POS Solution with WSO2 Carbon and Apple iPod
                Touch</a><a>Thilanka Kiriporuwa</a>, <br><span
                    class="company">Head of Human Resources and Operations, <strong>Odel</strong></span></div>
             <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s25" name="qr1s25" type="radio" value="1" <%=radio1Value[25]%>/>1</label>
                    <label><input id="qr1s25" name="qr1s25" type="radio" value="2" <%=radio2Value[25]%> />2</label>
                    <label><input id="qr1s25" name="qr1s25" type="radio"  value="3" <%=radio3Value[25]%> />3</label>
                    <label><input id="qr1s25" name="qr1s25" type="radio" value="4"  <%=radio4Value[25]%>/>4</label>
                    <label><input id="qr1s25" name="qr1s25" type="radio" value="5" <%=radio5Value[25]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s25" name="qc1s25"><%=comments[25]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s25" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
        <td class="track-02 soaplatform">
            <div class="topic"><a class="session-title">Security in practice</a><a
                    >Prabath Siriwardena</a>,<span class="company">Architect &amp; Senior Manager &ndash;
                Carbon Platform &amp; Security, <strong>WSO2</strong></span></div>
              <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s26" name="qr1s26" type="radio" value="1" <%=radio1Value[26]%>/>1</label>
                    <label><input id="qr1s26" name="qr1s26" type="radio" value="2" <%=radio2Value[26]%> />2</label>
                    <label><input id="qr1s26" name="qr1s26" type="radio"  value="3" <%=radio3Value[26]%> />3</label>
                    <label><input id="qr1s26" name="qr1s26" type="radio" value="4"  <%=radio4Value[26]%>/>4</label>
                    <label><input id="qr1s26" name="qr1s26" type="radio" value="5" <%=radio5Value[26]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s26" name="qc1s26"><%=comments[26]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s26" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr class="main">
        <td class="time">16:45</td>
        <td colspan="2">
            <div class="topic"><span class="session-title">Panel: <strong>Cloud and SOA: The good, the bad and the
                ugly</strong></span>Moderated by Paul Fremantle<span class="company">Panelists: Sastry Malladi, Afkham Azeez,  Brad Svee, Narendra Nathma</span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s27" name="qr1s27" type="radio" value="1" <%=radio1Value[27]%>/>1</label>
                    <label><input id="qr1s27" name="qr1s27" type="radio" value="2" <%=radio2Value[27]%> />2</label>
                    <label><input id="qr1s27" name="qr1s27" type="radio"  value="3" <%=radio3Value[27]%> />3</label>
                    <label><input id="qr1s27" name="qr1s27" type="radio" value="4"  <%=radio4Value[27]%>/>4</label>
                    <label><input id="qr1s27" name="qr1s27" type="radio" value="5" <%=radio5Value[27]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s27" name="qc1s27"><%=comments[27]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s27" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    </tbody>
</table>

<div class="date togglebleTitle">
	<span>Conference - Day 3</span> Thursday, September 15
</div>
<table cellspacing="0" cellpadding="0" class="sessions" style="display:none;">
    <tbody>
    <tr class="main">
        <td class="time">09:00</td>
        <td colspan="2">
            <div class="topic"><a class="session-title">Keynote: Enterprise Integration Patterns: Past, Present and
                Future</a><a>Gregor Hohpe</a>,<span class="company"><strong>Google</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s28" name="qr1s28" type="radio" value="1" <%=radio1Value[28]%>/>1</label>
                    <label><input id="qr1s28" name="qr1s28" type="radio" value="2" <%=radio2Value[28]%> />2</label>
                    <label><input id="qr1s28" name="qr1s28" type="radio"  value="3" <%=radio3Value[28]%> />3</label>
                    <label><input id="qr1s28" name="qr1s28" type="radio" value="4"  <%=radio4Value[28]%>/>4</label>
                    <label><input id="qr1s28" name="qr1s28" type="radio" value="5" <%=radio5Value[28]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s28" name="qc1s28"><%=comments[28]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s28" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr class="main">
        <td class="time">9:45</td>
        <td colspan="2">
            <div class="topic"><span
                    class="session-title">Keynote:SOA &amp; Beyond: Using Open Source Technologies</span><a
                    href="/events/wso2con-2011-colombo/speakers#narendra">Narendra Nathmal</a>,<span class="company">Chief Architect, <strong>Advanced
                SOA Center of Excellence Cognizant Technologies</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s29" name="qr1s29" type="radio" value="1" <%=radio1Value[29]%>/>1</label>
                    <label><input id="qr1s29" name="qr1s29" type="radio" value="2" <%=radio2Value[29]%> />2</label>
                    <label><input id="qr1s29" name="qr1s29" type="radio"  value="3" <%=radio3Value[29]%> />3</label>
                    <label><input id="qr1s29" name="qr1s29" type="radio" value="4"  <%=radio4Value[29]%>/>4</label>
                    <label><input id="qr1s29" name="qr1s29" type="radio" value="5" <%=radio5Value[29]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s29" name="qc1s29"><%=comments[29]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s29" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr class="tracks">
        <td class="time">&nbsp;</td>
        <td class="track1">Track 01</td>
        <td>Track 02</td>
    </tr>
    <tr>
        <td class="time">11:00</td>
        <td class="track-01 egov">
            <div class="topic"><a class="session-title">Using WSO2 Products in e-Government Infrastructures</a><a
                    >Maria Belkina</a>,<span class="company">Project Manager, <strong>Saint-Petersburg
                Information and Analytics Center</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s30" name="qr1s30" type="radio" value="1" <%=radio1Value[30]%>/>1</label>
                    <label><input id="qr1s30" name="qr1s30" type="radio" value="2" <%=radio2Value[30]%> />2</label>
                    <label><input id="qr1s30" name="qr1s30" type="radio"  value="3" <%=radio3Value[30]%> />3</label>
                    <label><input id="qr1s30" name="qr1s30" type="radio" value="4"  <%=radio4Value[30]%>/>4</label>
                    <label><input id="qr1s30" name="qr1s30" type="radio" value="5" <%=radio5Value[30]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s30" name="qc1s30"><%=comments[30]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s30" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
        <td class="track-02 soaplatform">
            <div class="topic"><a class="session-title">Develop, Test and Deploy your SOA Application through a Single
                Platform</a><a>Chathuri Wimalasena</a>,<span
                    class="company">Senior Software Engineer, <strong>WSO2</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s31" name="qr1s31" type="radio" value="1" <%=radio1Value[31]%>/>1</label>
                    <label><input id="qr1s31" name="qr1s31" type="radio" value="2" <%=radio2Value[31]%> />2</label>
                    <label><input id="qr1s31" name="qr1s31" type="radio"  value="3" <%=radio3Value[31]%> />3</label>
                    <label><input id="qr1s31" name="qr1s31" type="radio" value="4"  <%=radio4Value[31]%>/>4</label>
                    <label><input id="qr1s31" name="qr1s31" type="radio" value="5" <%=radio5Value[31]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s31" name="qc1s31"><%=comments[31]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s31" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr>
        <td class="time">11:45</td>
        <td class="track-01 egov">
            <div class="topic"><a class="session-title">Multi-tenancy and Cloud Computing for eGoverment Services</a><a
                    >Mifan Careem</a>,<span
                    class="company">CTO, <strong>Respere</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s32" name="qr1s32" type="radio" value="1" <%=radio1Value[32]%>/>1</label>
                    <label><input id="qr1s32" name="qr1s32" type="radio" value="2" <%=radio2Value[32]%> />2</label>
                    <label><input id="qr1s32" name="qr1s32" type="radio"  value="3" <%=radio3Value[32]%> />3</label>
                    <label><input id="qr1s32" name="qr1s32" type="radio" value="4"  <%=radio4Value[32]%>/>4</label>
                    <label><input id="qr1s32" name="qr1s32" type="radio" value="5" <%=radio5Value[32]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s32" name="qc1s32"><%=comments[32]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s32" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
        <td class="track-02 soaplatform">
            <div class="topic"><a class="session-title">Quality - The key to successful SOA</a><a
                   >Charitha Kankanamge</a>,<span class="company">Senior Technical Lead and Manager, <strong>WSO2</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s33" name="qr1s33" type="radio" value="1" <%=radio1Value[33]%>/>1</label>
                    <label><input id="qr1s33" name="qr1s33" type="radio" value="2" <%=radio2Value[33]%> />2</label>
                    <label><input id="qr1s33" name="qr1s33" type="radio"  value="3" <%=radio3Value[33]%> />3</label>
                    <label><input id="qr1s33" name="qr1s33" type="radio" value="4"  <%=radio4Value[33]%>/>4</label>
                    <label><input id="qr1s33" name="qr1s33" type="radio" value="5" <%=radio5Value[33]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s33" name="qc1s33"><%=comments[33]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s33" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
    </tr>
    <tr>
        <td class="time">13:30</td>
        <td class="track-01 enterprise">
            <div class="topic"><a class="session-title">Cuban Experiences with SOA and the WSO2 Suite</a><a
                    >Jorge Infante Osorio,</a><br><span
                    class="company">Universidad de las Ciencias Informáticas</span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s34" name="qr1s34" type="radio" value="1" <%=radio1Value[34]%>/>1</label>
                    <label><input id="qr1s34" name="qr1s34" type="radio" value="2" <%=radio2Value[34]%> />2</label>
                    <label><input id="qr1s34" name="qr1s34" type="radio"  value="3" <%=radio3Value[34]%> />3</label>
                    <label><input id="qr1s34" name="qr1s34" type="radio" value="4"  <%=radio4Value[34]%>/>4</label>
                    <label><input id="qr1s34" name="qr1s34" type="radio" value="5" <%=radio5Value[34]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s34" name="qc1s34"><%=comments[34]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s34" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
        <td class="track-02 enterprise">
            <div class="topic"><a class="session-title">Using WSO2 as a Mobile Services Platform</a><a
                    >Simon Bilton,</a><br><span class="company">Head of Professional Services, <strong>Gödel
                Technologies Europe</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s35" name="qr1s35" type="radio" value="1" <%=radio1Value[35]%>/>1</label>
                    <label><input id="qr1s35" name="qr1s35" type="radio" value="2" <%=radio2Value[35]%> />2</label>
                    <label><input id="qr1s35" name="qr1s35" type="radio"  value="3" <%=radio3Value[35]%> />3</label>
                    <label><input id="qr1s35" name="qr1s35" type="radio" value="4"  <%=radio4Value[35]%>/>4</label>
                    <label><input id="qr1s35" name="qr1s35" type="radio" value="5" <%=radio5Value[35]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s35" name="qc1s35"><%=comments[35]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s35" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>
    <tr>
        <td class="time">14:15</td>
        <td class="track-01 enterprise">
            <div class="topic"><a class="session-title">Advanced Business Process Instance Monitoring in WSO2 Carbon</a><a
                    >David Schumm</a>,<span class="company">Research Assistant, <strong>Institute
                of Architecture of Application Systems (IAAS)</strong></span></div>
             <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s36" name="qr1s36" type="radio" value="1" <%=radio1Value[36]%>/>1</label>
                    <label><input id="qr1s36" name="qr1s36" type="radio" value="2" <%=radio2Value[36]%> />2</label>
                    <label><input id="qr1s36" name="qr1s36" type="radio"  value="3" <%=radio3Value[36]%> />3</label>
                    <label><input id="qr1s36" name="qr1s36" type="radio" value="4"  <%=radio4Value[36]%>/>4</label>
                    <label><input id="qr1s36" name="qr1s36" type="radio" value="5" <%=radio5Value[36]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s36" name="qc1s36"><%=comments[36]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s36" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
        <td class="track-02 enterprise">
            <div class="topic"><a class="session-title">A Dynamic Telecommunications SOA platform - A WSO2 and 2degrees
                Mobile Ltd Co-creation</a><a>Neeraj Satija</a>,<span
                    class="company">Software Development Manager, <strong>Two Degrees Mobile Limited</strong></span>
            </div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s37" name="qr1s37" type="radio" value="1" <%=radio1Value[37]%>/>1</label>
                    <label><input id="qr1s37" name="qr1s37" type="radio" value="2" <%=radio2Value[37]%> />2</label>
                    <label><input id="qr1s37" name="qr1s37" type="radio"  value="3" <%=radio3Value[37]%> />3</label>
                    <label><input id="qr1s37" name="qr1s37" type="radio" value="4"  <%=radio4Value[37]%>/>4</label>
                    <label><input id="qr1s37" name="qr1s37" type="radio" value="5" <%=radio5Value[37]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s37" name="qc1s37"><%=comments[37]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s37" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>
        </td>
    </tr>

    <tr>
        <td class="time">15:30</td>
        <td colspan="2">
            <div class="topic"><a class="session-title">Engineering to take over the world</a>Samisa
                Abeysinghe,<br><span class="company">Vice President Engineering, <strong>WSO2</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s38" name="qr1s38" type="radio" value="1" <%=radio1Value[38]%>/>1</label>
                    <label><input id="qr1s38" name="qr1s38" type="radio" value="2" <%=radio2Value[38]%> />2</label>
                    <label><input id="qr1s38" name="qr1s38" type="radio"  value="3" <%=radio3Value[38]%> />3</label>
                    <label><input id="qr1s38" name="qr1s38" type="radio" value="4"  <%=radio4Value[38]%>/>4</label>
                    <label><input id="qr1s38" name="qr1s38" type="radio" value="5" <%=radio5Value[38]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s38" name="qc1s38"><%=comments[38]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s38" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
    </tr>
    <tr class="main">
        <td class="time">16:15</td>
        <td colspan="2">
            <div class="topic"><a class="session-title">Keynote: WSO2 Vision and Roadmap</a>Paul Fremantle,<span
                    class="company">Founder &amp; CTO <strong>WSO2</strong></span></div>
            <table>
                <tr><td><p>
                    <label>(Poor)<input id="qr1s39" name="qr1s39" type="radio" value="1" <%=radio1Value[39]%>/>1</label>
                    <label><input id="qr1s39" name="qr1s39" type="radio" value="2" <%=radio2Value[39]%> />2</label>
                    <label><input id="qr1s39" name="qr1s39" type="radio"  value="3" <%=radio3Value[39]%> />3</label>
                    <label><input id="qr1s39" name="qr1s39" type="radio" value="4"  <%=radio4Value[39]%>/>4</label>
                    <label><input id="qr1s39" name="qr1s39" type="radio" value="5" <%=radio5Value[39]%> />5(Excellent)</label>
                </p></td></tr>
                <tr><td><textarea  rows="2" id="qc1s39" name="qc1s39"><%=comments[39]%></textarea></td></tr>
                <tr><td><input type="button" value="Submit" class="button" id="1s39" onclick="submitFeedBack(this.id)"/></td></tr>
            </table>

        </td>
    </tr>
    </tbody>
</table>


</div>
<!-- End loginBox -->

<!--
<div class="stickyFooter">
<input type="submit" value="Submit" class="button" style="margin-right:10px;" />
<input type="reset" value="Reset"  class="button" />
</div>
-->
</div>
</form>
</div>

</div>
<!-- ending pageSizer -->


</body>
</html>
</fmt:bundle>


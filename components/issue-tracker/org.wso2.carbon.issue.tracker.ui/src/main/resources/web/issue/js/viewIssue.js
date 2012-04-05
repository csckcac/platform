function submitPaginatedIssueData(pageNumber,dropdown) {

    sessionAwareFunction(function() {
        document.getElementById("requestedPage").value = pageNumber;
        var myindex =document.getElementById("viewAccount").selectedIndex;
        var issueForm = document.getElementById("issuesForm");
        issueForm.submit();
    });
}


function viewAddSchemaUI() {
    var addSelector = document.getElementById('addMethodSelector');
    var selectedValue = addSelector.options[addSelector.selectedIndex].value;

    var uploadUI = document.getElementById('uploadUI');
    var importUI = document.getElementById('importUI');

    if (selectedValue == "upload") {

        uploadUI.style.display = "";
        importUI.style.display = "none";

    } else if (selectedValue == "import") {

        uploadUI.style.display = "none";
        importUI.style.display = "";

    }
}
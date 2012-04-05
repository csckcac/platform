function showOverallQs() {

  var overallQsSymbolMax =  document.getElementById('overallQsSymbolMax');
  var overallQs = document.getElementById('overallQs');
  if(overallQs.style.display == 'none') {
    overallQsSymbolMax.setAttribute('style','background-image:url(../images/minus.gif);');
    overallQs.style.display = '';
  } else {
      overallQsSymbolMax.setAttribute('style','background-image:url(../images/plus.gif);');
      overallQs.style.display = 'none';
  }
}

function areaOnFocus(element, inputText)
{
     if( document.getElementById(element).value == inputText)
     {
    	 document.getElementById(element).value='';
     }
}

function areaOnBlur(element, inputText)
{
     if( document.getElementById(element).value=='')
     {
    	 document.getElementById(element).value = inputText;
     }
}

function showDay1Qs() {
	  var day1QsSymbolMax =  document.getElementById('day1QsSymbolMax');
	  var day1Qs = document.getElementById('day1Qs');
	  if(day1Qs.style.display == 'none') {
	    day1QsSymbolMax.setAttribute('style','background-image:url(../images/minus.gif);');
	    day1Qs.style.display = '';
	  } else {
	      day1QsSymbolMax.setAttribute('style','background-image:url(../images/plus.gif);');
	      day1Qs.style.display = 'none';
	  }
	}

function showday2Qs() {
	  var day2QsSymbolMax =  document.getElementById('day2QsSymbolMax');
	  var day2Qs = document.getElementById('day2Qs');
	  if(day2Qs.style.display == 'none') {
	    day2QsSymbolMax.setAttribute('style','background-image:url(../images/minus.gif);');
	    day2Qs.style.display = '';
	  } else {
	      day2QsSymbolMax.setAttribute('style','background-image:url(../images/plus.gif);');
	      day2Qs.style.display = 'none';
	  }
	}

function showday3Qs() {
	  var day3QsSymbolMax =  document.getElementById('day3QsSymbolMax');
	  var day3Qs = document.getElementById('day3Qs');
	  if(day3Qs.style.display == 'none') {
	    day3QsSymbolMax.setAttribute('style','background-image:url(../images/minus.gif);');
	    day3Qs.style.display = '';
	  } else {
	      day3QsSymbolMax.setAttribute('style','background-image:url(../images/plus.gif);');
	      day3Qs.style.display = 'none';
	  }
	}


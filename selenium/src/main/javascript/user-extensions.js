
var PAGE_LOAD_TIMEOUT = 3500;
var PAGE_PROCESS_TIMEOUT = 5000;

	
function contains(array, obj) {
	for (var i = 0; i < array.length; i++) {
		if (array[i] == obj) {
			return true;
		}
	}
	return false;
}
					
function getIthDayFromDate(date, i) {
	var millisecondsInDay = 86400000;   
	var time = date.getTime() + i * millisecondsInDay;
	var returnDate = new Date(time);
	return returnDate;
}
    
function getUrlForDate(searchUrl, date) {
	var day = date.getDate(); var month = (date.getMonth() + 1); var year = date.getFullYear();
	var dateQuery = "&tbs=cdr%3A1%2Ccd_min%3A" + month + "%2F" + day + "%2F" + year + "%2Ccd_max%3A" + month + "%2F" + day + "%2F" + year;
	
	return searchUrl + dateQuery;
}  
	
function getLinksToVisit(element) {	
	var links = new Array();
	var headings = element.getElementsByTagName("h3");

	for (var i = 0; i < headings.length; i++) {
		var linksInHeading = headings[i].getElementsByTagName("a");
		var link = linksInHeading[0].getAttribute("href");
		
		links.push(link);
	}
	
	return links;
}

/**
* Selenium IDE Action that performs a day-by-day search for a given Google search url and then visits the search result pages
*
* @param searchUrl - any Google search url, inputed as target through Selenium IDE UI
* @param parameterString - comma-separated parameters in the following format startDateString[MM/dd/yyyy],numberOfDays. 
* For example, parameter string "6/9/2013,2" would start the search from June 9 and go back 2 days
*/	
Selenium.prototype.doGoogleSearchDayByDayFromGivendDay = function(searchUrl, parameterString) {
	var outerThis = this;
	var linksToVisit = [];
	var givenDayString = parameterString.split(",")[0]; 
	var numberOfDays = parseInt(parameterString.split(",")[1]);
    var givenDay = new Date(givenDayString);  
	
	function processSearchPage(i, page) {
		var start = page * 100;
		var ithDay = getIthDayFromDate(givenDay, -i);
		var urlForDateAndStart = getUrlForDate(searchUrl, ithDay) + "&start=" + start;
		
		outerThis.doOpen(urlForDateAndStart);
		
		setTimeout(function() {
			outerThis.doEcho("Processing search page " + urlForDateAndStart);
			var element = outerThis.page().findElement("css=#rso");
			var links = getLinksToVisit(element);
			
			for(var i = 0; i < links.length; i++) {
				var link = links[i];
				
				if (!contains(linksToVisit, link)) {
					linksToVisit.push(link);
				}
			}
				
			outerThis.doEcho("Found " + links.length + " links to visit; total: " + linksToVisit.length);
		}, PAGE_LOAD_TIMEOUT);
	};
    
	processSearchPage(0, 0);
	
	var currentPage = 1;
	var currentDay = 0;
	
	for(var i = 1; i < 10; i++) {		
		setTimeout(function() {
			processSearchPage(currentDay, currentPage++);
		}, i * PAGE_PROCESS_TIMEOUT);
	}
	
	setTimeout(function() {
		currentPage = 0;
		for (var i = 1; i <= numberOfDays; i++) {
			for(var j = 0; j < 10; j++) {			
				setTimeout(function() {
					processSearchPage(currentDay++ / 10, currentPage++ % 10);
				}, ((i - 1) * 10 + j + 1) * PAGE_PROCESS_TIMEOUT);
			}
		}
	}, 10 * PAGE_PROCESS_TIMEOUT);
	
	setTimeout(function() {
		outerThis.doEcho("Total " + linksToVisit.length + " links to visit");
		
		var currentIndex = 0;
		for (var i = 0; i < linksToVisit.length; i++) {
			setTimeout(function() {
				outerThis.doOpen(linksToVisit[currentIndex++]);
			}, i * PAGE_LOAD_TIMEOUT);
		}
	}, 20 * numberOfDays * PAGE_PROCESS_TIMEOUT);
};
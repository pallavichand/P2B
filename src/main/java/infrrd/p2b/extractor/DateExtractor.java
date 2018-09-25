package infrrd.p2b.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import infrrd.p2b.entity.DocumentDetails;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class DateExtractor implements DocumentDetailsExtractor  {


	@Override
	public DocumentDetails extract(String ocrText, DocumentDetails docDetails) {
		ocrText = dateCorrection(ocrText);
		List<String> dateExtractorRegex = new ArrayList<String>();
		dateExtractorRegex.add("(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)([a-z]{1,7})?(\\s)?\\d{1,2}[.,\\s]\\s?\\d{4}");	
		dateExtractorRegex.add("(?<!\\d)([0]?[1-9]|[1][1-2])[.\\--\\/]([0-2]?[1-9]|[3][0-1])[.\\--\\/]\\d{2,4}(?!\\d)");
		dateExtractorRegex.add("(?<!\\d)\\d{4}[.\\--\\/]([0]?[1-9]|[1][1-2])[.\\--\\/]([0-2]?[1-9]|[3][0-1])(?!\\d)");
		for (String dateRegex : dateExtractorRegex) {
			Pattern datePattern = Pattern.compile(dateRegex);
			Matcher dateMatcher = datePattern.matcher(ocrText);
			if(dateMatcher.find()){
				String date = dateMatcher.group().trim();
				docDetails.setBillDate(dateMatcher.group());
				log.info("Date Value Found  --> {}", date);
				return docDetails;
			}
		}
		return docDetails;
	}
	

	private  String dateCorrection(String ocrText) {
		Map<String, String> dateCorrect = new HashMap<String, String>();
		String dateCorrectRegex = "\\W({0})\\W";
		ocrText =ocrText.toLowerCase();
	    dateCorrect.put("ptember","september");
	    dateCorrect.put("2007","2017");
		for (Entry<String, String> dateVal : dateCorrect.entrySet()) {
			String regexVal = dateCorrectRegex.replace("{0}",dateVal.getKey() );
			Pattern datePattern = Pattern.compile(regexVal);
			Matcher dateMatcher = datePattern.matcher(ocrText);
			while(dateMatcher.find()){
				ocrText = ocrText.replace(dateMatcher.group(), " "+dateVal.getValue()+" ");
			}
		}
		return ocrText;
	}


	@Override
	public DocumentDetails extractRem(String ocrText, DocumentDetails docDetails) {
		// TODO Auto-generated method stub
		return null;
	}

}

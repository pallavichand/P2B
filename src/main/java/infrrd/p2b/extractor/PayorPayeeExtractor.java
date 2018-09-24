package infrrd.p2b.extractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import infrrd.p2b.entity.DocumentDetails;
import infrrd.p2b.extractor.test.ChequeExtractorTest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PayorPayeeExtractor  implements DocumentDetailsExtractor {
String []  regexForPayorByKeyword = { "[a-z\\s,]+(inc|llc)","(?<=(account)\\W)([a-z-\\s]+)"};

@Override
public DocumentDetails extract(String ocrText, DocumentDetails docDetails) {
	
	String payor = getPayorByKeywordRegex(ocrText);
	docDetails.setPayor(payor);
	return docDetails;
}

@SuppressWarnings("deprecation")
private String getPayorByKeywordRegex(String ocrText) {
	String payor = null;
	String[] ocrTextArray = ocrText.split("\\n");
	boolean payorFound=false;
	for (String payorRegex : regexForPayorByKeyword) {
		int count = 0;
		for (String lineText : ocrTextArray) {
			count++;
			String payorText = lineText.toLowerCase();
			Pattern payorPattern = Pattern.compile(payorRegex);
			Matcher payorMatcher = payorPattern.matcher(payorText);
			if (payorMatcher.find()) {
				payor= removeUselessWords(payorMatcher.group());
				payor = WordUtils.capitalize(payor);
				
				payorFound = true;
				log.info("Payor Found  -->  {}", payor);
			}
			if (payorFound || count > 12) {
				break;
			}
		}
		if (payorFound ) {
			break;
		}
		}
	return payor;
}

	private String removeUselessWords(String payorString) {
		String[] payorWords = payorString.split(" ");
		String newPayorString= "";
		boolean payorIsCorrect =false;
		for (String string : payorWords) {
			if(string.length() < 3 && !payorIsCorrect){
				continue;
			}
			else{
				payorIsCorrect = true;
				newPayorString = newPayorString+string+" ";
			}
		}
		String[] nonPayorWords = { " to ","acct " };
		for (String nonPayor : nonPayorWords) {
			if (newPayorString.contains(nonPayor)) {
				newPayorString = newPayorString.substring(newPayorString.indexOf(nonPayor) + nonPayor.length());
			}
		}
		return newPayorString.trim();
	}
	
}

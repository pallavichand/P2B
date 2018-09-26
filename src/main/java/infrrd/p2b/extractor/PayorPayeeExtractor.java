package infrrd.p2b.extractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import infrrd.p2b.entity.DocumentDetails;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("deprecation")
@Slf4j
public class PayorPayeeExtractor implements DocumentDetailsExtractor {
	String[] regexForPayorByKeyword = { "[a-z\\s,]+(inc|llc|supply)", "(?<=(account)\\W)([a-z-\\s]+)" };
	String specialCharacterRegex = "[;.,|:]";
	String poBox ="(pobox|p o box|p.o. box|p.o.box|po box|\\Wst\\W|\\Wave\\W)";

	@Override
	public DocumentDetails extract(String ocrText, DocumentDetails docDetails) {

		String payor = getPayorForChecque(ocrText);
		docDetails.setPayor(payor);
		String payee = getPayeeByForCheque(ocrText);
		docDetails.setPayee(payee);
		return docDetails;
	}
	private String getPayorForChecque(String ocrText) {
		String payor = getPayorByKeywordRegex(ocrText);
		return payor;
	}
	
	private String getPayorByKeywordRegex(String ocrText) {
		String payor = null;
		String[] ocrTextArray = ocrText.split("\\n");
		boolean payorFound = false;
		for (String payorRegex : regexForPayorByKeyword) {
			for (String lineText : ocrTextArray) {
				String payorText = lineText.toLowerCase();
				Pattern payorPattern = Pattern.compile(payorRegex);
				Matcher payorMatcher = payorPattern.matcher(payorText);
				if (payorMatcher.find()) {
					payor = removeUselessWords(payorMatcher.group());
					payor = WordUtils.capitalize(payor);

					payorFound = true;
					log.info("Payor Found  -->  {}", payor);
				}
				if (payorFound ) {
					break;
				}
			}
			if (payorFound) {
				break;
			}
		}
		return payor;
	}
	
	private String getPayeeByForCheque(String ocrText) {
		String payee = getPayeeFromPOBox(ocrText);
		payee = WordUtils.capitalize(payee);
		log.info("Payee Found  -->  {}", payee);
		return payee;
	}

	private String getPayeeFromPOBox(String ocrText) {
		String []lines = ocrText.split("\\n");
		String payeeFound= null;
		int poboxCount=0;
		for (int i = 0; i < lines.length; i++) {
	//		String currText = lines[i].toLowerCase().replaceAll( specialCharacterRegex, StringUtils.SPACE ).replaceAll( " +", StringUtils.EMPTY )
//		            .trim();
			Pattern payorPattern = Pattern.compile(poBox);
			Matcher payorMatcher = payorPattern.matcher(lines[i].toLowerCase());
			if ( payorMatcher.find() ) {
				poboxCount++;
		            if((i >(lines.length/5)||poboxCount>1) && i>2){
		            	payeeFound= getPayeeValue(lines, i).toLowerCase();
		            }
		            
		        }
			
		}
		if (!StringUtils.isEmpty(payeeFound)) {
			payeeFound = refinePayeeValue(payeeFound);
		}
       
        return payeeFound;
	}

	private String getPayeeValue(String[] lines, int index) {

		String payeeVal = "";

		for (int i = index - 2; i < index; i++) {
			if (!isInvalidPayeeVal(lines[i])) {
				payeeVal = payeeVal+ lines[i]+" ";
			}
		}
		return payeeVal.trim();
	}

	private boolean isInvalidPayeeVal(String payeeString) {
		String[] nonPayeeWords = { "void", "after", "days" , "per"};
		for (String nonPayeeWord : nonPayeeWords) {
			if (payeeString.toLowerCase().contains(nonPayeeWord)) {
				return true;
			}
		}
		return false;
	}
	private String refinePayeeValue(String payeeFound) {
		String[] prePayeeWords = { "to", "oroer", "order", "pay", "he" };
		String[] postPayeeWords = { "envelofe" , "envelope", "of", "detals", "details", "be", " to "};
		payeeFound = payeeFound.replaceAll("[\\[\\]\\{\\};|:]", "").trim();
		String[] payeeWords = payeeFound.toLowerCase().split(" ");
		String newPayeeWord = "";
		boolean payeeWordStarted = false;
		for (String payeeWord : payeeWords){
			boolean wordRemoved = false;
			if(StringUtils.isNumeric(payeeWord)){
				wordRemoved =true;
				continue;
			}
			if(!payeeWordStarted){
			for  (String prePayeeWord : prePayeeWords) {
				if (payeeWord.equals(prePayeeWord) ) {
					wordRemoved =true;
					continue;
				}

			}
			}
			if(!wordRemoved){
				newPayeeWord = newPayeeWord+payeeWord+" ";
				payeeWordStarted =true;
			}

			
		}
		newPayeeWord = newPayeeWord.trim();
		for (String postPayeeWord : postPayeeWords) {
			if(newPayeeWord.contains(postPayeeWord) && (newPayeeWord.lastIndexOf(postPayeeWord) > newPayeeWord.length()/3)){
				newPayeeWord = newPayeeWord.substring(0, newPayeeWord.lastIndexOf(postPayeeWord) );
			}
		}
		newPayeeWord = newPayeeWord.replaceAll("[;.,<>,\\s\\/]+$", "").trim();
		return newPayeeWord;
	}
	private String removeUselessWords(String payorString) {
		String[] payorWords = payorString.split(" ");
		String newPayorString = "";
		boolean payorIsCorrect = false;
		for (String string : payorWords) {
			if (string.length() < 3 && !payorIsCorrect) {
				continue;
			} else {
				payorIsCorrect = true;
				newPayorString = newPayorString + string + " ";
			}
		}
		String[] nonPayorWords = { " to ", "acct " };
		for (String nonPayor : nonPayorWords) {
			if (newPayorString.contains(nonPayor)) {
				newPayorString = newPayorString.substring(newPayorString.indexOf(nonPayor) + nonPayor.length());
			}
		}
		return newPayorString.trim();
	}
	@Override
	public DocumentDetails extractRem(String ocrText, DocumentDetails docDetails) {
		// TODO Auto-generated method stub
		return null;
	}

}

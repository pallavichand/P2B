package infrrd.p2b.train.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = { "classpath:application.properties" })
public class PayerPayeeDataSet {
	@Value("${payerpayeelist}")
	private String payerpayeeValue;
	
	List<String> payerpayeeList;

	public List<String> getPayerpayeeList() {
		payerpayeeList =new ArrayList<String>();
		String []values= payerpayeeValue.split("[,]");
		for (String string : values) {
			payerpayeeList.add(string);
		}
		return payerpayeeList;
	}

	public Map<String,String> comparePayerpayeeList(String ocrText) {
		payerpayeeList = getPayerpayeeList();
		String dataSearchRegex = "\\W({0})\\W";
		Map<String, String> indexPlusCompany = new HashMap<String,String>();
		List<String> values= new ArrayList<String>();
		for (String payerPayee : payerpayeeList) {
			String [] multipleValues = payerPayee.split("[:]");
			
			for (String value : multipleValues) {
				String regexVal = dataSearchRegex.replace("{0}",value );
				Pattern datePattern = Pattern.compile(regexVal);
				Matcher dateMatcher = datePattern.matcher(ocrText);
				while(dateMatcher.find()){
					if(!values.contains(multipleValues[0])){
						values.add(multipleValues[0]);
						//indexPlusCompany.put(dateMatcher.start(), multipleValues[0]);
					}
					
				}
			}
		}
		
		
		return indexPlusCompany;
		
	}

}


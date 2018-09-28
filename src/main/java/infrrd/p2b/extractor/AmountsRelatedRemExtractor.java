package infrrd.p2b.extractor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class AmountsRelatedRemExtractor {
	
	
	public Map<String,String> getFields (String ocrText){
		
		
		
		Map<String ,String> output  = new HashMap<String,String>();
		ocrText = ocrText.toLowerCase();
		ocrText = ocrText.replace(" usb ", " usd ");
		String[] lines  = ocrText.split("\n");
		boolean found = false;
		
		
		Pattern  p;
		
		
		for (String line : lines) {
			
			
			if (ocrText.contains("tvp nyc")) {

				String totalUsdRegex = "(?<=usd.{0,500})([$]?\\d[\\d.,]+)";

				p = Pattern.compile(totalUsdRegex);
				Matcher m3 = p.matcher(line);

				if (m3.find()) {
					System.out.println(m3.group());
					found = true;
					output.put("TotalNetAmount", m3.group(1));
					output.put("TotalGrossAmount", "");
					output.put("TotalDiscountAmount", "");
					break;
				}

			}
			
			if (!found) {

				String totalAllRegex = "(?<=total.{0,500})([$]?\\d[\\d.,]+)[ ]+(-?[$]?\\d[\\d.,]+)[ ]+([$]?\\d[\\d.,]+)";
				if (line.contains("total")) {

					p = Pattern.compile(totalAllRegex);
					Matcher m = p.matcher(line);

					if (m.find()) {
						System.out.println(m.group());
						found = true;
						output.put("TotalGrossAmount", m.group(1));
						output.put("TotalDiscountAmount", m.group(2));
						output.put("TotalNetAmount", m.group(3));
						break;
					}

				}
			}

			if (!found) {
				String totalRegex = "(?<=(total|payment|check am).{0,100})([$]?\\d[\\d.,]+)";
				p = Pattern.compile(totalRegex);
				Matcher m1 = p.matcher(line);

				if (m1.find()) {
					found = true;
					output.put("TotalNetAmount", m1.group(2));
					output.put("TotalGrossAmount","");
					output.put("TotalDiscountAmount", "");
					break;
				}

			}

		}
		
		
		return output;
		
		
	}

}

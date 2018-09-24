package infrrd.p2b.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChequeDetails extends DocumentDetails{
	String payee;
public ChequeDetails(DocumentDetails docDetails) {
		this.setAmount(docDetails.getAmount());
	}


}

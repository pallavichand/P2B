package infrrd.p2b.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FieldDetails {
	String values;
	boolean isVicinity;
	String vicinityWord;
	boolean isSpecialSymbol;
	String specialSymbolValue;
}

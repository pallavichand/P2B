package infrrd.p2b.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RemittenceHeader {
String header;
int startIndex;
int endIndex;
}

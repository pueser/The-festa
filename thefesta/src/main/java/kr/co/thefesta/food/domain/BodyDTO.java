package kr.co.thefesta.food.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class BodyDTO {

	private	ItemsDTO items;
	private int numOfRows;
	private int pageNo;
	private int totalCount;
	
}

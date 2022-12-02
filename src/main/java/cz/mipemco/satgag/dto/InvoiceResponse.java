package cz.mipemco.satgag.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Pemčák
 * (c) 2022 ConfigAir LLC. All Rights Reserved.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceResponse
{

	public String status;
	public InvoiceAction successAction = new InvoiceAction();
	public String pr;
	public List<String> routes = new ArrayList<>();
	public boolean disposable = false;
	public String reason;

	public class InvoiceAction {
		public String tag;
		public String message;

	}
}

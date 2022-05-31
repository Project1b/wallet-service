package pe.com.bank.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletDebitCardDTO {
	
	private String debitCardId;
	private Double amount;

}

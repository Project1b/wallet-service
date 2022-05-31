package pe.com.bank.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletOperationDTO {
	
	private Double amount;
	private Long sourcePhoneNumber;
	private Long destinationPhoneNumber;
	private String sourceWalletId;
	private String destinationWalletId;
	private String message;

}

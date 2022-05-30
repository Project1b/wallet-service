package pe.com.bank.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponseDTO {
	
	private String messageCode;
	private String messageDescription;

}

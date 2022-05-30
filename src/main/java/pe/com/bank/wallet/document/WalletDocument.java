package pe.com.bank.wallet.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "wallet")
public class WalletDocument {
	
	@Id
	private String walletId;
	private String documentType;
	private Integer documentNumber;
	private Double balance;
	private Integer phoneNumber;
	private Long phoneImei;
	private String email;
	private String debitCardId;

}

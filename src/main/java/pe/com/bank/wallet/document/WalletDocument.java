package pe.com.bank.wallet.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "wallet")
public class WalletDocument implements Serializable {
	
	@Id
	private String walletId;
	private String documentType;
	private Long documentNumber;
	private Double balance;
	private Long phoneNumber;
	private Long phoneImei;
	private String email;
	private String debitCardId;

}

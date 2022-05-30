package pe.com.bank.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletOperationAccountDTO {

    private String sourceCardId;
    private String destinationCardId;
    private Double amount;

}

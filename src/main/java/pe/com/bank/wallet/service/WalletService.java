package pe.com.bank.wallet.service;

import pe.com.bank.wallet.document.WalletDocument;
import pe.com.bank.wallet.dto.WalletOperationDTO;
import pe.com.bank.wallet.dto.WalletResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WalletService {

	public Flux<WalletDocument> getAllWallet();
	public Mono<WalletDocument> getWalletById(String walletId);
	public Mono<WalletDocument> saveWallet(WalletDocument walletDocument);
	public Mono<WalletDocument> updateWalletById(WalletDocument walletDocument,String walletId);
	public Mono<Void> deleteWalletById(String walletId);
	public Mono<WalletResponseDTO> operationWallet(WalletOperationDTO walletOperationDTO);
	public Mono<WalletResponseDTO> asociateDebitCard(String walletId,String debitCardId);
	
}

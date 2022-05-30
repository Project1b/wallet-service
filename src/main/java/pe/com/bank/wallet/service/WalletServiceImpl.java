package pe.com.bank.wallet.service;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import pe.com.bank.wallet.document.WalletDocument;
import pe.com.bank.wallet.dto.WalletOperationDTO;
import pe.com.bank.wallet.dto.WalletResponseDTO;
import pe.com.bank.wallet.repository.WalletRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class WalletServiceImpl implements WalletService{
	
	
	WalletRepository walletRepository;
	private StreamBridge streamBridge;
	
	public Flux<WalletDocument> getAllWallet(){
		return walletRepository.findAll();
	}
	
	public Mono<WalletDocument> getWalletById(String walletId){
		return walletRepository.findById(walletId);
	}
	
	public Mono<WalletDocument> saveWallet(WalletDocument walletDocument){
		return walletRepository.save(walletDocument);
	}
	
	public Mono<WalletDocument> updateWalletById(WalletDocument updateWalletDocument,String walletId){
		return walletRepository.findById(walletId).flatMap(wallet -> {
			
			wallet.setDocumentType(updateWalletDocument.getDocumentType() !=null ? updateWalletDocument.getDocumentType():wallet.getDocumentType());
			wallet.setDocumentNumber(updateWalletDocument.getDocumentNumber() !=null ? updateWalletDocument.getDocumentNumber():wallet.getDocumentNumber());
			wallet.setBalance(updateWalletDocument.getBalance() !=null ? updateWalletDocument.getBalance():wallet.getBalance());
			wallet.setPhoneNumber(updateWalletDocument.getPhoneNumber() !=null ? updateWalletDocument.getPhoneNumber():wallet.getPhoneNumber());
			wallet.setPhoneImei(updateWalletDocument.getPhoneImei() !=null ? updateWalletDocument.getPhoneImei():wallet.getPhoneImei());
			wallet.setEmail(updateWalletDocument.getEmail() !=null ? updateWalletDocument.getEmail():wallet.getEmail());
			wallet.setDebitcardId(updateWalletDocument.getDebitcardId() !=null ? updateWalletDocument.getDebitcardId():wallet.getDebitcardId());
			return walletRepository.save(wallet);
		});
	}
	
	public Mono<WalletResponseDTO> operationWallet(WalletOperationDTO walletOperationDTO){
		return walletRepository.findByPhoneNumber(walletOperationDTO.getSourcePhoneNumber()).flatMap( sourceWallet ->{
			walletOperationDTO.setSourceWalletId(sourceWallet.getWalletId());
			if(sourceWallet.getBalance()>= walletOperationDTO.getAmount()) {
				sourceWallet.setBalance(sourceWallet.getBalance()-walletOperationDTO.getAmount());
				walletRepository.save(sourceWallet).subscribe();
				return walletRepository.findByPhoneNumber(walletOperationDTO.getDestinationPhoneNumber()).flatMap( destinationWallet -> {
					walletOperationDTO.setDestinationWalletId(destinationWallet.getWalletId());
					destinationWallet.setBalance(destinationWallet.getBalance()+walletOperationDTO.getAmount());
					return walletRepository.save(destinationWallet).flatMap( w -> {
						sendWalletDocument(walletOperationDTO);
						return Mono.just(new WalletResponseDTO("0000","successful operation"));
					});
				});
			}else {
				return Mono.just(new WalletResponseDTO("0002","amount not available in source wallet"));
			}
		});
	}
	
	
	public Mono<WalletResponseDTO> asociateDebitCard(String walletId,String debitCardId){
		
		return updateWalletById(new WalletDocument(null,null,null,null,null,null,null,debitCardId),walletId).flatMap( a -> {
			return Mono.just(new WalletResponseDTO("0000","successful operation"));
		});
		
	}
	
	
	private void sendWalletDocument(WalletOperationDTO walletOperationDTO) {
		 streamBridge.send("wallet-out-0",walletOperationDTO);
	}
	
	public Mono<Void> deleteWalletById(String walletId){
		return walletRepository.deleteById(walletId);
	}
}
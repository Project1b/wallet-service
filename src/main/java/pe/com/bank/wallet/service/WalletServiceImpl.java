package pe.com.bank.wallet.service;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import pe.com.bank.wallet.document.WalletDocument;
import pe.com.bank.wallet.dto.WalletOperationAccountDTO;
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

	RedisTemplate redisTemplate;
	
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
			wallet.setDebitCardId(updateWalletDocument.getDebitCardId() !=null ? updateWalletDocument.getDebitCardId():wallet.getDebitCardId());
			return walletRepository.save(wallet);
		});
	}
	
	public Mono<WalletResponseDTO> operationWallet(WalletOperationDTO walletOperationDTO){
		
		
		return walletRepository.findByPhoneNumber(walletOperationDTO.getSourcePhoneNumber()).flatMap( sourceWallet ->{
			walletOperationDTO.setSourceWalletId(sourceWallet.getWalletId());
			if(sourceWallet.getBalance()>= walletOperationDTO.getAmount()) {
				sourceWallet.setBalance(sourceWallet.getBalance()-walletOperationDTO.getAmount());
				return walletRepository.save(sourceWallet).flatMap( saveSource -> {	
					return walletRepository.findByPhoneNumber(walletOperationDTO.getDestinationPhoneNumber()).flatMap( destinationWallet -> {
						walletOperationDTO.setDestinationWalletId(destinationWallet.getWalletId());
						destinationWallet.setBalance(destinationWallet.getBalance()+walletOperationDTO.getAmount());
						return walletRepository.save(destinationWallet).flatMap( saveDestination -> {
							sendWalletDocument(walletOperationDTO);
							
							if(sourceWallet.getDebitCardId()!=null || destinationWallet.getDebitCardId()!=null) {
								WalletOperationAccountDTO walletOperationAccountDTO = new WalletOperationAccountDTO();
								walletOperationAccountDTO.setAmount(walletOperationDTO.getAmount());
								walletOperationAccountDTO.setSourceCardId(sourceWallet.getDebitCardId());
								walletOperationAccountDTO.setSourceCardId(destinationWallet.getDebitCardId());
								senWalletOperationAccount(walletOperationAccountDTO);
							}
								
							return Mono.just(new WalletResponseDTO("0000","successful operation"));
						});
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

	private void senWalletOperationAccount(WalletOperationAccountDTO walletOperationAccountDTO){
		streamBridge.send("walletAccount-out-0",walletOperationAccountDTO);
	}
	
	public Mono<Void> deleteWalletById(String walletId){
		return walletRepository.deleteById(walletId);
	}

	public Mono<WalletDocument> findWalletPhoneById(int id) {
		String key = "wallet_" + id;
		ValueOperations<String, WalletDocument> operations = redisTemplate.opsForValue();
		if (redisTemplate.hasKey(key)){
			WalletDocument walle = operations.get(key);
			return Mono.create(walletMonoSink -> walletMonoSink.success(walle));
		}
		Mono<WalletDocument> walletMono = walletRepository.findByPhoneNumber(id);
		if (walletMono == null)
			return walletMono;
		walletMono.subscribe(wallet -> operations.set(key, wallet));
		return walletMono;
	}

}

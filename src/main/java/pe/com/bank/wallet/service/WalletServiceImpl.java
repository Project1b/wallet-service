package pe.com.bank.wallet.service;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import pe.com.bank.wallet.document.WalletDocument;
import pe.com.bank.wallet.dto.WalletDebitCardDTO;
import pe.com.bank.wallet.dto.WalletOperationAccountDTO;
import pe.com.bank.wallet.dto.WalletOperationDTO;
import pe.com.bank.wallet.dto.WalletResponseDTO;
import pe.com.bank.wallet.repository.WalletRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WalletServiceImpl implements WalletService{
	
	@Autowired
	WalletRepository walletRepository;
	@Autowired
	StreamBridge streamBridge;
	
	RedisTemplate redisTemplate;
	
	private final ReactiveRedisOperations<String, WalletDocument> operations;
	
	
	public WalletServiceImpl(ReactiveRedisOperations<String, WalletDocument> operations) {
        this.operations = operations;
    }

	
	
	public Flux<WalletDocument> getAllWallet(){
		return walletRepository.findAll();
	}
	
	public Mono<WalletDocument> getWalletById(String walletId){
        String key = "wallet_" + walletId;
        
        
        return operations.hasKey(key).flatMap( o -> {
        
            if (Boolean.TRUE.equals(o)) { 
            	System.out.println("test1 "+key);
            	return operations.opsForValue().get(key);
            }else {
            	System.out.println("test2: "+key);
            	return walletRepository.findById(walletId).flatMap( wallet ->{
            		System.out.println("test3: "+key);
            		operations.opsForValue().set(key,wallet);
            		return Mono.just(wallet);
            	});
            }
            	
        });
        
   
        
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
	
	public Mono<WalletDocument> updateWalletByDebitCardId(WalletDocument updateWalletDocument,String debitCardId){
		return walletRepository.findByDebitCardId(debitCardId).flatMap(wallet -> {
			
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
	
	
	public Mono<Void> deleteWalletById(String walletId){
		return walletRepository.deleteById(walletId);
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
								walletOperationAccountDTO.setDestinationCardId(destinationWallet.getDebitCardId());
								senWalletOperationAccount(walletOperationAccountDTO);
							}
								
							return Mono.just(new WalletResponseDTO("0000","successful operation"));
						});
					});
					
				});
				
			}else {
				return Mono.just(new WalletResponseDTO("0001","amount not available in source wallet"));
			}
		});
	}
	
	public Mono<WalletResponseDTO> asociateDebitCard(WalletDebitCardDTO walletDebitCardDTO,String walletId){
		
		return walletRepository.findById(walletId).flatMap( wallet -> {
			if(wallet.getDebitCardId()==null || wallet.getDebitCardId().equals("")) {
			return updateWalletById(new WalletDocument(null,null,null,null,null,null,null,walletDebitCardDTO.getDebitCardId()),walletId).flatMap( updateWallet -> {
				sendDebitCardIdAsociated(new WalletDebitCardDTO(updateWallet.getDebitCardId(),updateWallet.getBalance()));
				return Mono.just(new WalletResponseDTO("0000","successful operation"));
			});	
			}else {
				return Mono.just(new WalletResponseDTO("0001","wallet has already a card associated"));
			}
		});
		
		 	
	}	
	
	private void sendWalletDocument(WalletOperationDTO walletOperationDTO) {
		 streamBridge.send("wallet-transaction-out-0",walletOperationDTO);
	}
	
	private void senWalletOperationAccount(WalletOperationAccountDTO walletOperationAccountDTO){
		streamBridge.send("wallet-account-out-0",walletOperationAccountDTO);
	}
	
	private void sendDebitCardIdAsociated(WalletDebitCardDTO walletDebitCardDTO) {
		 streamBridge.send("wallet-debitCardAsociated-out-0",walletDebitCardDTO);
	}	
	
	 @Bean
	 Consumer<WalletDebitCardDTO> updateCurrentAmountAccount() {
	    return walletOperationDTO -> {
	    	
	    	updateWalletByDebitCardId(new WalletDocument(null,null,null,walletOperationDTO.getAmount(),null,null,null,null),walletOperationDTO.getDebitCardId())
	    	.subscribe();	           
	    };	
	 }	

	public Mono<WalletDocument> findWalletPhoneById(Long id) {
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

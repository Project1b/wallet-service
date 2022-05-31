package pe.com.bank.wallet.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import pe.com.bank.wallet.document.WalletDocument;
import reactor.core.publisher.Mono;


public interface WalletRepository extends ReactiveMongoRepository<WalletDocument, String>{
	
	Mono<WalletDocument> findByPhoneNumber(Long phoneNumber);
	Mono<WalletDocument> findByDebitCardId(String debitCardId);

}

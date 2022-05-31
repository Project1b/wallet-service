package pe.com.bank.wallet.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import pe.com.bank.wallet.document.WalletDocument;
import pe.com.bank.wallet.dto.WalletDebitCardDTO;
import pe.com.bank.wallet.dto.WalletOperationDTO;
import pe.com.bank.wallet.dto.WalletResponseDTO;
import pe.com.bank.wallet.service.WalletService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@RestController
@RequestMapping("/v1")
public class WalletController {
	
	WalletService walletService;
	
	
	@GetMapping("/getAllWallet")
	public Flux<WalletDocument> getAllWallet(){
		return walletService.getAllWallet();
	}
	
	@GetMapping("/getWalletById/{walletId}")
	public Mono<WalletDocument> getWalletById(@PathVariable String walletId){
		return walletService.getWalletById(walletId);
	}
	
	@PostMapping("/saveWallet")
	public Mono<WalletDocument> saveWallet(@RequestBody WalletDocument walletDocument){
		return walletService.saveWallet(walletDocument);		
	}
	
	@PutMapping("/updateWalletById/{walletId}")
	public Mono<WalletDocument> updateWalletById(@RequestBody WalletDocument walletDocument,@PathVariable String walletId){
		return walletService.updateWalletById(walletDocument, walletId);
	}
	
	@DeleteMapping("/deleteWalletById/{walletId}")
	public Mono<Void> deleteWalletById(String walletId){
		return walletService.deleteWalletById(walletId);
	}
	
	@PostMapping("/operationWallet")
	public Mono<WalletResponseDTO> operationWallet(@RequestBody WalletOperationDTO walletOperationDTO){
		return walletService.operationWallet(walletOperationDTO);
	}
	
	@PostMapping("/asociateDebitCard/{walletId}")
	public Mono<WalletResponseDTO> asociateDebitCard(@RequestBody WalletDebitCardDTO walletDebitCardDTO,@PathVariable String walletId){
		return walletService.asociateDebitCard(walletDebitCardDTO,walletId);
	}
	
	

	@GetMapping(value="/wallet/phone/{id}")
	public Mono<WalletDocument> getWallet(@PathVariable("id") Long id){
		return walletService.findWalletPhoneById(id);
	}

}

package pe.com.bank.wallet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import pe.com.bank.wallet.document.WalletDocument;

@Configuration
public class RedisConfiguration {
	
	@Bean
    ReactiveRedisOperations<String, WalletDocument> reactiveRedisOperations(
            ReactiveRedisConnectionFactory factory) {
		
		Jackson2JsonRedisSerializer<WalletDocument> serializer = new Jackson2JsonRedisSerializer<>(WalletDocument.class);
		
		RedisSerializationContext.RedisSerializationContextBuilder<String, WalletDocument> builder =
		        RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
		
		RedisSerializationContext<String, WalletDocument> context = builder.value(serializer).build();
		
        return new ReactiveRedisTemplate<>(factory,context);
    }

}
